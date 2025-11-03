// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import java.time.LocalDateTime;

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

	// -------------------------------------------------------------------------
	// PÁGINA DE INICIO (PÚBLICA)
	// -------------------------------------------------------------------------

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
		if (!authService.isAnonymous()) {
			return "redirect:/main";
		}

		// Si no hay usuario logado → muestra la página de inicio
		return "html/index";
	}

	// -------------------------------------------------------------------------
	// PÁGINA PRINCIPAL (PRIVADA)
	// -------------------------------------------------------------------------

	/**
	 * Página principal tras el login.
	 *
	 * Muestra el efecto parallax con texto de prueba.
	 *
	 * Si el usuario no está logado, se lanza una SecurityException,
	 * que será capturada por ErrorControllerAdvice y mostrará
	 * la página 403 personalizada.
	 *
	 * @param model Modelo para Thymeleaf.
	 * @return Plantilla `main.html`.
	 */
	@GetMapping("/main")
	public String mainPage(Model model) {
		var user = authService.getUser()
			.orElseThrow(() -> new SecurityException("Debe iniciar sesión para acceder."));

		// Información contextual para la vista
		model.addAttribute("loggedUserName", user.getFullName());
		model.addAttribute("loggedUserAdmin", user.isAdmin());
		model.addAttribute("serverNow", LocalDateTime.now());

		// Devuelve la plantilla principal (parallax + contenido central)
		return "html/main";
	}
}

/*
===============================================================================
CONTROL EXPLÍCITO DE ACCESO EN PÁGINAS PÚBLICAS Y PRIVADAS
===============================================================================
1. Página raíz ("/") → pública.
   - Si el usuario está logado, se redirige a /main.
   - Si es anónimo, se muestra index.html con opciones de login/registro.

2. Página principal ("/main") → privada.
   - Solo accesible con sesión activa.
   - Si no hay sesión, se lanza SecurityException (403).

===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. CONSISTENCIA CON AuthService
-------------------------------
Se usa `isAnonymous()` y `getUser()` para comprobar el estado de sesión.
Esto mantiene coherencia con otros controladores (Auth, User, UserList).

2. MODELO PARA LA VISTA
------------------------
La plantilla `main.html` recibe:
 - `loggedUserName` → nombre del usuario actual.
 - `loggedUserAdmin` → booleano que indica si es admin.
 - `serverNow` → fecha/hora del servidor.

3. OBJETIVO PEDAGÓGICO
------------------------
Demuestra cómo:
 - Distinguir rutas públicas y privadas en el controlador.
 - Usar helpers semánticos de sesión (`isAnonymous`, `getUser`).
 - Inyectar datos contextuales a la vista (usuario, hora actual).
===============================================================================
*/