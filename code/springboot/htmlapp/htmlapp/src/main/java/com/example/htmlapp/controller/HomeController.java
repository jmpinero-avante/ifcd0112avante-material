// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.htmlapp.model.logic.AuthService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador principal de la aplicación.
 *
 * Gestiona la página de inicio (login / registro) y la redirección
 * a la página principal cuando el usuario ya está autenticado.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Controller
 * ----------------------------------------------------------------------------
 * Indica que esta clase forma parte de la capa de control (MVC).
 * Sus métodos devuelven nombres de plantillas Thymeleaf que Spring
 * resolverá automáticamente en `src/main/resources/templates/`.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @RequiredArgsConstructor
 * ----------------------------------------------------------------------------
 * Lombok genera automáticamente un constructor con los campos `final`
 * o marcados con `@NonNull`. Esto permite inyectar dependencias sin
 * necesidad de usar `@Autowired` explícitamente.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL PROPÓSITO DE ESTE CONTROLADOR
 * ----------------------------------------------------------------------------
 * Este controlador actúa como puerta de entrada de la aplicación:
 *  - Si el usuario NO está logado, muestra la página de inicio (`index.html`),
 *    donde puede hacer login o registrarse.
 *  - Si el usuario YA está logado, lo redirige automáticamente a `/main`,
 *    que es la página principal con el efecto parallax y el contenido central.
 *
 * También se encarga de establecer en el modelo la información contextual
 * necesaria para las vistas (nombre del usuario y fecha/hora de servidor).
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

	private final AuthService authService;

	/**
	 * Muestra la página de inicio o redirige a la principal
	 * dependiendo del estado de la sesión.
	 *
	 * Si hay un usuario logado, se redirige a `/main`.
	 * Si no, se muestra la página `index.html`, que incluye
	 * el formulario de login y de registro.
	 *
	 * @return Nombre de la plantilla o redirección.
	 */
	@GetMapping
	public String home() {
		// Si ya hay usuario logado → redirige a la página principal
		if (authService.isLogged()) {
			return "redirect:/main";
		}

		// Si no hay usuario logado → muestra la página de inicio
		return "html/index";
	}

	/**
	 * Página principal tras el login.
	 *
	 * Muestra el efecto parallax con texto de prueba.
	 *
	 * Si el usuario no está logado, se lanza una SecurityException,
	 * que será capturada por ErrorControllerAdvice y redirigirá
	 * a la página 403 personalizada.
	 *
	 * @param model Modelo para Thymeleaf.
	 * @return Plantilla `main.html`.
	 */
	@GetMapping("/main")
	public String mainPage(Model model) {
		var user = authService.getLoggedUser()
			.orElseThrow(() ->
				new SecurityException("Debe iniciar sesión para acceder."));

		// Información contextual para la vista
		model.addAttribute("loggedUserName", user.getFullName());
		model.addAttribute("loggedUserAdmin", user.isAdmin());
		model.addAttribute("serverNow", java.time.LocalDateTime.now());

		// Devuelve la plantilla principal (lorem ipsum con parallax)
		return "html/main";
	}
}

/*
 * ----------------------------------------------------------------------------
 * CONTROL EXPLÍCITO DE ACCESO EN PÁGINAS PÚBLICAS
 * ----------------------------------------------------------------------------
 * La página raíz ("/") realiza una comprobación directa del estado de sesión:
 *
 *     if (authService.isLogged()) {
 *         return "redirect:/main";
 *     }
 *
 * Esto evita que usuarios autenticados regresen al inicio público o a los
 * formularios de login/registro, garantizando una navegación coherente.
 *
 * ----------------------------------------------------------------------------
 * DIFERENCIA RESPECTO A PÁGINAS PRIVADAS
 * ----------------------------------------------------------------------------
 * En las páginas que requieren sesión (como `/main` o `/user/...`),
 * el control se realiza automáticamente a través de `PermissionsService`
 * o mediante la validación dentro del propio método (como aquí con
 * `getLoggedUser().orElseThrow()`).
 *
 * ----------------------------------------------------------------------------
 * OBJETIVO PEDAGÓGICO
 * ----------------------------------------------------------------------------
 * Este controlador enseña cómo:
 *  - Controlar manualmente el acceso a rutas públicas y privadas.
 *  - Redirigir de forma clara según el estado de la sesión.
 *  - Mostrar información contextual (usuario, hora, rol) en las vistas.
 */