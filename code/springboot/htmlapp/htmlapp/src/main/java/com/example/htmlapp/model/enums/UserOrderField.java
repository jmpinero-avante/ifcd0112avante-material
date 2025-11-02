// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.enums;

/**
 * Enumeración que representa los campos por los que puede ordenarse
 * la lista de usuarios.
 *
 * Usar un enum en lugar de Strings evita errores tipográficos y mejora
 * la seguridad de tipos y la legibilidad del código.
 */
public enum UserOrderField {
	EMAIL,
	FULL_NAME,
	CREATION_DATETIME;
}