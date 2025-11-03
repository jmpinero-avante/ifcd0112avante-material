// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;
import com.example.htmlapp.model.enums.SortDirection;
import com.example.htmlapp.model.enums.UserOrderField;
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de gestión de listas de usuarios.
 *
 * Se encarga de proporcionar listados ordenados y de ejecutar
 * operaciones masivas (bulk) sobre la tabla de usuarios:
 *  - Otorgar o revocar privilegios de administrador.
 *  - Eliminar usuarios.
 *
 * ----------------------------------------------------------------------------
 * RESPONSABILIDAD
 * ----------------------------------------------------------------------------
 * - Este servicio **no maneja vistas ni sesión HTTP** directamente.
 * - La verificación de permisos se realiza mediante `PermissionsService`.
 * - La persistencia y eficiencia se delega al `UserRepository`.
 */
@Service
@RequiredArgsConstructor
public class UserListService {

	private final UserRepository userRepository;
	private final AuthService authService;
	private final PermissionsService permissionsService;

	// -------------------------------------------------------------------------
	// LISTADO GENERAL DE USUARIOS
	// -------------------------------------------------------------------------

	/**
	 * Devuelve la lista de todos los usuarios ordenada según los parámetros.
	 *
	 * @param orderBy   Campo de ordenación.
	 * @param direction Dirección del orden (ASC o DESC).
	 * @return Lista de usuarios ordenada.
	 */
	@Transactional(readOnly = true)
	public List<User> listAllUsers(UserOrderField orderBy, SortDirection direction) {
		permissionsService.checkAdminPermission();

		return switch (orderBy) {
			case EMAIL -> direction == SortDirection.ASC
				? userRepository.findAllOrderByEmailAsc()
				: userRepository.findAllOrderByEmailDesc();
			case FULL_NAME -> direction == SortDirection.ASC
				? userRepository.findAllOrderByFullNameAsc()
				: userRepository.findAllOrderByFullNameDesc();
			case CREATION_DATETIME -> direction == SortDirection.ASC
				? userRepository.findAllOrderByCreationTimestampAsc()
				: userRepository.findAllOrderByCreationTimestampDesc();
		};
	}

	// -------------------------------------------------------------------------
	// FILTRADO DE USUARIOS (EXCLUYENDO USUARIO LOGADO)
	// -------------------------------------------------------------------------

	/**
	 * Filtra una lista de IDs de usuario, eliminando el usuario actual si
	 * está incluido. Devuelve los usuarios válidos.
	 *
	 * @param ids Lista de IDs seleccionados.
	 * @return Lista de usuarios existentes, sin el usuario logado.
	 */
	@Transactional(readOnly = true)
	public List<User> listFilteredUsers(List<Integer> ids) {
		permissionsService.checkAdminPermission();

		if (ids == null || ids.isEmpty()) {
			throw new IllegalArgumentException("Debe seleccionar al menos un usuario.");
		}

		Integer currentUserId = authService.getUserId().orElse(null);

		List<User> users = userRepository.findAllByIdIn(ids).stream()
			.filter(u -> !u.getId().equals(currentUserId))
			.collect(Collectors.toList());

		if (users.isEmpty()) {
			throw new OperationFailedException("No hay usuarios válidos para procesar.", 400);
		}

		return users;
	}

	// -------------------------------------------------------------------------
	// OPERACIONES MASIVAS (BULK)
	// -------------------------------------------------------------------------

	/**
	 * Cambia el estado de administrador de un conjunto de usuarios.
	 *
	 * @param ids     Lista de IDs.
	 * @param isAdmin true para otorgar, false para revocar.
	 */
	@Transactional
	public void setAdminStatusBulk(List<Integer> ids, boolean isAdmin) {
		List<User> validUsers = listFilteredUsers(ids);
		List<Integer> validIds = validUsers.stream()
			.map(User::getId)
			.collect(Collectors.toList());

		if (validIds.isEmpty()) {
			throw new OperationFailedException("No hay usuarios válidos para modificar.", 400);
		}

		userRepository.updateAdminStatusByIds(validIds, isAdmin);
	}

	/**
	 * Elimina en bloque una lista de usuarios (excepto el logado).
	 *
	 * @param ids Lista de IDs a eliminar.
	 */
	@Transactional
	public void deleteUsersBulk(List<Integer> ids) {
		List<User> validUsers = listFilteredUsers(ids);
		List<Integer> validIds = validUsers.stream()
			.map(User::getId)
			.collect(Collectors.toList());

		if (validIds.isEmpty()) {
			throw new OperationFailedException("No hay usuarios válidos para eliminar.", 400);
		}

		userRepository.deleteAllByIdIn(validIds);
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. EFICIENCIA EN OPERACIONES MASIVAS
-------------------------------------
Este servicio aprovecha los métodos bulk de `UserRepository`:
 - `updateAdminStatusByIds(...)`
 - `deleteAllByIdIn(...)`
para realizar actualizaciones y eliminaciones directas en la base de datos,
sin cargar entidades en memoria, lo que mejora enormemente el rendimiento.

2. SEGURIDAD Y PERMISOS
------------------------
Cada operación valida que el usuario sea administrador antes de continuar.
Además, se excluye siempre al usuario logado de cualquier acción masiva,
evitando que pueda eliminarse o revocarse permisos a sí mismo.

3. SEPARACIÓN DE RESPONSABILIDADES
-----------------------------------
- `UserRepository` → Acceso a datos y consultas JPQL.
- `UserListService` → Lógica de negocio y consistencia.
- `PermissionsService` → Control de seguridad y permisos.

4. OBJETIVO PEDAGÓGICO
------------------------
Ilustra cómo combinar:
 - consultas JPQL personalizadas para eficiencia,
 - validación de negocio previa a las operaciones,
 - y buenas prácticas de diseño en capas (repository → service → controller).
===============================================================================
*/