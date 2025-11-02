// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.enums;

/**
 * Enumerado que define las acciones masivas disponibles
 * en la administración de usuarios.
 *
 * Sustituye las cadenas literales ("grant", "revoke", "delete")
 * para hacer el código más seguro, claro y mantenible.
 *
 * Spring convierte automáticamente los valores de la URL o
 * formularios en instancias de este enum, por ejemplo:
 *
 *   action=GRANT_ADMIN   → BulkActionType.GRANT_ADMIN
 *   action=grant_admin   → BulkActionType.GRANT_ADMIN
 *
 * Si el valor no coincide con ningún miembro, Spring lanza una
 * MethodArgumentTypeMismatchException antes de ejecutar el método,
 * que será gestionada por ErrorControllerAdvice (error 400).
 */
public enum BulkActionType {
	GRANT_ADMIN,
	REVOKE_ADMIN,
	DELETE_USERS
}