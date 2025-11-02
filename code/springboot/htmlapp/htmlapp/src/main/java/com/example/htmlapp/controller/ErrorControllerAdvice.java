// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Controlador global de errores y excepciones.
 *
 * Su objetivo es capturar las excepciones más comunes lanzadas por
 * los controladores y servicios de la aplicación, y redirigir a
 * páginas de error personalizadas con mensajes adecuados.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE @ControllerAdvice
 * ----------------------------------------------------------------------------
 * Indica a Spring que esta clase contiene manejadores globales de errores
 * aplicables a todos los controladores del proyecto.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @ExceptionHandler
 * ----------------------------------------------------------------------------
 * Permite asociar un tipo de excepción a un método que gestionará la respuesta
 * en caso de producirse ese error en cualquier parte del código MVC.
 */
@ControllerAdvice
public class ErrorControllerAdvice {

	private static final Logger log =
		LoggerFactory.getLogger(ErrorControllerAdvice.class);

	/**
	 * Maneja errores de tipo 404 (ruta no encontrada).
	 *
	 * Este error ocurre cuando el usuario solicita una URL que no existe
	 * y Spring lanza NoHandlerFoundException.
	 *
	 * @param ex Excepción capturada.
	 * @param model Modelo para pasar datos a la vista.
	 * @return Plantilla HTML personalizada para el error 404.
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	public String handleNotFound(NoHandlerFoundException ex, Model model) {
		log.warn("Error 404 - Recurso no encontrado: {}", ex.getRequestURL());
		log.debug("Detalles de la excepción 404", ex);
		model.addAttribute("errorTitle", "Página no encontrada");
		model.addAttribute("errorMessage",
			"La página solicitada no existe o ha sido movida.");
		model.addAttribute("errorCode", 404);
		return "error/404";
	}

	/**
	 * Maneja errores de tipo 403 (acceso denegado).
	 *
	 * Este error ocurre cuando el usuario intenta acceder a una
	 * sección restringida sin tener permisos suficientes.
	 *
	 * @param ex Excepción capturada.
	 * @param model Modelo para pasar datos a la vista.
	 * @return Plantilla HTML personalizada para el error 403.
	 */
	@ExceptionHandler(SecurityException.class)
	public String handleForbidden(SecurityException ex, Model model) {
		log.warn("Error 403 - Acceso denegado: {}", ex.getMessage());
		log.debug("Detalles de la excepción 403", ex);
		model.addAttribute("errorTitle", "Acceso denegado");
		model.addAttribute("errorMessage", ex.getMessage());
		model.addAttribute("errorCode", 403);
		return "error/403";
	}

	/**
	 * Maneja errores de tipo 400 (peticiones con parámetros inválidos).
	 *
	 * Este error se lanza, por ejemplo, cuando Spring no puede convertir
	 * un parámetro String de la URL en un valor de enumerado (enum),
	 * como ocurre en los controladores que usan @RequestParam con enums.
	 *
	 * @param ex Excepción capturada.
	 * @param model Modelo para pasar datos a la vista.
	 * @return Plantilla HTML personalizada para el error 400.
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public String handleBadRequest(MethodArgumentTypeMismatchException ex, Model model) {
		log.error("Error 400 - Parámetro inválido: {} (valor recibido: {})",
			ex.getName(), ex.getValue());
		log.debug("Detalles de la excepción 400", ex);
		model.addAttribute("errorTitle", "Parámetros inválidos");
		model.addAttribute("errorMessage",
			"Los parámetros de la URL no son válidos. " +
			"Por favor, revise los valores de ordenación o dirección.");
		model.addAttribute("errorCode", 400);
		return "error/400";
	}

	/**
	 * Maneja errores de violación de integridad en base de datos.
	 *
	 * Ejemplo: insertar un usuario con un email duplicado (columna UNIQUE).
	 *
	 * @param ex Excepción capturada.
	 * @param model Modelo para pasar datos a la vista.
	 * @return Plantilla HTML personalizada para el error de operación.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public String handleDataIntegrity(DataIntegrityViolationException ex, Model model) {
		log.error("Error 409 - Violación de integridad en base de datos: {}", ex.getMessage());
		log.debug("Detalles de la excepción 409", ex);
		model.addAttribute("errorTitle", "Error en la base de datos");
		model.addAttribute("errorMessage",
			"La operación no se pudo completar porque viola una restricción " +
			"de integridad (por ejemplo, un valor duplicado).");
		model.addAttribute("errorCode", 409);
		return "error/operation-error";
	}

	/**
	 * Maneja cualquier otro error no controlado.
	 *
	 * Este método actúa como "última defensa" para capturar excepciones
	 * que no hayan sido gestionadas en otro lugar.
	 *
	 * @param ex Excepción capturada.
	 * @param model Modelo para pasar datos a la vista.
	 * @return Plantilla HTML genérica de error.
	 */
	@ExceptionHandler(Exception.class)
	public String handleGenericException(Exception ex, Model model) {
		log.error("Error 500 - Excepción no controlada: {}", ex.getMessage());
		log.debug("Detalles de la excepción 500", ex);
		model.addAttribute("errorTitle", "Error inesperado");
		model.addAttribute("errorMessage",
			"Se ha producido un error no esperado. " +
			"Por favor, contacte con el administrador del sistema.");
		model.addAttribute("errorCode", 500);
		return "error/generic-error";
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE LA FILOSOFÍA DE SPRING PARA EL MANEJO DE ERRORES
 * ----------------------------------------------------------------------------
 * - Los controladores (Controller) se mantienen limpios y centrados
 *   en la lógica de presentación.
 *
 * - Las excepciones de conversión o seguridad son capturadas por
 *   este componente global, que muestra una vista coherente al usuario.
 *
 * - De esta forma, la capa de presentación no necesita código repetido
 *   para manejar errores, y la aplicación sigue un flujo uniforme.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL LOGGING
 * ----------------------------------------------------------------------------
 * Usamos SLF4J (Simple Logging Facade for Java) a través de la clase Logger.
 * Spring Boot configura automáticamente el sistema de logs (Logback) para
 * que los mensajes se muestren en consola y puedan redirigirse a ficheros.
 *
 * Los niveles usados son:
 *   - warn()  → para accesos denegados o recursos no encontrados.
 *   - error() → para errores graves o excepciones que interrumpen el flujo.
 *   - debug() → para registrar el stack trace completo y facilitar la depuración.
 *
 * El nivel debug no se muestra por defecto en producción, pero puede activarse
 * añadiendo en application.yml:
 *
 *   logging:
 *     level:
 *       root: debug
 *
 * De esta forma, se conserva la información de depuración sin exponerla
 * al usuario final, manteniendo la seguridad y la trazabilidad.
 *
 * ----------------------------------------------------------------------------
 * VISTAS DE ERROR RECOMENDADAS
 * ----------------------------------------------------------------------------
 * templates/error/
 *   ├── 400.html   → Parámetros inválidos (MethodArgumentTypeMismatchException)
 *   ├── 403.html   → Acceso denegado (SecurityException)
 *   ├── 404.html   → Página no encontrada (NoHandlerFoundException)
 *   ├── operation-error.html → Violación de integridad (DataIntegrityViolationException)
 *   └── generic-error.html   → Cualquier otro error (Exception)
 */