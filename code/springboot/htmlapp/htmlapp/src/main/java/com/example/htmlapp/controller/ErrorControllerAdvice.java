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
 * Sustituye la “Whitelabel Error Page” de Spring Boot por vistas
 * personalizadas coherentes con el diseño HtmlApp.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @ControllerAdvice
 * ----------------------------------------------------------------------------
 * Aplica estas reglas globalmente a todos los controladores.
 * Permite capturar excepciones y dirigir a la plantilla adecuada
 * sin necesidad de duplicar código de manejo de errores.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @Slf4j
 * ----------------------------------------------------------------------------
 * Lombok genera automáticamente un logger `log` para registrar
 * eventos relevantes del ciclo de vida de la aplicación.
 * 
 * Niveles más usados:
 *  - log.warn()  → Errores esperables (403, 404, 400).
 *  - log.error() → Fallos críticos o imprevistos (500).
 *  - log.debug() → StackTrace completo (solo en desarrollo).
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
		model.addAttribute("errorMessage", "La página solicitada no existe o fue movida.");
		return "html/error/404";
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
		model.addAttribute("errorMessage",
			ex.getMessage() != null ? ex.getMessage() : "No tiene permisos para acceder a esta página.");
		return "html/error/403";
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
		model.addAttribute("errorMessage",
			ex.getMessage() != null ? ex.getMessage() : "La solicitud contiene datos no válidos.");
		return "html/error/400";
	}

	// -------------------------------------------------------------------------
	// 500 - Error en operación de negocio
	// -------------------------------------------------------------------------
	/**
	 * Maneja excepciones de tipo OperationFailedException.
	 *
	 * Esta excepción encapsula errores de negocio o validación,
	 * proporcionando un código de estado y un mensaje personalizados.
	 */
	@ExceptionHandler(OperationFailedException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleOperationFailed(OperationFailedException ex, Model model) {
		int code = (ex.getStatusCode() != 0) ? ex.getStatusCode() : 500;

		log.error("Error en operación de negocio (código {}): {}", code, ex.getMessage());
		log.debug("StackTrace:", ex);

		model.addAttribute("errorCode", code);
		model.addAttribute("errorMessage",
			ex.getMessage() != null ? ex.getMessage() : "No se pudo completar la operación solicitada.");

		// Usa la plantilla de error funcional
		return "html/error/operation-error";
	}

	// -------------------------------------------------------------------------
	// 500 - Error genérico no controlado
	// -------------------------------------------------------------------------
	/**
	 * Captura cualquier otra excepción no prevista.
	 * Es el último “catch-all” de la aplicación.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleGenericException(Exception ex, Model model) {
		log.error("Error interno del servidor: {}", ex.getMessage());
		log.debug("StackTrace:", ex);

		model.addAttribute("errorCode", 500);
		model.addAttribute("errorMessage",
			ex.getMessage() != null ? ex.getMessage() : "Ha ocurrido un error inesperado en el servidor.");

		return "html/error/generic-error";
	}
}

/*
 * ----------------------------------------------------------------------------
 * NOTAS ADICIONALES
 * ----------------------------------------------------------------------------
 * 1. Todas las vistas de error están en:
 *    src/main/resources/templates/html/error/
 *
 * 2. Excepciones esperadas:
 *    - SecurityException          → acceso denegado (403)
 *    - IllegalArgumentException   → datos incorrectos (400)
 *    - OperationFailedException   → fallo de negocio (400–500)
 *    - NoHandlerFoundException    → recurso inexistente (404)
 *    - Exception                  → genérica (500)
 *
 * 3. Registro de errores:
 *    - WARN  → errores previstos (403, 404, 400).
 *    - ERROR → errores inesperados o críticos.
 *    - DEBUG → stackTrace completo para depuración.
 *
 * 4. Compatibilidad con las plantillas modernas:
 *    Usa `layout-error.html`, que recibe `errorCode` y `errorMessage`.
 *    Las plantillas `operation-error.html` y `generic-error.html`
 *    aplican valores por defecto si el modelo no los incluye.
 * ----------------------------------------------------------------------------
 */