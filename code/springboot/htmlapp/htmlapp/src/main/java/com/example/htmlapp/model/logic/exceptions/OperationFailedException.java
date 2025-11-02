// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic.exceptions;

import java.io.Serial;

/**
 * Excepción de "operación fallida" de la lógica de negocio.
 *
 * Se utiliza cuando una operación solicitada por el usuario no puede
 * completarse por razones de negocio o de integridad (por ejemplo,
 * violaciones de UNIQUE, estados no válidos, precondiciones incumplidas,
 * etc.).
 *
 * ----------------------------------------------------------------------------
 * CUÁNDO LANZARLA
 * ----------------------------------------------------------------------------
 * - Al intentar actualizar/eliminar un recurso que no cumple las reglas
 *   de negocio.
 * - Cuando la base de datos rechaza la operación por restricciones
 *   lógicas (p. ej., UNIQUE) y queremos elevar un mensaje comprensible.
 * - Para comunicar fallos "esperables" de la operación (no bugs).
 *
 * ----------------------------------------------------------------------------
 * MANEJO CENTRALIZADO
 * ----------------------------------------------------------------------------
 * Esta excepción es capturada por ErrorControllerAdvice:
 *
 *   @ExceptionHandler(OperationFailedException.class)
 *   @ResponseStatus(HttpStatus.CONFLICT)
 *   public String handleOperationError(...)
 *
 * De este modo, se muestra la plantilla:
 *   templates/error/operation-error.html
 *
 * Nota: No se anota esta clase con @ResponseStatus para mantener la
 * gestión de códigos HTTP y plantillas de error centralizada en el
 * ControllerAdvice (separación de responsabilidades y mayor control).
 */
public class OperationFailedException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	/** Constructor por defecto. */
	public OperationFailedException() {
		super("No se pudo completar la operación solicitada.");
	}

	/** Constructor con mensaje descriptivo. */
	public OperationFailedException(String message) {
		super(message);
	}

	/** Constructor con causa encadenada. */
	public OperationFailedException(Throwable cause) {
		super(cause);
	}

	/** Constructor con mensaje y causa encadenada. */
	public OperationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}