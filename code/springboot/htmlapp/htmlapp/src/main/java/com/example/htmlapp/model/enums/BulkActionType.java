// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.enums;

/**
 * Enumerado que define los tipos de operaciones masivas
 * que se pueden realizar sobre varios usuarios a la vez.
 *
 * Estas acciones se reciben desde los formularios de la vista
 * de administración (userlist/list.html) y se procesan en el
 * controlador UserListController.
 *
 * ----------------------------------------------------------------------------
 * OPCIONES DISPONIBLES
 * ----------------------------------------------------------------------------
 * - GRANT  → Otorgar privilegios de administrador.
 * - REVOKE → Revocar privilegios de administrador.
 * - DELETE → Eliminar los usuarios seleccionados.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA CONVERSIÓN AUTOMÁTICA DE ENUMS
 * ----------------------------------------------------------------------------
 * Spring MVC convierte automáticamente los parámetros de texto
 * recibidos en peticiones (GET o POST) a sus valores de enum
 * correspondientes, siempre que los nombres coincidan.
 *
 * Ejemplo:
 *   POST /admin/users/bulk?action=GRANT
 *
 * En este caso, el parámetro "action" se convierte en
 * BulkActionType.GRANT sin necesidad de código adicional.
 *
 * Si el valor recibido no coincide con ningún literal del enum,
 * Spring lanza una excepción de tipo
 * MethodArgumentTypeMismatchException, que es gestionada
 * globalmente por el controlador de errores (ErrorControllerAdvice)
 * para mostrar una página 400 Bad Request.
 */
public enum BulkActionType {

	/** Otorga privilegios de administrador a los usuarios seleccionados. */
	GRANT,

	/** Revoca los privilegios de administrador. */
	REVOKE,

	/** Elimina definitivamente los usuarios seleccionados. */
	DELETE
}

/*
 * ----------------------------------------------------------------------------
 * NOTA PEDAGÓGICA
 * ----------------------------------------------------------------------------
 * Este enumerado permite sustituir cadenas literales ("grant", "revoke",
 * "delete") por valores tipados, mejorando la legibilidad y reduciendo
 * errores en tiempo de ejecución.
 *
 * Usar enums en lugar de strings facilita:
 *  - Validación automática de parámetros en Spring.
 *  - Autocompletado en IDEs.
 *  - Mantenimiento más seguro y coherente.
 *
 * ----------------------------------------------------------------------------
 * USO EN FORMULARIOS HTML (EJEMPLO)
 * ----------------------------------------------------------------------------
 * <form th:action="@{/admin/users/bulk}" method="post">
 *   <input type="hidden" name="ids" th:value="${user.id}" />
 *
 *   <button type="submit" name="action" value="GRANT">Dar admin</button>
 *   <button type="submit" name="action" value="REVOKE">Quitar admin</button>
 *   <button type="submit" name="action" value="DELETE">Eliminar</button>
 * </form>
 *
 * Cuando el formulario se envía, Spring convierte automáticamente
 * el parámetro "action" a la constante BulkActionType correspondiente.
 */