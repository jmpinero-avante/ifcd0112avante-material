// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.List;

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
 * Este servicio **no maneja vistas, sesión ni permisos**.
 * Se limita exclusivamente a ejecutar operaciones de negocio
 * sobre los datos recibidos, asumiendo que el controlador ya
 * ha validado los permisos necesarios.
 */
@Service
@RequiredArgsConstructor
public class UserListService {

	private final UserRepository userRepository;

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
	// FILTRADO DE USUARIOS
	// -------------------------------------------------------------------------

	/**
	 * Filtra una lista de IDs y devuelve solo los usuarios existentes.
	 *
	 * No realiza comprobaciones de permisos; se asume que el controlador
	 * ya ha excluido los usuarios que no deben procesarse (como el logado).
	 *
	 * @param ids Lista de IDs seleccionados.
	 * @return Lista de usuarios existentes.
	 */
	@Transactional(readOnly = true)
	public List<User> listUsersByIds(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new IllegalArgumentException("Debe seleccionar al menos un usuario.");
		}

		List<User> users = userRepository.findAllByIdIn(ids);
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
		if (ids == null || ids.isEmpty()) {
			throw new OperationFailedException("No hay usuarios válidos para modificar.", 400);
		}
		userRepository.updateAdminStatusByIds(ids, isAdmin);
	}

	/**
	 * Elimina en bloque una lista de usuarios.
	 *
	 * @param ids Lista de IDs a eliminar.
	 */
	@Transactional
	public void deleteUsersBulk(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new OperationFailedException("No hay usuarios válidos para eliminar.", 400);
		}
		userRepository.deleteAllByIdIn(ids);
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. PUREZA DE LA LÓGICA DE NEGOCIO
---------------------------------
Este servicio no conoce el contexto de sesión ni los permisos del usuario.
Su única responsabilidad es ejecutar operaciones válidas y consistentes
sobre el conjunto de usuarios recibido.

2. SEGURIDAD
-------------
La verificación de permisos (por ejemplo, si el usuario es administrador)
se realiza en el **controlador** antes de invocar estos métodos.

3. EFICIENCIA EN OPERACIONES BULK
---------------------------------
Los métodos bulk utilizan sentencias SQL directas a través del repositorio
para minimizar el tráfico y mejorar el rendimiento.

4. SEPARACIÓN DE CAPAS
-----------------------
- `UserRepository` → consultas y acceso a datos.
- `UserListService` → operaciones de negocio.
- `UserListController` → control de flujo, vistas y permisos.

5. OBJETIVO PEDAGÓGICO
------------------------
Ilustrar un diseño de servicios limpio, reusable y sin acoplamiento
a la capa de presentación o seguridad.
===============================================================================
*/