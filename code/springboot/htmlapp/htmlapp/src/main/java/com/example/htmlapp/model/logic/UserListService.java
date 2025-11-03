// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;
import com.example.htmlapp.model.enums.SortDirection;
import com.example.htmlapp.model.enums.UserOrderField;
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio que gestiona operaciones sobre listas de usuarios.
 *
 * Centraliza las acciones que afectan a varios usuarios al mismo tiempo,
 * como listar, borrar o cambiar privilegios en bloque.
 *
 * Todas estas operaciones requieren privilegios de administrador.
 * La verificación se realiza mediante PermissionsService.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE @Service
 * ----------------------------------------------------------------------------
 * Marca esta clase como parte de la capa de lógica de negocio. Spring la
 * detecta automáticamente y la inyecta donde sea necesario.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @RequiredArgsConstructor
 * ----------------------------------------------------------------------------
 * Genera automáticamente un constructor con los atributos final,
 * lo que permite la inyección de dependencias sin @Autowired.
 */
@Service
@RequiredArgsConstructor
public class UserListService {

	private final UserRepository userRepository;
	private final PermissionsService permissionsService;

	/**
	 * Devuelve una lista de todos los usuarios, con orden dinámico.
	 *
	 * @param orderBy   Campo por el que ordenar (EMAIL, FULL_NAME o CREATION_DATETIME).
	 * @param direction Dirección del orden (ASC o DESC).
	 * @return Lista ordenada de usuarios.
	 * @throws SecurityException si el usuario actual no tiene privilegios de administrador.
	 */
	public List<User> listAllUsers(UserOrderField orderBy, SortDirection direction)
		throws SecurityException {

		// Verifica que el usuario actual sea administrador antes de continuar.
		permissionsService.checkAdminPermission();

		List<User> users = userRepository.findAll();

		/*
		 * Selección del comparador en función del campo de ordenación.
		 * Si el valor es null o no coincide con ningún caso, se aplicará el
		 * bloque 'default', que ordena por la fecha de creación (creationTimestamp).
		 */
		Comparator<User> comparator = switch (orderBy) {
			case EMAIL -> Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER);
			case FULL_NAME -> Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER);
			default -> Comparator.comparing(User::getCreationTimestamp);
		};

		// Si la dirección de orden es descendente, invierte el comparador.
		if (direction == SortDirection.DESC) {
			comparator = comparator.reversed();
		}

		// Devuelve la lista de usuarios ordenada.
		return users.stream().sorted(comparator).collect(Collectors.toList());
	}

	// =========================================================================
	// MÉTODOS AUXILIARES DE FILTRADO
	// =========================================================================

	/**
	 * Filtra una lista de IDs para eliminar el del usuario logado actual.
	 *
	 * Si tras el filtrado no queda ningún ID válido, lanza una excepción.
	 *
	 * @param ids Lista original de IDs recibida en la operación bulk.
	 * @return Lista de IDs válidos tras excluir al usuario actual.
	 * @throws SecurityException         si el usuario actual no tiene permisos.
	 * @throws OperationFailedException  si la lista queda vacía tras el filtrado.
	 */
	public List<Integer> getFilteredUserIds(List<Integer> ids)
		throws SecurityException, OperationFailedException {

		User admin = permissionsService.checkAdminPermission();
		Integer currentUserId = admin.getId();

		List<Integer> filteredIds = ids.stream()
			.filter(id -> id != null && !id.equals(currentUserId))
			.collect(Collectors.toList());

		if (filteredIds.isEmpty()) {
			throw new OperationFailedException(
				"No hay usuarios válidos para procesar la operación. " +
				"El usuario actual no puede modificarse ni eliminarse a sí mismo.", 400);
		}

		return filteredIds;
	}

	/**
	 * Recupera una lista de usuarios a partir de una lista de IDs, excluyendo
	 * siempre al usuario logado actual.
	 *
	 * Este método combina la lógica de filtrado con la consulta a base de datos.
	 *
	 * @param ids Lista original de IDs.
	 * @return Lista de entidades User válidas.
	 * @throws SecurityException         si el usuario actual no tiene permisos.
	 * @throws OperationFailedException  si no queda ningún usuario válido tras el filtrado.
	 */
	public List<User> listFilteredUsers(List<Integer> ids)
		throws SecurityException, OperationFailedException {

		List<Integer> validIds = getFilteredUserIds(ids);
		return userRepository.findAllByIdIn(validIds);
	}

	// =========================================================================
	// OPERACIONES MASIVAS
	// =========================================================================

	/**
	 * Cambia el estado de administrador de una lista de usuarios.
	 *
	 * Utiliza la operación bulk del repositorio para mejorar el rendimiento,
	 * ejecutando una sola sentencia SQL:
	 *
	 *   UPDATE users SET is_admin = ? WHERE id IN (...)
	 *
	 * @param ids     Lista de IDs de usuario.
	 * @param isAdmin Nuevo valor para el campo isAdmin.
	 * @throws SecurityException         si el usuario no tiene permisos de administrador.
	 * @throws OperationFailedException  si no hay usuarios válidos para modificar.
	 */
	@Transactional
	public void setAdminStatusBulk(List<Integer> ids, boolean isAdmin)
		throws SecurityException, OperationFailedException {

		List<Integer> targetIds = getFilteredUserIds(ids);
		userRepository.updateAdminStatusBulk(targetIds, isAdmin);
	}

	/**
	 * Borra una lista de usuarios en una única operación SQL.
	 *
	 * Usa el método personalizado del repositorio:
	 *
	 *   DELETE FROM users WHERE id IN (...)
	 *
	 * Esto evita múltiples llamadas a deleteById() y mejora la eficiencia.
	 *
	 * @param ids Lista de IDs de usuario a eliminar.
	 * @throws SecurityException         si el usuario no tiene permisos de administrador.
	 * @throws OperationFailedException  si no hay usuarios válidos para eliminar.
	 */
	@Transactional
	public void deleteUsersBulk(List<Integer> ids)
		throws SecurityException, OperationFailedException {

		List<Integer> targetIds = getFilteredUserIds(ids);
		userRepository.deleteAllByIds(targetIds);
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE STREAMS Y LAMBDAS
 * ----------------------------------------------------------------------------
 * Este servicio utiliza programación funcional (Streams, Lambdas) para
 * manipular listas de usuarios de forma legible:
 *
 *   filteredIds = ids.stream()
 *       .filter(id -> !id.equals(currentUserId))
 *       .collect(Collectors.toList());
 *
 *   users.stream()
 *       .sorted(comparator)
 *       .collect(Collectors.toList());
 *
 * Esto simplifica la lógica y evita bucles explícitos.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA SEGURIDAD Y LAS EXCEPCIONES
 * ----------------------------------------------------------------------------
 * Cada método invoca permissionsService.checkAdminPermission() al principio.
 * Si el usuario no tiene privilegios de administrador, se lanza una
 * SecurityException que será gestionada por ErrorControllerAdvice.
 *
 * Además, se excluye siempre al usuario logado de las operaciones en bloque.
 * Si no queda ningún usuario tras esa exclusión, se lanza una excepción
 * OperationFailedException con código 400 para notificarlo explícitamente.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE OPERACIONES BULK EN REPOSITORIOS
 * ----------------------------------------------------------------------------
 * Los métodos deleteAllByIds() y updateAdminStatusBulk() ejecutan consultas
 * JPQL masivas a través del repositorio, lo que:
 *   - Reduce el número de consultas a la base de datos.
 *   - Mejora el rendimiento en operaciones sobre grandes volúmenes de datos.
 *   - Mantiene el servicio centrado en la lógica de negocio y permisos.
 *
 * ----------------------------------------------------------------------------
 * OBJETIVO PEDAGÓGICO
 * ----------------------------------------------------------------------------
 * Este servicio ejemplifica cómo:
 *  - Separar responsabilidades entre capas (negocio ↔ persistencia).
 *  - Controlar permisos y validar datos antes de acceder al repositorio.
 *  - Usar operaciones bulk con Spring Data JPA de forma segura.
 *  - Gestionar errores de forma coherente con la capa de presentación.
 */