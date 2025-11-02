// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio centralizado de verificación de permisos.
 *
 * Este servicio agrupa los métodos que controlan el acceso
 * a las operaciones de la aplicación, separando la lógica de
 * autorización de la lógica de negocio (UserService, etc.).
 *
 * ----------------------------------------------------------------------------
 * DIFERENCIA ENTRE AUTENTICACIÓN Y AUTORIZACIÓN
 * ----------------------------------------------------------------------------
 * - Autenticación (AuthService): identifica al usuario y gestiona
 *   su sesión (login, logout, usuario actual).
 *
 * - Autorización (PermissionsService): decide si el usuario puede
 *   realizar una determinada acción o acceder a una parte concreta
 *   del sistema, según su rol o identidad.
 *
 * ----------------------------------------------------------------------------
 * USO TÍPICO:
 * ----------------------------------------------------------------------------
 *   permissionsService.checkLoggedUserPermission();
 *   permissionsService.checkAdminPermission();
 *   permissionsService.checkAdminOrLoggedUserPermission(userId);
 *
 *   // o en su versión extendida, que devuelve el User:
 *   User u = permissionsService.checkAdminOrLoggedUserPermission(5);
 */
@Service
@RequiredArgsConstructor
public class PermissionsService {

	private final AuthService authService;
	private final UserRepository userRepository;

	/**
	 * Verifica que haya un usuario logado.
	 *
	 * @return El usuario logado.
	 * @throws SecurityException si no hay usuario logado.
	 */
	public User checkLoggedUserPermission() {
		return authService.getLoggedUser().orElseThrow(() ->
			new SecurityException("Acceso denegado: no hay usuario logado.")
		);
	}

	/**
	 * Verifica que el usuario logado tenga privilegios de administrador.
	 *
	 * @return El usuario administrador.
	 * @throws SecurityException si no hay usuario o no es administrador.
	 */
	public User checkAdminPermission() {
		User user = checkLoggedUserPermission();
		if (!user.isAdmin()) {
			throw new SecurityException(
				"Acceso denegado: el usuario no tiene privilegios de administrador."
			);
		}
		return user;
	}

	/**
	 * Verifica que el usuario logado sea el propietario o administrador.
	 *
	 * @param userId ID del usuario sobre el que se realiza la acción.
	 * @return El usuario sobre el que se tiene permiso.
	 * @throws SecurityException si no existe o no tiene permiso.
	 */
	public User checkAdminOrLoggedUserPermission(int userId) {
		User loggedUser = checkLoggedUserPermission();

		User target = userRepository.findById(userId).orElseThrow(() ->
			new SecurityException("El usuario especificado no existe.")
		);

		boolean isOwner = loggedUser.getId().equals(target.getId());
		boolean isAdmin = loggedUser.isAdmin();

		if (!(isOwner || isAdmin)) {
			throw new SecurityException(
				"Acceso denegado: no tiene permiso para acceder a estos datos."
			);
		}

		return target;
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE LA DEVOLUCIÓN DEL USUARIO
 * ----------------------------------------------------------------------------
 * En esta versión, los métodos devuelven el objeto User validado. Esto permite
 * a los servicios que los usan evitar duplicar búsquedas en la base de datos.
 *
 * Ejemplo:
 *   User user = permissionsService.checkAdminOrLoggedUserPermission(id);
 *   user.setFullName("Nuevo nombre");
 *   userRepository.save(user);
 *
 * ----------------------------------------------------------------------------
 * SOBRE SecurityException
 * ----------------------------------------------------------------------------
 * SecurityException es una excepción estándar apropiada para indicar
 * violaciones de acceso o permisos insuficientes.
 *
 * Puede capturarse globalmente y mostrar una página 403 personalizada.
 */