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
 * Controlador global de manejo de errores.
 *
 * Captura excepciones y redirige a las plantillas de error personalizadas
 * situadas en /templates/error/.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @ControllerAdvice
 * ----------------------------------------------------------------------------
 * Esta anotación permite definir un componente que actúa como un "observador"
 * global de excepciones lanzadas en cualquier controlador del proyecto.
 *
 * Cualquier excepción no capturada de manera específica será interceptada aquí,
 * lo que permite devolver una página de error coherente con el diseño general
 * de la aplicación.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @ExceptionHandler
 * ----------------------------------------------------------------------------
 * Cada método dentro de esta clase está anotado con @ExceptionHandler(Tipo.class),
 * lo que indica que manejará las excepciones del tipo especificado (o sus subclases).
 *
 * Spring busca el método más específico disponible:
 *   - Si ocurre una SecurityException → ejecuta handleForbidden().
 *   - Si ocurre un NoHandlerFoundException → ejecuta handleNotFound().
 *   - Si ocurre cualquier otra excepción → ejecuta handleGenericError().
 *
 * Esta jerarquía permite controlar de forma muy precisa qué vista se muestra
 * al usuario en función del tipo de error producido.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @Slf4j
 * ----------------------------------------------------------------------------
 * La anotación @Slf4j (Simple Logging Facade for Java) de Lombok genera
 * automáticamente un **atributo estático y final** llamado `log` en la clase,
 * de tipo org.slf4j.Logger.
 *
 * Es equivalente a escribir manualmente:
 *
 *     private static final org.slf4j.Logger log =
 *         org.slf4j.LoggerFactory.getLogger(ErrorControllerAdvice.class);
 *
 * Esto permite usar directamente métodos como:
 *     log.info("Inicio de la aplicación");
 *     log.warn("Acceso no autorizado");
 *     log.error("Fallo crítico en el sistema", ex);
 *     log.debug("Detalles para depuración");
 *
 * ----------------------------------------------------------------------------
 * NIVELES DE LOG
 * ----------------------------------------------------------------------------
 * Los niveles más comunes de SLF4J son:
 *   - trace → información extremadamente detallada (nivel más bajo)
 *   - debug → información útil para desarrolladores durante la depuración
 *   - info  → mensajes informativos de funcionamiento normal
 *   - warn  → advertencias o situaciones anómalas pero no críticas
 *   - error → errores graves o excepciones que afectan al funcionamiento
 *
 * ----------------------------------------------------------------------------
 * DIFERENCIA ENTRE LOGGING Y CONSOLE OUTPUT
 * ----------------------------------------------------------------------------
 * Usar loggers (como SLF4J) es preferible a System.out.println()
 * porque:
 *   - Permite configurar la salida de logs (archivo, consola, syslog...).
 *   - Controla el nivel de detalle mediante configuración externa (application.yml).
 *   - Facilita auditoría, depuración y mantenimiento.
 *
 * En Spring Boot, el logging está gestionado internamente por Logback,
 * pero el uso de @Slf4j garantiza independencia del framework concreto.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL REGISTRO DE LOGS EN ESTA CLASE
 * ----------------------------------------------------------------------------
 * En este controlador, los logs se usan así:
 *   - log.warn() → para errores del usuario (403, 404, 400)
 *   - log.error() → para fallos del sistema (500)
 *   - log.debug() → para registrar el stack trace completo (solo visible
 *     si el nivel de logging está configurado en DEBUG)
 *
 * Esto permite depurar los errores sin exponer información sensible al usuario.
 */
@Slf4j
@ControllerAdvice
public class ErrorControllerAdvice {

	/**
	 * Maneja errores de acceso no autorizado o permisos insuficientes (403).
	 */
	@ExceptionHandler(SecurityException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handleForbidden(SecurityException ex, Model model) {
		log.warn("Acceso no autorizado: {}", ex.getMessage());
		log.debug("Detalles de la excepción:", ex);
		model.addAttribute("errorMessage", ex.getMessage());
		return "error/403";
	}

	/**
	 * Maneja errores de recurso no encontrado (404).
	 *
	 * Esta excepción suele lanzarse cuando no existe un controlador o ruta
	 * que coincida con la URL solicitada por el usuario.
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNotFound(NoHandlerFoundException ex, Model model) {
		log.warn("Página no encontrada: {}", ex.getRequestURL());
		log.debug("Detalles de la excepción:", ex);
		model.addAttribute("errorMessage", "La página solicitada no existe.");
		return "error/404";
	}

	/**
	 * Maneja errores de solicitud incorrecta (400).
	 *
	 * Este tipo de error se usa cuando la petición contiene parámetros
	 * inválidos, formatos erróneos o datos no procesables.
	 *
	 * Puede ocurrir, por ejemplo, si el usuario manipula manualmente
	 * la URL o si se envían datos incompletos en un formulario.
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleBadRequest(IllegalArgumentException ex, Model model) {
		log.warn("Petición incorrecta: {}", ex.getMessage());
		log.debug("Detalles de la excepción:", ex);
		model.addAttribute("errorMessage", ex.getMessage());
		return "error/400";
	}

	/**
	 * Maneja errores de operación fallida (por ejemplo, al actualizar
	 * o eliminar datos en base de datos).
	 *
	 * Este caso puede incluir fallos de integridad, restricciones UNIQUE,
	 * o cualquier otro problema de negocio que impida completar la acción.
	 *
	 * Se lanza explícitamente desde los servicios mediante la clase
	 * OperationFailedException (definida en model.logic.exceptions).
	 */
	@ExceptionHandler(OperationFailedException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public String handleOperationError(OperationFailedException ex, Model model) {
		log.warn("Error en la operación: {}", ex.getMessage());
		log.debug("Detalles de la excepción:", ex);
		model.addAttribute("errorMessage", ex.getMessage());
		return "error/operation-error";
	}

	/**
	 * Captura cualquier otra excepción no manejada explícitamente
	 * y devuelve una página de error genérica (500 o "generic-error").
	 *
	 * Esto garantiza que el usuario no vea un stack trace en el navegador,
	 * y que toda la experiencia de error sea coherente y controlada.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleGenericError(Exception ex, Model model) {
		log.error("Error interno en la aplicación: {}", ex.getMessage());
		log.debug("Stack trace completo:", ex);
		model.addAttribute("errorMessage",
			"Se ha producido un error interno. Por favor, inténtelo más tarde.");
		return "error/generic-error";
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE EL MANEJO DE EXCEPCIONES PERSONALIZADAS
 * ----------------------------------------------------------------------------
 *
 * La clase OperationFailedException se encuentra en:
 *   com.example.htmlapp.model.logic.exceptions.OperationFailedException
 *
 * Permite lanzar errores específicos desde la capa de servicios cuando
 * ocurre un fallo lógico o de base de datos, y mostrar al usuario una
 * página adaptada como "operation-error.html".
 *
 * ----------------------------------------------------------------------------
 * SOBRE LAS PÁGINAS DE ERROR
 * ----------------------------------------------------------------------------
 *
 * error/400.html     → Peticiones inválidas o incompletas.
 * error/403.html     → Falta de permisos.
 * error/404.html     → Recurso no encontrado.
 * error/500.html     → Error interno o inesperado.
 * error/operation-error.html → Fallos en operaciones específicas.
 * error/generic-error.html   → Fallback general para cualquier otro caso.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LAS BUENAS PRÁCTICAS
 * ----------------------------------------------------------------------------
 *
 * - No se muestra nunca el stack trace ni detalles técnicos al usuario final.
 * - Todos los mensajes visibles son redactados en lenguaje natural.
 * - Los logs guardan la información completa para diagnóstico posterior.
 * - Se usa una estructura coherente y pedagógica para enseñar buenas prácticas
 *   en el manejo de errores en aplicaciones Spring Boot.
 */