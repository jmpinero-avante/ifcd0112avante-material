// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic.exceptions;

/**
 * Excepción personalizada para representar errores de operación
 * dentro de la lógica de negocio o en los controladores.
 *
 * Esta clase permite encapsular tanto el mensaje de error como
 * un código de estado HTTP asociado, facilitando su gestión por
 * el controlador global de excepciones (ErrorControllerAdvice).
 *
 * ----------------------------------------------------------------------------
 * USO TÍPICO:
 * ----------------------------------------------------------------------------
 * throw new OperationFailedException("Usuario no encontrado.", 404);
 *
 * El manejador global (ErrorControllerAdvice) puede entonces acceder
 * al código con getStatusCode() para devolver la respuesta adecuada.
 */
public class OperationFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** Código de estado HTTP asociado al error (por defecto, 500). */
	private final int statusCode;

	/**
	 * Constructor básico: crea una excepción con mensaje y código 500.
	 *
	 * @param message Descripción del error.
	 */
	public OperationFailedException(String message) {
		super(message);
		this.statusCode = 500;
	}

	/**
	 * Constructor extendido con mensaje y causa, usando código 500 por defecto.
	 *
	 * @param message Descripción del error.
	 * @param cause   Excepción original que provocó el fallo.
	 */
	public OperationFailedException(String message, Throwable cause) {
		super(message, cause);
		this.statusCode = 500;
	}

	/**
	 * Constructor extendido con mensaje y código HTTP.
	 *
	 * @param message    Descripción del error.
	 * @param statusCode Código de estado HTTP (400, 403, 404, 500...).
	 */
	public OperationFailedException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * Constructor completo: mensaje, código y causa.
	 *
	 * @param message    Descripción del error.
	 * @param statusCode Código de estado HTTP.
	 * @param cause      Excepción original que provocó el fallo.
	 */
	public OperationFailedException(String message, int statusCode, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	/**
	 * Devuelve el código de estado HTTP asociado a esta excepción.
	 *
	 * @return Código de estado (por defecto, 500 si no se especificó).
	 */
	public int getStatusCode() {
		return statusCode;
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. OBJETIVO
-----------
Esta excepción unifica la gestión de errores controlados en los controladores
y servicios, permitiendo incluir un código de estado HTTP además del mensaje.

2. MANEJO CENTRALIZADO
----------------------
El manejador global (ErrorControllerAdvice) puede capturar esta excepción y:
 - Mostrar una plantilla HTML personalizada según el código.
 - Registrar logs con nivel WARNING o ERROR.
 - Devolver una respuesta HTTP con el código correspondiente.

3. BUENAS PRÁCTICAS
--------------------
- Se utiliza como RuntimeException (unchecked) para no forzar `throws`.
- Se define un valor por defecto (500) para fallos inesperados.
- Incluye un `serialVersionUID` para compatibilidad en entornos distribuidos.

4. EJEMPLOS DE USO
------------------
 throw new OperationFailedException("Acceso denegado.", 403);
 throw new OperationFailedException("Datos inválidos.", 400, ex);
 throw new OperationFailedException("Error interno.", 500, ex);
 throw new OperationFailedException("Error genérico.", ex);
===============================================================================
*/