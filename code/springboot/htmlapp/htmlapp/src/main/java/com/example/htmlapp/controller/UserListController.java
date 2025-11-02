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
import com.example.htmlapp.model.logic.UserListService;

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
			return "admin/user-list";
		} catch (SecurityException ex) {
			model.addAttribute("errorMessage", ex.getMessage());
			return "error/403";
		}
	}

	/**
	 * Procesa una operación en bloque (otorgar admin, quitar admin o borrar).
	 *
	 * @param action Tipo de acción: "grant", "revoke" o "delete".
	 * @param ids    Lista de IDs seleccionados en el formulario.
	 * @param model  Modelo de datos para la vista de confirmación o error.
	 * @return Redirección o plantilla según el resultado.
	 */
	@PostMapping("/admin/users/bulk")
	public String bulkAction(
		@RequestParam("action") String action,
		@RequestParam("ids") List<Integer> ids,
		Model model
	) {
		try {
			switch (action) {
				case "grant" -> userListService.setAdminStatusBulk(ids, true);
				case "revoke" -> userListService.setAdminStatusBulk(ids, false);
				case "delete" -> userListService.deleteUsersBulk(ids);
				default -> throw new IllegalArgumentException("Acción no válida.");
			}
			model.addAttribute("message", "Operación realizada correctamente.");
			return "admin/user-bulk-success";
		} catch (IllegalArgumentException ex) {
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
 * De esta forma se mantiene la filosofía de Spring:
 *   - Los controladores permanecen limpios.
 *   - La validación y manejo de errores se centraliza.
 *   - El framework gestiona automáticamente la conversión de tipos.
 */