// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic.exceptions;

/**
 * Excepción personalizada para indicar que una operación
 * de negocio ha fallado.
 *
 * Se utiliza en lugar de RuntimeException genérica cuando
 * se quiere informar de un error funcional (no técnico)
 * al usuario o a la capa de presentación.
 *
 * ----------------------------------------------------------------------------
 * EJEMPLOS DE USO
 * ----------------------------------------------------------------------------
 *   throw new OperationFailedException("No se pudo enviar el correo");
 *
 *   throw new OperationFailedException(
 *       "El saldo es insuficiente para realizar la compra.", 409);
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL STATUS CODE
 * ----------------------------------------------------------------------------
 * Aunque esta excepción usa HTTP 500 por defecto, puede incluir un
 * código numérico alternativo (409, 422, etc.) para representar
 * errores específicos de negocio. Esto permite mostrar diferentes
 * códigos en la interfaz sin cambiar el comportamiento HTTP.
 */
public class OperationFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int statusCode;

	/**
	 * Constructor con solo mensaje.
	 *
	 * @param message Mensaje descriptivo del error.
	 */
	public OperationFailedException(String message) {
		super(message);
		this.statusCode = 0; // Valor por defecto (se tratará como 500)
	}

	/**
	 * Constructor con mensaje y código de estado.
	 *
	 * @param message    Descripción del error.
	 * @param statusCode Código HTTP o semántico asociado.
	 */
	public OperationFailedException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * Devuelve el código de estado asociado al error.
	 *
	 * Si el valor es 0, el ErrorControllerAdvice mostrará 500.
	 *
	 * @return Código numérico del error.
	 */
	public int getStatusCode() {
		return statusCode;
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE SU USO EN SPRING
 * ----------------------------------------------------------------------------
 * Las excepciones de este tipo son capturadas por el controlador
 * global de errores (ErrorControllerAdvice), que mostrará la
 * plantilla error/operation-error.html.
 *
 * En producción, el usuario solo verá un mensaje amigable.
 * En los logs, sin embargo, se registrará el error completo.
 */