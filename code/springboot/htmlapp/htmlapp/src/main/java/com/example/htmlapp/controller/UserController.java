// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.logic.AuthService;
import com.example.htmlapp.model.logic.PermissionsService;
import com.example.htmlapp.model.logic.UserService;
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import lombok.RequiredArgsConstructor;

/**
 * Controlador que gestiona las operaciones sobre un único usuario.
 *
 * Incluye:
 * - Visualización de datos personales.
 * - Edición y actualización de datos.
 * - Cambio de contraseña.
 * - Eliminación de cuenta (con confirmación).
 * - Asignación o revocación de privilegios de administrador.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA RESPONSABILIDAD
 * ----------------------------------------------------------------------------
 * Este controlador maneja las acciones relacionadas con un usuario concreto.
 * La validación de permisos se delega en PermissionsService.
 * La persistencia y validación de negocio se delega en UserService.
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final PermissionsService permissionsService;
	private final AuthService authService;

	// -------------------------------------------------------------------------
	// VER DETALLES
	// -------------------------------------------------------------------------

	@GetMapping("/details/{id}")
	public String showUserDetails(@PathVariable Integer id, Model model) {
		User target = permissionsService.checkAdminOrLoggedUserPermission(id);

		model.addAttribute("user", target);
		model.addAttribute("loggedUser", authService.getUser().orElse(null));
		return "html/user/details";
	}

	// -------------------------------------------------------------------------
	// EDITAR DATOS PERSONALES
	// -------------------------------------------------------------------------

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Integer id, Model model) {
		User target = permissionsService.checkAdminOrLoggedUserPermission(id);
		model.addAttribute("user", target);
		model.addAttribute("loggedUser", authService.getUser().orElse(null));
		return "html/user/edit";
	}

	@PostMapping("/edit/{id}")
	@Transactional
	public String processEditForm(
		@PathVariable Integer id,
		@RequestParam String email,
		@RequestParam(required = false) String fullName,
		Model model
	) {
		User target = permissionsService.checkAdminOrLoggedUserPermission(id);

		try {
			target.setEmail(email);
			target.setFullName(fullName);
			userService.updateUser(target);

			authService.getUser().ifPresent(current -> {
				if (current.getId().equals(id)) {
					authService.refreshUser();
				}
			});

			return String.format("redirect:/user/details/%d", id);

		} catch (IllegalArgumentException ex) {
			throw new OperationFailedException(
				"Error al actualizar los datos del usuario.", 400, ex);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error inesperado al procesar la actualización.", 500, ex);
		}
	}

	// -------------------------------------------------------------------------
	// CAMBIAR CONTRASEÑA
	// -------------------------------------------------------------------------

	@GetMapping("/change-password/{id}")
	public String showChangePasswordForm(@PathVariable Integer id, Model model) {
		User target = permissionsService.checkAdminOrLoggedUserPermission(id);
		model.addAttribute("user", target);
		return "html/user/change-password";
	}

	@PostMapping("/change-password/{id}")
	@Transactional
	public String processChangePassword(
		@PathVariable Integer id,
		@RequestParam String currentPassword,
		@RequestParam String newPassword,
		@RequestParam String confirmPassword,
		Model model
	) {
		User target = permissionsService.checkAdminOrLoggedUserPermission(id);

		if (!newPassword.equals(confirmPassword)) {
			throw new OperationFailedException("Las contraseñas no coinciden.", 400);
		}

		boolean isSelfChange = authService.getUser()
			.map(u -> u.getId().equals(id))
			.orElse(false);

		if (isSelfChange && !authService.verifyPassword(id, currentPassword)) {
			throw new OperationFailedException("La contraseña actual no es válida.", 403);
		}

		try {
			userService.changePassword(target, newPassword);

			if (isSelfChange) {
				authService.refreshUser();
			}

			return "html/user/change-password-success";

		} catch (Exception ex) {
			throw new OperationFailedException("Error al cambiar la contraseña.", 500, ex);
		}
	}

	// -------------------------------------------------------------------------
	// ELIMINAR USUARIO
	// -------------------------------------------------------------------------

	@GetMapping("/delete/{id}")
	public String showDeleteConfirmation(@PathVariable Integer id, Model model) {
		User target = permissionsService.checkAdminOrLoggedUserPermission(id);
		model.addAttribute("user", target);
		return "html/user/delete-confirm";
	}

	@PostMapping("/delete/{id}")
	@Transactional
	public String processDelete(@PathVariable Integer id, Model model) {
		User current = permissionsService.checkAdminOrLoggedUserPermission(id);

		try {
			userService.deleteUser(current.getId());

			if (authService.getUser()
					.map(u -> u.getId().equals(id))
					.orElse(false)) {
				authService.logout();
				return "redirect:/?logout";
			}

			return "html/user/delete-success";

		} catch (SecurityException ex) {
			throw new OperationFailedException(
				"No tiene permisos para eliminar este usuario.", 403, ex);

		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error inesperado al eliminar el usuario.", 500, ex);
		}
	}

	// -------------------------------------------------------------------------
	// GESTIÓN DE PRIVILEGIOS ADMINISTRATIVOS
	// -------------------------------------------------------------------------

	@GetMapping("/make-admin/{id}")
	public String grantAdminPrivileges(@PathVariable Integer id) {
		permissionsService.checkAdminPermission();
		try {
			userService.setAdminStatus(id, true);

			authService.getUserId().ifPresent(currentId -> {
				if (currentId.equals(id)) {
					authService.refreshUser();
				}
			});

			return String.format("redirect:/user/details/%d", id);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error inesperado al asignar privilegios de administrador.", 500, ex);
		}
	}

	@GetMapping("/revoke-admin/{id}")
	public String revokeAdminPrivileges(@PathVariable Integer id) {
		permissionsService.checkAdminPermission();
		try {
			userService.setAdminStatus(id, false);

			authService.getUserId().ifPresent(currentId -> {
				if (currentId.equals(id)) {
					authService.refreshUser();
				}
			});

			return String.format("redirect:/user/details/%d", id);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error inesperado al revocar privilegios de administrador.", 500, ex);
		}
	}
}

/*
===============================================================================
ACTUALIZACIÓN DE SESIÓN TRAS CAMBIOS
===============================================================================
Se añaden llamadas a `authService.refreshUser()` para mantener sincronizado el
usuario en sesión tras cualquier operación que modifique su propio registro.

PUNTOS CLAVE:
 - /edit/{id} → si edita su propio perfil.
 - /change-password/{id} → si cambia su propia contraseña.
 - /make-admin /revoke-admin → si cambia su propio rol.

No se ejecuta en eliminaciones (logout ya limpia la sesión) ni en acciones
sobre otros usuarios.

===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
Este patrón asegura coherencia entre el estado persistido en base de datos
y el objeto `User` almacenado en sesión, sin obligar al usuario a cerrar y
abrir sesión tras cada cambio.
===============================================================================
*/