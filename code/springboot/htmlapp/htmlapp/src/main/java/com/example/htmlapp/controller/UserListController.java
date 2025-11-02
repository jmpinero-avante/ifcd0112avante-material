// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.enums.SortDirection;
import com.example.htmlapp.model.enums.UserOrderField;
import com.example.htmlapp.model.enums.BulkActionType;
import com.example.htmlapp.model.logic.UserListService;
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import lombok.RequiredArgsConstructor;

/**
 * Controlador encargado de la página de administración de usuarios.
 *
 * Gestiona el listado, la ordenación y las operaciones en bloque
 * (otorgar/quitar privilegios o borrar usuarios).
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE @Controller
 * ----------------------------------------------------------------------------
 * Indica que esta clase forma parte de la capa de presentación (MVC).
 * Retorna nombres de vistas (plantillas HTML Thymeleaf) en lugar de datos JSON.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA INYECCIÓN DE DEPENDENCIAS
 * ----------------------------------------------------------------------------
 * @RequiredArgsConstructor (de Lombok) genera un constructor con los campos
 * marcados como final, lo que permite inyectar automáticamente el servicio.
 */
@Controller
@RequiredArgsConstructor
public class UserListController {

	private final UserListService userListService;

	// -------------------------------------------------------------------------
	// LISTADO DE USUARIOS
	// -------------------------------------------------------------------------

	/**
	 * Muestra la página de listado de usuarios.
	 *
	 * Acepta parámetros opcionales de ordenación y dirección.
	 * Si no se indican, el listado se ordena por fecha de creación descendente.
	 *
	 * @param orderBy   Campo por el que ordenar (EMAIL, FULL_NAME...).
	 * @param direction Dirección del orden (ASC o DESC).
	 * @param model     Modelo de datos para la vista Thymeleaf.
	 * @return Nombre de la plantilla HTML a renderizar.
	 *
	 * @throws org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
	 *         Si los parámetros de la URL no coinciden con los valores del enum.
	 */
	@GetMapping("/admin/users")
	public String listUsers(
		@RequestParam(name = "orderBy", required = false) UserOrderField orderBy,
		@RequestParam(name = "direction", required = false) SortDirection direction,
		Model model
	) {
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

			return "html/admin/user-list";

		} catch (SecurityException ex) {
			model.addAttribute("errorMessage", ex.getMessage());
			return "error/403";
		}
	}

	// -------------------------------------------------------------------------
	// OPERACIONES EN BLOQUE
	// -------------------------------------------------------------------------

	/**
	 * Procesa una operación en bloque (otorgar admin, quitar admin o borrar).
	 *
	 * @param action Tipo de acción masiva (GRANT_ADMIN, REVOKE_ADMIN o DELETE_USERS).
	 * @param ids    Lista de IDs seleccionados en el formulario.
	 * @param model  Modelo de datos para la vista de confirmación o error.
	 * @return Redirección o plantilla según el resultado.
	 *
	 * ----------------------------------------------------------------------------
	 * CONVERSIÓN AUTOMÁTICA DE ENUMS
	 * ----------------------------------------------------------------------------
	 * Spring convierte el parámetro "action" recibido (por GET o POST)
	 * en un valor de BulkActionType automáticamente.
	 *
	 * Si el valor no coincide con ninguno de los definidos en el enum,
	 * lanzará MethodArgumentTypeMismatchException antes de ejecutar el método,
	 * que será gestionada por ErrorControllerAdvice → plantilla error/400.html.
	 */
	@PostMapping("/admin/users/bulk")
	public String bulkAction(
		@RequestParam("action") BulkActionType action,
		@RequestParam("ids") List<Integer> ids,
		Model model
	) {
		try {
			switch (action) {
				case GRANT_ADMIN -> userListService.setAdminStatusBulk(ids, true);
				case REVOKE_ADMIN -> userListService.setAdminStatusBulk(ids, false);
				case DELETE_USERS -> userListService.deleteUsersBulk(ids);
			}

			model.addAttribute("message", "Operación realizada correctamente.");
			return "html/admin/user-bulk-success";

		} catch (OperationFailedException ex) {
			model.addAttribute("errorMessage", ex.getMessage());
			return "error/operation-error";

		} catch (SecurityException ex) {
			model.addAttribute("errorMessage", ex.getMessage());
			return "error/403";
		}
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE LA CONVERSIÓN AUTOMÁTICA DE ENUMS EN SPRING MVC
 * ----------------------------------------------------------------------------
 * Spring convierte automáticamente los parámetros String de la URL en valores
 * de enumerados (enums) siempre que los nombres coincidan.
 *
 * Ejemplo:
 *   /admin/users?orderBy=EMAIL&direction=ASC
 *
 * Si el valor no coincide con ningún miembro del enum, Spring lanza una
 * MethodArgumentTypeMismatchException antes de ejecutar el método.
 *
 * Esta excepción puede manejarse globalmente mediante un controlador de
 * excepciones anotado con @ControllerAdvice para mostrar una página de error
 * personalizada (por ejemplo, 400 Bad Request o una plantilla Thymeleaf).
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE ENUMS PARA ACCIONES MASIVAS
 * ----------------------------------------------------------------------------
 * Usar el enum BulkActionType mejora la seguridad y legibilidad:
 *   - Evita errores por cadenas mal escritas.
 *   - Centraliza las operaciones válidas.
 *   - Permite autocompletado y validación en tiempo de compilación.
 *
 * Además, mantiene el código alineado con la filosofía de Spring Boot:
 * validación declarativa y conversión de tipos automática.
 */