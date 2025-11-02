// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic.exceptions;

/**
 * Excepción personalizada para operaciones fallidas dentro de la lógica
 * de negocio o de controladores.
 *
 * Se utiliza para capturar errores de alto nivel (operaciones que no se
 * pudieron completar correctamente) y mostrarlos mediante el manejador
 * global de errores (ErrorControllerAdvice).
 *
 * ----------------------------------------------------------------------------
 * USO TÍPICO
 * ----------------------------------------------------------------------------
 * throw new OperationFailedException("No se pudo actualizar el usuario.");
 * throw new OperationFailedException("Error de validación.", 400);
 * throw new OperationFailedException("Error en base de datos.", 500, ex);
 */
public class OperationFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int errorCode;

	// Constructor básico con mensaje (usa código 500 por defecto)
	public OperationFailedException(String message) {
		super(message);
		this.errorCode = 500;
	}

	// Constructor con mensaje y código personalizado
	public OperationFailedException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	// NUEVO: Constructor con mensaje y causa (usa código 500 por defecto)
	public OperationFailedException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = 500;
	}

	// NUEVO: Constructor con mensaje, código y causa
	public OperationFailedException(String message, int errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}