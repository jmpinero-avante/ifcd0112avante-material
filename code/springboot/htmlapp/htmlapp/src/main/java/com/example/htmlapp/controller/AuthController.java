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
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

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
 * GET  /register    → muestra el formulario de registro (anónimo o admin)
 * POST /register    → crea el usuario
 * GET  /logout      → cierra la sesión y redirige al inicio
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
	 * Si el usuario ya está autenticado, se le redirige a /main.
	 */
	@GetMapping("/login")
	public String showLoginForm(Model model) {
		if (!authService.isAnonymous()) {
			return "redirect:/main";
		}
		return "html/auth/login";
	}

	/**
	 * Procesa el formulario de inicio de sesión.
	 *
	 * @param email    Correo electrónico del usuario.
	 * @param password Contraseña en texto plano.
	 * @param model    Modelo para pasar mensajes de error a la vista.
	 * @return Redirección o plantilla con error.
	 */
	@PostMapping("/login")
	public String processLogin(
		@RequestParam String email,
		@RequestParam String password,
		Model model
	) {
		try {
			boolean success = authService.login(email, password);
			if (success) {
				return "redirect:/main";
			}
			model.addAttribute("errorMessage", "Correo o contraseña incorrectos.");
			return "html/auth/login";
		} catch (Exception ex) {
			throw new OperationFailedException("Error al iniciar sesión.", 500, ex);
		}
	}

	// -------------------------------------------------------------------------
	// REGISTRO
	// -------------------------------------------------------------------------

	/**
	 * Muestra el formulario de registro.
	 *
	 * Permitido si:
	 *   - El usuario está anónimo (registro público).
	 *   - El usuario logado es administrador (alta manual).
	 */
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		if (!authService.isAnonymousOrAdmin()) {
			return "redirect:/main";
		}
		model.addAttribute("userForm", new User());
		return "html/auth/register";
	}

	/**
	 * Procesa el registro de usuario.
	 *
	 * Si es anónimo → se inicia sesión con la nueva cuenta.
	 * Si es admin → se crea el usuario sin alterar la sesión actual.
	 */
	@PostMapping("/register")
	public String processRegister(
		@ModelAttribute("userForm") User userForm,
		@RequestParam("passwordPlain") String passwordPlain,
		@RequestParam("passwordConfirm") String passwordConfirm,
		Model model
	) {
		if (!authService.isAnonymousOrAdmin()) {
			throw new OperationFailedException("No tiene permisos para registrar usuarios.", 403);
		}

		if (!passwordPlain.equals(passwordConfirm)) {
			model.addAttribute("errorMessage", "Las contraseñas no coinciden.");
			return "html/auth/register";
		}

		try {
			String email = userForm.getEmail();

			User newUser = userService.registerUser(
				userForm.getFullName(),
				email,
				passwordPlain
			);

			if (authService.isAnonymous()) {
				authService.login(email, passwordPlain);
				model.addAttribute("loggedUserName", newUser.getFullName());
				return "redirect:/main";
			}

			return "redirect:/userlist/list"; // alta por administrador

		} catch (OperationFailedException ex) {
			throw ex; // se maneja globalmente
		} catch (Exception ex) {
			throw new OperationFailedException("Error al registrar usuario.", 500, ex);
		}
	}

	// -------------------------------------------------------------------------
	// LOGOUT
	// -------------------------------------------------------------------------

	/**
	 * Cierra la sesión y redirige a la página de inicio con ?logout.
	 */
	@GetMapping("/logout")
	public String logout() {
		try {
			authService.logout();
			return "redirect:/?logout";
		} catch (Exception ex) {
			throw new OperationFailedException("Error al cerrar sesión.", 500, ex);
		}
	}
}

/*
===============================================================================
CONTROL EXPLÍCITO DE ACCESO A PÁGINAS PÚBLICAS
===============================================================================
Las páginas de login y registro realizan comprobaciones explícitas de acceso:
 - Los usuarios ya logados no pueden volver a /login.
 - Solo usuarios anónimos o administradores pueden acceder a /register.

Esto evita el uso innecesario de interceptores, manteniendo el flujo claro
y visible en el propio controlador.

===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. DIFERENCIA ENTRE LOGIN Y REGISTRO
------------------------------------
 - Login → autentica al usuario existente.
 - Register → crea una nueva cuenta (pública o administrada).

2. CONTROL DE SESIÓN
---------------------
El AuthService centraliza toda la lógica de sesión, incluyendo:
 - login(email, password)
 - logout()
 - isAnonymous(), isAdmin(), isAnonymousOrAdmin()

3. EXCEPCIONES Y ERRORES
-------------------------
 - 400 → Datos inválidos o contraseñas no coincidentes.
 - 403 → Falta de permisos para registrar.
 - 500 → Fallos inesperados de servidor.

4. OBJETIVO PEDAGÓGICO
------------------------
Este controlador enseña cómo:
 - Separar autenticación y registro en flujos independientes.
 - Usar helpers semánticos en AuthService para claridad.
 - Gestionar las sesiones de forma limpia y coherente.
===============================================================================
*/