// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.enums.BulkActionType;
import com.example.htmlapp.model.enums.SortDirection;
import com.example.htmlapp.model.enums.UserOrderField;
import com.example.htmlapp.model.logic.AuthService;
import com.example.htmlapp.model.logic.UserListService;
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import lombok.RequiredArgsConstructor;

/**
 * Controlador encargado de la administración y gestión de listas de usuarios.
 *
 * Permite listar, ordenar y realizar operaciones masivas (otorgar o revocar
 * privilegios de administrador, o eliminar usuarios).
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ESTRUCTURA DE RUTAS
 * ----------------------------------------------------------------------------
 * Este controlador responde bajo el prefijo común /userlist:
 *
 * - GET  /userlist/list          → listado de usuarios
 * - POST /userlist/bulk-confirm  → pantalla de confirmación de acción masiva
 * - POST /userlist/bulk-success  → ejecución final de la acción
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA SEPARACIÓN DE RESPONSABILIDADES
 * ----------------------------------------------------------------------------
 * - UserListService contiene la lógica de negocio y seguridad.
 * - Este controlador solo gestiona el flujo de vistas y parámetros.
 */
@Controller
@RequestMapping("/userlist")
@RequiredArgsConstructor
public class UserListController {

	private final UserListService userListService;
	private final AuthService authService;

	// -------------------------------------------------------------------------
	// LISTADO DE USUARIOS
	// -------------------------------------------------------------------------

	/**
	 * Muestra la página de listado de usuarios.
	 *
	 * Acepta parámetros opcionales de ordenación y dirección.
	 * Si no se indican, se aplican los valores por defecto:
	 * orden por fecha de creación descendente.
	 *
	 * @param orderBy   Campo por el que ordenar (EMAIL, FULL_NAME...).
	 * @param direction Dirección del orden (ASC o DESC).
	 * @param model     Modelo de datos para la vista Thymeleaf.
	 * @return Plantilla de listado (list.html).
	 */
	@GetMapping("/list")
	public String listUsers(
		@RequestParam(name = "orderBy", required = false) UserOrderField orderBy,
		@RequestParam(name = "direction", required = false) SortDirection direction,
		Model model
	) {
		// Verificación explícita de sesión (autenticación)
		if (!authService.isLogged()) {
			throw new SecurityException("Debe iniciar sesión para acceder a esta página.");
		}

		try {
			List<User> users = userListService.listAllUsers(
				orderBy != null ? orderBy : UserOrderField.CREATION_DATETIME,
				direction != null ? direction : SortDirection.DESC
			);

			model.addAttribute("users", users);
			model.addAttribute("orderBy",
				orderBy != null ? orderBy : UserOrderField.CREATION_DATETIME);
			model.addAttribute("direction",
				direction != null ? direction : SortDirection.DESC);

			return "html/userlist/list";

		} catch (SecurityException ex) {
			throw new OperationFailedException(
				"No tiene permisos para acceder a esta página.", 403, ex);
		}
	}

	// -------------------------------------------------------------------------
	// CONFIRMACIÓN DE ACCIÓN MASIVA
	// -------------------------------------------------------------------------

	/**
	 * Muestra una vista de confirmación antes de ejecutar una operación masiva.
	 *
	 * Se usa cuando el administrador selecciona varios usuarios y escoge una
	 * acción (otorgar, revocar o eliminar).
	 *
	 * @param action Tipo de acción (GRANT, REVOKE, DELETE).
	 * @param ids    Lista de IDs de usuarios seleccionados.
	 * @param model  Modelo para la vista de confirmación.
	 * @return Plantilla de confirmación (bulk-confirm.html).
	 */
	@PostMapping("/bulk-confirm")
	public String confirmBulkAction(
		@RequestParam("action") BulkActionType action,
		@RequestParam("ids") List<Integer> ids,
		Model model
	) {
		if (!authService.isLogged()) {
			throw new SecurityException("Debe iniciar sesión para acceder a esta página.");
		}

		try {
			List<User> users = userListService.listFilteredUsers(ids);
			String idsString = String.join(",", ids.stream()
				.map(String::valueOf)
				.toList());

			model.addAttribute("action", action);
			model.addAttribute("users", users);
			model.addAttribute("count", users.size());
			model.addAttribute("idsString", idsString);

			return "html/userlist/bulk-confirm";

		} catch (IllegalArgumentException ex) {
			throw new OperationFailedException("Error en la selección de usuarios.", 400, ex);
		} catch (SecurityException ex) {
			throw new OperationFailedException("No tiene permisos para realizar esta acción.", 403, ex);
		}
	}

	// -------------------------------------------------------------------------
	// EJECUCIÓN DE ACCIÓN MASIVA
	// -------------------------------------------------------------------------

	/**
	 * Ejecuta la operación masiva (otorgar, revocar o borrar) y muestra resultado.
	 *
	 * @param action Tipo de acción.
	 * @param ids    Lista de IDs a modificar.
	 * @param model  Modelo de la vista.
	 * @return Plantilla de éxito (bulk-success.html).
	 */
	@PostMapping("/bulk-success")
	public String processBulkAction(
		@RequestParam("action") BulkActionType action,
		@RequestParam("ids") List<Integer> ids,
		Model model
	) {
		if (!authService.isLogged()) {
			throw new SecurityException("Debe iniciar sesión para acceder a esta página.");
		}

		try {
			switch (action) {
				case GRANT -> userListService.setAdminStatusBulk(ids, true);
				case REVOKE -> userListService.setAdminStatusBulk(ids, false);
				case DELETE -> userListService.deleteUsersBulk(ids);
				default -> throw new IllegalArgumentException("Acción no válida.");
			}

			model.addAttribute("action", action);
			return "html/userlist/bulk-success";

		} catch (IllegalArgumentException ex) {
			throw new OperationFailedException("Error al procesar la operación.", 400, ex);
		} catch (SecurityException ex) {
			throw new OperationFailedException("No tiene permisos para realizar esta acción.", 403, ex);
		} catch (Exception ex) {
			throw new OperationFailedException("Error inesperado al ejecutar la operación.", 500, ex);
		}
	}
}

/*
===============================================================================
CONTROL EXPLÍCITO DE ACCESO
===============================================================================
Este controlador incluye comprobaciones explícitas de autenticación mediante
`authService.isLogged()`, antes de mostrar o modificar listas de usuarios.

Esto permite distinguir entre:
 - Autenticación: comprobar que existe sesión activa.
 - Autorización: validar permisos concretos (en UserListService).

===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. CONVERSIÓN AUTOMÁTICA DE ENUMS
---------------------------------
Spring convierte automáticamente los parámetros String de la URL o formulario
en valores de enumerados (UserOrderField, SortDirection, BulkActionType),
siempre que los nombres coincidan.

Si el valor no coincide con ningún miembro del enum, Spring lanza una
MethodArgumentTypeMismatchException antes de ejecutar el método, que es
manejada por el controlador de errores global (ErrorControllerAdvice).

2. FLUJO DE ACCIONES MASIVAS
-----------------------------
- /list muestra el listado principal.
- /bulk-confirm pide confirmación al administrador antes de actuar.
- /bulk-success ejecuta la acción y muestra una confirmación.

3. EXCEPCIONES
---------------
Se usa OperationFailedException para centralizar el manejo de errores:
 - 400 → errores de validación o selección incorrecta.
 - 403 → falta de permisos.
 - 500 → errores internos o inesperados.

4. OBJETIVO PEDAGÓGICO
------------------------
Este controlador enseña cómo:
 - Estructurar rutas jerárquicas coherentes.
 - Usar enumerados en formularios y parámetros.
 - Separar vistas de confirmación y ejecución real.
 - Controlar autenticación y autorización de manera clara y centralizada.
===============================================================================
*/