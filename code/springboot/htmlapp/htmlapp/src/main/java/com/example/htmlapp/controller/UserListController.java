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
import com.example.htmlapp.model.logic.PermissionsService;
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
 * - UserListService contiene solo la lógica de negocio (sin permisos).
 * - Este controlador gestiona la autenticación y la autorización.
 */
@Controller
@RequestMapping("/userlist")
@RequiredArgsConstructor
public class UserListController {

	private final UserListService userListService;
	private final AuthService authService;
	private final PermissionsService permissionsService;

	// -------------------------------------------------------------------------
	// LISTADO DE USUARIOS
	// -------------------------------------------------------------------------

	/**
	 * Muestra la página de listado de usuarios (solo para administradores).
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
		// Validar sesión y permisos de administrador
		permissionsService.checkAdminPermission();

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

		} catch (Exception ex) {
			throw new OperationFailedException("Error al cargar el listado de usuarios.", 500, ex);
		}
	}

	// -------------------------------------------------------------------------
	// CONFIRMACIÓN DE ACCIÓN MASIVA
	// -------------------------------------------------------------------------

	/**
	 * Muestra una vista de confirmación antes de ejecutar una operación masiva.
	 *
	 * Solo los administradores pueden acceder a esta vista.
	 * Excluye siempre al usuario logado de la lista de destino.
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
		permissionsService.checkAdminPermission();

		if (ids == null || ids.isEmpty()) {
			throw new OperationFailedException("Debe seleccionar al menos un usuario.", 400);
		}

		Integer currentUserId = authService.getUserId().orElse(null);
		List<User> users = userListService.listUsersByIds(ids).stream()
			.filter(u -> !u.getId().equals(currentUserId)) // excluye al logado
			.toList();

		if (users.isEmpty()) {
			throw new OperationFailedException("No hay usuarios válidos para procesar.", 400);
		}

		String idsString = String.join(",", users.stream()
			.map(u -> String.valueOf(u.getId()))
			.toList());

		model.addAttribute("action", action);
		model.addAttribute("users", users);
		model.addAttribute("count", users.size());
		model.addAttribute("idsString", idsString);

		return "html/userlist/bulk-confirm";
	}

	// -------------------------------------------------------------------------
	// EJECUCIÓN DE ACCIÓN MASIVA
	// -------------------------------------------------------------------------

	/**
	 * Ejecuta la operación masiva (otorgar, revocar o borrar) y muestra resultado.
	 *
	 * Solo accesible para administradores.
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
		permissionsService.checkAdminPermission();

		if (ids == null || ids.isEmpty()) {
			throw new OperationFailedException("Debe seleccionar al menos un usuario.", 400);
		}

		Integer currentUserId = authService.getUserId().orElse(null);
		List<Integer> validIds = ids.stream()
			.filter(id -> !id.equals(currentUserId)) // excluye el propio usuario
			.toList();

		if (validIds.isEmpty()) {
			throw new OperationFailedException("No hay usuarios válidos para procesar.", 400);
		}

		try {
			switch (action) {
				case GRANT -> userListService.setAdminStatusBulk(validIds, true);
				case REVOKE -> userListService.setAdminStatusBulk(validIds, false);
				case DELETE -> userListService.deleteUsersBulk(validIds);
				default -> throw new IllegalArgumentException("Acción no válida.");
			}

			model.addAttribute("action", action);
			model.addAttribute("count", validIds.size());
			return "html/userlist/bulk-success";

		} catch (IllegalArgumentException ex) {
			throw new OperationFailedException("Error al procesar la operación.", 400, ex);
		} catch (Exception ex) {
			throw new OperationFailedException("Error inesperado al ejecutar la operación.", 500, ex);
		}
	}
}

/*
===============================================================================
CONTROL DE ACCESO Y PERMISOS
===============================================================================
Este controlador valida explícitamente los permisos de administrador antes de
realizar cualquier operación, delegando la lógica de negocio al servicio.

 - `permissionsService.checkAdminPermission()` asegura que el usuario tenga rol admin.
 - El usuario logado se excluye de cualquier operación masiva (seguridad adicional).
 - Si no hay sesión activa o no es admin, se lanza una `SecurityException` (403).

===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. CONVERSIÓN AUTOMÁTICA DE ENUMS
---------------------------------
Spring convierte automáticamente parámetros String en enumerados (UserOrderField,
SortDirection, BulkActionType) siempre que los nombres coincidan.

2. FLUJO DE ACCIONES MASIVAS
-----------------------------
- /list muestra el listado principal.
- /bulk-confirm confirma antes de ejecutar.
- /bulk-success ejecuta y muestra resultado.

3. EXCEPCIONES Y MANEJO GLOBAL
-------------------------------
Este controlador lanza `OperationFailedException` con códigos específicos:
 - 400 → errores de validación.
 - 403 → falta de permisos.
 - 500 → errores internos.

4. OBJETIVO PEDAGÓGICO
------------------------
Ilustra cómo:
 - Delegar la seguridad al controlador y mantener los servicios puros.
 - Filtrar el usuario logado de las operaciones masivas.
 - Mantener coherencia y trazabilidad en el manejo de errores.
===============================================================================
*/