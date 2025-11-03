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
 *  - Visualización de datos personales.
 *  - Edición y actualización de datos.
 *  - Cambio de contraseña.
 *  - Eliminación de cuenta (con confirmación).
 *  - Asignación o revocación de privilegios de administrador.
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
		model.addAttribute("loggedUser", authService.getLoggedUser().orElse(null));
		return "html/user/details";
	}

	// -------------------------------------------------------------------------
	// EDITAR DATOS PERSONALES
	// -------------------------------------------------------------------------

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Integer id, Model model) {
		User target = permissionsService.checkAdminOrLoggedUserPermission(id);
		model.addAttribute("user", target);
		model.addAttribute("loggedUser", authService.getLoggedUser().orElse(null));
		return "html/user/edit";
	}

	@PostMapping("/edit/{id}")
	@Transactional
	public String processEditForm(@PathVariable Integer id,
	                              @RequestParam String email,
	                              @RequestParam(required = false) String fullName,
	                              Model model) {

		User target = permissionsService.checkAdminOrLoggedUserPermission(id);

		try {
			target.setEmail(email);
			target.setFullName(fullName);

			userService.updateUser(target);
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
	public String processChangePassword(@PathVariable Integer id,
	                                    @RequestParam String currentPassword,
	                                    @RequestParam String newPassword,
	                                    @RequestParam String confirmPassword,
	                                    Model model) {

		User target = permissionsService.checkAdminOrLoggedUserPermission(id);

		if (!newPassword.equals(confirmPassword)) {
			throw new OperationFailedException("Las contraseñas no coinciden.", 400);
		}

		boolean isSelfChange = authService.getLoggedUser()
			.map(u -> u.getId().equals(id))
			.orElse(false);

		if (isSelfChange && !authService.verifyPassword(id, currentPassword)) {
			throw new OperationFailedException(
				"La contraseña actual no es válida.", 403);
		}

		try {
			userService.changePassword(target, newPassword);
			return "html/user/change-password-success";

		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error al cambiar la contraseña.", 500, ex);
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
	public String processDelete(@PathVariable Integer id) {
		try {
			userService.deleteUser(id);

			// Si el usuario se elimina a sí mismo, cerrar sesión
			authService.getLoggedUser().ifPresent(logged -> {
				if (logged.getId().equals(id)) {
					authService.logout();
				}
			});

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
		try {
			permissionsService.checkAdminPermission();
			userService.setAdminStatus(id, true);
			return String.format("redirect:/user/details/%d", id);
		} catch (SecurityException ex) {
			throw new OperationFailedException(
				"Acceso denegado. No tiene privilegios para otorgar permisos.", 403, ex);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error inesperado al asignar privilegios de administrador.", 500, ex);
		}
	}

	@GetMapping("/revoke-admin/{id}")
	public String revokeAdminPrivileges(@PathVariable Integer id) {
		try {
			permissionsService.checkAdminPermission();
			userService.setAdminStatus(id, false);
			return String.format("redirect:/user/details/%d", id);
		} catch (SecurityException ex) {
			throw new OperationFailedException(
				"Acceso denegado. No tiene privilegios para revocar permisos.", 403, ex);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error inesperado al revocar privilegios de administrador.", 500, ex);
		}
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. CONTROL DE PERMISOS
-----------------------
Cada acción valida los permisos con PermissionsService:
 - checkAdminOrLoggedUserPermission(id)
   → Devuelve el objeto User si hay permiso, o lanza SecurityException.

2. FLUJO DE EDICIÓN
--------------------
El formulario de edición usa updateUser() de UserService, que ignora campos
sensibles (passwordHash, salt, isAdmin) para garantizar seguridad.

3. CAMBIO DE CONTRASEÑA
------------------------
El usuario debe introducir su contraseña actual (si no es admin).
Las contraseñas nuevas deben coincidir antes de aplicar el cambio.

4. ELIMINACIÓN DE USUARIO
--------------------------
Si el usuario logado se borra a sí mismo, se cierra la sesión.
El flujo retorna a la página inicial con mensaje informativo.

5. GESTIÓN DE PRIVILEGIOS ADMIN
-------------------------------
Los privilegios solo pueden cambiarse desde los métodos make-admin y
revoke-admin, accesibles exclusivamente para administradores.

6. EXCEPCIONES Y CÓDIGOS DE ERROR
---------------------------------
 - 400 → Datos inválidos o entrada incorrecta.
 - 403 → Falta de permisos o autenticación fallida.
 - 500 → Error interno o fallo inesperado.

Los errores se encapsulan en OperationFailedException para ser tratados por
el manejador global (ErrorControllerAdvice).

7. OBJETIVO PEDAGÓGICO
------------------------
Este controlador enseña cómo:
 - Separar la lógica de edición y privilegios.
 - Controlar permisos con precisión.
 - Mantener la integridad de la sesión y los datos.
 - Asignar códigos de error coherentes para depuración y trazabilidad.
===============================================================================
*/