// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.logic.AuthService;
import com.example.htmlapp.model.logic.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador de autenticación (login, registro y logout).
 *
 * Gestiona el alta de nuevos usuarios, el inicio de sesión y el cierre
 * de sesión de los usuarios registrados en la aplicación.
 *
 * Utiliza los servicios AuthService y UserService para manejar la lógica
 * de sesión y la persistencia en base de datos.
 *
 * ----------------------------------------------------------------------------
 * ESTRUCTURA DE RUTAS
 * ----------------------------------------------------------------------------
 * GET  /login       → muestra el formulario de inicio de sesión
 * POST /login       → procesa las credenciales
 * GET  /register    → muestra el formulario de registro
 * POST /register    → crea el usuario y lo inicia automáticamente
 * GET  /logout      → cierra la sesión y redirige al inicio
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA SEPARACIÓN DE RESPONSABILIDADES
 * ----------------------------------------------------------------------------
 * - UserService se encarga de la creación de usuarios (registro).
 * - AuthService se encarga de mantener la sesión (login / logout).
 * - Este controlador solo gestiona el flujo entre vistas y servicios,
 *   y decide qué plantilla Thymeleaf debe mostrarse.
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final UserService userService;

	// -------------------------------------------------------------------------
	// LOGIN
	// -------------------------------------------------------------------------

	/**
	 * Muestra el formulario de inicio de sesión.
	 *
	 * Si el usuario ya está autenticado, se le redirige a la página principal
	 * (/main) para evitar que intente iniciar sesión de nuevo.
	 *
	 * @param model Modelo de Thymeleaf (no se usa en esta vista, pero
	 *              podría servir para mostrar mensajes informativos).
	 * @return Plantilla auth/login.html o redirección a /main.
	 */
	@GetMapping("/login")
	public String showLoginForm(Model model) {
		if (authService.getLoggedUser().isPresent()) {
			return "redirect:/main";
		}
		return "html/auth/login";
	}

	/**
	 * Procesa el formulario de inicio de sesión.
	 *
	 * Si las credenciales son correctas, redirige al usuario a /main.
	 * Si no lo son, se recarga el formulario mostrando un mensaje de error.
	 *
	 * @param email    Correo electrónico del usuario.
	 * @param password Contraseña en texto plano (enviada desde el formulario).
	 * @param model    Modelo para pasar mensajes de error a la vista.
	 * @return Redirección o plantilla de error.
	 *
	 * ----------------------------------------------------------------------------
	 * POSIBLES VALORES DE RETURN
	 * ----------------------------------------------------------------------------
	 * - "redirect:/main" → redirige al usuario autenticado a la página principal.
	 * - "auth/login"     → recarga el formulario de login mostrando un error.
	 */
	@PostMapping("/login")
	public String processLogin(
		@RequestParam String email,
		@RequestParam String password,
		Model model
	) {
		boolean success = authService.login(email, password);

		if (success) {
			return "redirect:/main";
		} else {
			model.addAttribute("errorMessage", "Correo o contraseña incorrectos.");
			return "html/auth/login";
		}
	}

	// -------------------------------------------------------------------------
	// REGISTRO
	// -------------------------------------------------------------------------

	/**
	 * Muestra el formulario de registro de usuario.
	 *
	 * Si ya hay un usuario logado, se redirige a /main.
	 *
	 * @param model Modelo de Thymeleaf.
	 * @return Plantilla auth/register.html o redirección a /main.
	 */
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		if (authService.getLoggedUser().isPresent()) {
			return "redirect:/main";
		}

		model.addAttribute("userForm", new User());
		return "html/auth/register";
	}

	/**
	 * Procesa el formulario de registro de usuario.
	 *
	 * 1. Valida que las contraseñas coincidan.
	 * 2. Crea el nuevo usuario mediante UserService.
	 * 3. Inicia sesión automáticamente si el registro fue correcto.
	 *
	 * ----------------------------------------------------------------------------
	 * SOBRE LOS PARÁMETROS
	 * ----------------------------------------------------------------------------
	 * @param userForm
	 *     - Objeto User que se rellena automáticamente con los campos del formulario.
	 *     - Solo se usan los campos relevantes: fullName y email.
	 *     - Los campos de base de datos (id, salt, passwordHash, etc.)
	 *       se gestionan dentro de UserService.
	 *
	 * @param passwordPlain
	 *     - Contraseña en texto plano introducida por el usuario.
	 *     - No pertenece a la entidad User.
	 *     - Se pasa como parámetro separado porque el hash se genera en el servidor.
	 *
	 * @param passwordConfirm
	 *     - Segundo campo del formulario para confirmar la contraseña.
	 *     - Se compara con passwordPlain para evitar errores de escritura.
	 *
	 * @param model
	 *     - Permite enviar mensajes de error al formulario si ocurre
	 *       algún problema de validación.
	 *
	 * ----------------------------------------------------------------------------
	 * POSIBLES VALORES DE RETURN
	 * ----------------------------------------------------------------------------
	 * - "redirect:/main"
	 *     → Registro exitoso. Redirige al usuario ya logado a la página principal.
	 *
	 * - "auth/register"
	 *     → Error de validación local (como contraseñas no coincidentes).
	 *
	 * Si ocurre un error de negocio (como email duplicado), se lanza una
	 * OperationFailedException, que será gestionada globalmente por
	 * ErrorControllerAdvice y mostrará la plantilla error/operation-error.html.
	 */
	@PostMapping("/register")
	public String processRegister(
		@ModelAttribute("userForm") User userForm,
		@RequestParam("passwordPlain") String passwordPlain,
		@RequestParam("passwordConfirm") String passwordConfirm,
		Model model
	) {
		String email = userForm.getEmail();

		if (!passwordPlain.equals(passwordConfirm)) {
			model.addAttribute("errorMessage", "Las contraseñas no coinciden.");
			return "html/auth/register";
		}

		// El registro puede lanzar OperationFailedException (por ejemplo, si el email ya existe).
		User newUser = userService.registerUser(
			userForm.getFullName(),
			email,
			passwordPlain
		);

		// Inicia la sesión automáticamente tras el registro.
		authService.login(email, passwordPlain);
		model.addAttribute("loggedUserName", newUser.getFullName());
		return "redirect:/main";
	}

	// -------------------------------------------------------------------------
	// LOGOUT
	// -------------------------------------------------------------------------

	/**
	 * Cierra la sesión del usuario actual y redirige a la página de inicio.
	 *
	 * Este método llama a AuthService.logout(), que elimina los datos de la
	 * sesión HTTP del usuario y garantiza que las siguientes peticiones
	 * sean tratadas como anónimas.
	 *
	 * Tras el logout, se redirige a / con un parámetro ?logout para
	 * que la vista muestre un mensaje informativo.
	 *
	 * @return Redirección a la página inicial con aviso de logout.
	 *
	 * ----------------------------------------------------------------------------
	 * POSIBLES VALORES DE RETURN
	 * ----------------------------------------------------------------------------
	 * - "redirect:/?logout"
	 *     → El usuario ha cerrado sesión. Se añade el parámetro ?logout
	 *       para que la vista index.html muestre un mensaje confirmando
	 *       que el cierre se ha realizado correctamente.
	 */
	@GetMapping("/logout")
	public String logout() {
		authService.logout();
		return "redirect:/?logout";
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE @ModelAttribute Y @RequestParam
 * ----------------------------------------------------------------------------
 *
 * - @ModelAttribute permite que Spring cree automáticamente un objeto (por ejemplo,
 *   un User) y rellene sus campos a partir de los parámetros del formulario cuyos
 *   nombres coinciden con los setters del objeto. Por ejemplo, un input "name=email"
 *   asigna su valor a user.setEmail(...).
 *
 * - @RequestParam obtiene valores individuales de la petición HTTP (ya sea por GET
 *   o POST). Se usa cuando el campo no forma parte del modelo de la entidad o cuando
 *   se quieren recibir parámetros sueltos como las contraseñas o confirmaciones.
 *
 * En este controlador:
 *   - userForm (ModelAttribute) se usa para recibir los datos comunes del usuario
 *     (nombre, email...).
 *   - passwordPlain y passwordConfirm (RequestParam) se usan para los campos del
 *     formulario que no existen en la entidad User.
 *
 * Spring combina automáticamente ambos mecanismos en la misma petición:
 * crea el objeto User, lo rellena con los campos coincidentes y pasa los
 * parámetros individuales en las variables anotadas con @RequestParam.
 */