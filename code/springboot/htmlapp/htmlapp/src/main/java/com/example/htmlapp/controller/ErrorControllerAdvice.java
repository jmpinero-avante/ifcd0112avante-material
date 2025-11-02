// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import lombok.extern.slf4j.Slf4j;

/**
 * Controlador global de errores.
 *
 * Centraliza la gestión de excepciones en toda la aplicación.
 * Sustituye la página “Whitelabel Error Page” de Spring Boot
 * por nuestras propias vistas de error personalizadas.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @ControllerAdvice
 * ----------------------------------------------------------------------------
 * Indica que esta clase intercepta excepciones lanzadas desde cualquier
 * controlador (@Controller) y permite dirigir al usuario a una página
 * HTML de error coherente con el diseño general de la aplicación.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @Slf4j
 * ----------------------------------------------------------------------------
 * Lombok genera un logger estático llamado `log` (de tipo org.slf4j.Logger)
 * que permite registrar mensajes de diagnóstico en distintos niveles:
 *
 *   log.info(...)   → información general.
 *   log.warn(...)   → advertencias o errores esperables (403, 404...).
 *   log.error(...)  → errores graves del servidor.
 *   log.debug(...)  → detalles técnicos y stackTrace (solo en desarrollo).
 *
 * ----------------------------------------------------------------------------
 * FILOSOFÍA DE DISEÑO
 * ----------------------------------------------------------------------------
 * - El usuario ve solo mensajes comprensibles y visualmente consistentes.
 * - Los detalles técnicos quedan registrados en los logs.
 * - No se expone información sensible en producción.
 */

@Slf4j
@ControllerAdvice
public class ErrorControllerAdvice {

	// -------------------------------------------------------------------------
	// 404 - Página no encontrada
	// -------------------------------------------------------------------------
	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNotFound(NoHandlerFoundException ex, Model model) {
		log.warn("Recurso no encontrado: {}", ex.getRequestURL());
		log.debug("StackTrace:", ex);

		model.addAttribute("errorCode", 404);
		model.addAttribute("title", "Página no encontrada");
		return "error/404";
	}

	// -------------------------------------------------------------------------
	// 403 - Acceso denegado
	// -------------------------------------------------------------------------
	@ExceptionHandler(SecurityException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handleForbidden(SecurityException ex, Model model) {
		log.warn("Acceso denegado: {}", ex.getMessage());
		log.debug("StackTrace:", ex);

		model.addAttribute("errorCode", 403);
		model.addAttribute("title", "Acceso denegado");
		return "error/403";
	}

	// -------------------------------------------------------------------------
	// 400 - Solicitud incorrecta
	// -------------------------------------------------------------------------
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleBadRequest(IllegalArgumentException ex, Model model) {
		log.warn("Solicitud incorrecta: {}", ex.getMessage());
		log.debug("StackTrace:", ex);

		model.addAttribute("errorCode", 400);
		model.addAttribute("title", "Solicitud incorrecta");
		return "error/400";
	}

	// -------------------------------------------------------------------------
	// 500 - Error en operación de negocio
	// -------------------------------------------------------------------------
	/**
	 * Maneja las excepciones de tipo OperationFailedException,
	 * usadas para errores funcionales o de negocio.
	 *
	 * Este método utiliza el código HTTP 500, pero además puede
	 * mostrar un código numérico alternativo definido en la propia
	 * excepción (por ejemplo, 409 o 422).
	 *
	 * @param ex Excepción personalizada de operación fallida.
	 * @param model Modelo de Thymeleaf.
	 * @return Vista "error/operation-error".
	 */
	@ExceptionHandler(OperationFailedException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleOperationFailed(OperationFailedException ex, Model model) {
		int code = ex.getStatusCode() != 0 ? ex.getStatusCode() : 500;

		log.error("Error en operación de negocio (código {}): {}", code, ex.getMessage());
		log.debug("StackTrace:", ex);

		model.addAttribute("errorCode", code);
		model.addAttribute("errorMessage", ex.getMessage());
		model.addAttribute("title", "Error en la operación");
		return "error/operation-error";
	}

	// -------------------------------------------------------------------------
	// 500 - Error genérico no controlado
	// -------------------------------------------------------------------------
	/**
	 * Maneja cualquier otra excepción no prevista.
	 * Es el último “catch-all” de la aplicación.
	 *
	 * @param ex Excepción no controlada.
	 * @param model Modelo para la vista.
	 * @return Vista "error/generic-error".
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleGenericException(Exception ex, Model model) {
		log.error("Error interno del servidor: {}", ex.getMessage());
		log.debug("StackTrace:", ex);

		model.addAttribute("errorCode", 500);
		model.addAttribute("errorMessage",
			"Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.");
		model.addAttribute("title", "Error interno del servidor");
		return "error/generic-error";
	}
}

/*
 * ----------------------------------------------------------------------------
 * NOTAS ADICIONALES
 * ----------------------------------------------------------------------------
 * 1. Todas las vistas de error se encuentran en:
 *    src/main/resources/templates/error/
 *
 * 2. Los controladores y servicios pueden lanzar excepciones:
 *      - SecurityException          → acceso denegado (403)
 *      - IllegalArgumentException   → datos incorrectos (400)
 *      - OperationFailedException   → error funcional (500 u otro)
 *      - Exception (genérico)       → error imprevisto (500)
 *
 * 3. Los logs se clasifican en niveles:
 *      - WARN → Errores esperables del usuario.
 *      - ERROR → Fallos graves en la lógica o servidor.
 *      - DEBUG → Detalles de depuración (stackTrace).
 */