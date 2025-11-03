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
 * Se encarga de proporcionar listados ordenados de usuarios y de ejecutar
 * operaciones masivas (bulk) como:
 *  - Otorgar privilegios de administrador.
 *  - Revocar privilegios de administrador.
 *  - Eliminar usuarios.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA SEPARACIÓN DE RESPONSABILIDADES
 * ----------------------------------------------------------------------------
 * - Este servicio **no maneja la vista ni la sesión HTTP** directamente.
 * - La lógica de seguridad y permisos se delega en `PermissionsService`.
 * - La lógica de negocio se realiza aquí de forma atómica y validada.
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

		List<User> users = userRepository.findAllById(ids).stream()
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
	@SuppressWarnings("null")
	@Transactional
	public void setAdminStatusBulk(List<Integer> ids, boolean isAdmin) {
		List<User> validUsers = listFilteredUsers(ids);
		for (User user : validUsers) {
			user.setIsAdmin(isAdmin);
		}
		userRepository.saveAll(validUsers);
	}

	/**
	 * Elimina en bloque una lista de usuarios (excepto el logado).
	 *
	 * @param ids Lista de IDs a eliminar.
	 */
	@SuppressWarnings("null")
	@Transactional
	public void deleteUsersBulk(List<Integer> ids) {
		List<User> validUsers = listFilteredUsers(ids);
		userRepository.deleteAll(validUsers);
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. SEGURIDAD CENTRALIZADA
--------------------------
Todas las operaciones requieren permisos de administrador.
   - Se valida al inicio de cada método.
   - La lógica de seguridad está en `PermissionsService`.

2. GESTIÓN DE LISTADOS
-----------------------
Los métodos de ordenación (`findAllOrderBy...`) se implementan en
`UserRepository` usando consultas personalizadas, lo que permite
mantener la lógica de negocio limpia y expresiva.

3. OPERACIONES MASIVAS
-----------------------
El patrón "bulk operation" permite modificar o eliminar múltiples
registros a la vez de forma eficiente y atómica.

4. FILTRADO DE USUARIO LOGADO
------------------------------
Para evitar inconsistencias, el propio usuario autenticado no puede:
   - Revocarse los permisos de administrador.
   - Eliminar su propia cuenta dentro de una operación masiva.

5. OBJETIVO PEDAGÓGICO
------------------------
Este servicio ilustra cómo aplicar principios de:
   - **Seguridad por diseño.**
   - **Separación de responsabilidades.**
   - **Atomicidad transaccional.**
   - **Eficiencia en operaciones masivas.**
===============================================================================
*/