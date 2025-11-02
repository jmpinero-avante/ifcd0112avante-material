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
	 * @param orderBy   Campo por el que ordenar (EMAIL, FULL_NAME o null).
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
			case EMAIL -> Comparator.comparing(
				User::getEmail, String.CASE_INSENSITIVE_ORDER);
			case FULL_NAME -> Comparator.comparing(
				User::getFullName, String.CASE_INSENSITIVE_ORDER);
			default -> Comparator.comparing(User::getCreationTimestamp);
		};

		// Si la dirección de orden es descendente, invierte el comparador.
		if (direction == SortDirection.DESC) {
			comparator = comparator.reversed();
		}

		// Devuelve la lista de usuarios ordenada.
		return users.stream().sorted(comparator).collect(Collectors.toList());
	}

	/**
	 * Cambia el estado de administrador de una lista de usuarios.
	 *
	 * Ignora al usuario actualmente logado para evitar que se
	 * elimine o modifique a sí mismo.
	 *
	 * Si, tras excluir al usuario actual, no queda ningún ID válido
	 * en la lista, se lanza una OperationFailedException.
	 *
	 * @param ids     Lista de IDs de usuario.
	 * @param isAdmin Nuevo valor para el campo isAdmin.
	 * @throws SecurityException        si el usuario no tiene permisos de administrador.
	 * @throws OperationFailedException si no hay usuarios válidos para modificar.
	 */
	@Transactional
	public void setAdminStatusBulk(List<Integer> ids, boolean isAdmin)
		throws SecurityException, OperationFailedException {

		List<Integer> targetIds = getFilteredUserIds(ids);

		targetIds.forEach(id -> userRepository.findById(id).ifPresent(user -> {
			user.setIsAdmin(isAdmin);
			userRepository.save(user);
		}));
	}

	/**
	 * Borra una lista de usuarios.
	 *
	 * Este método no permite borrar al usuario logado.
	 *
	 * Si, tras excluir al usuario actual, no queda ningún ID válido
	 * en la lista, se lanza una OperationFailedException.
	 *
	 * @param ids Lista de IDs de usuario a eliminar.
	 * @throws SecurityException        si el usuario no tiene permisos de administrador.
	 * @throws OperationFailedException si no hay usuarios válidos para borrar.
	 */
	@Transactional
	public void deleteUsersBulk(List<Integer> ids)
		throws SecurityException, OperationFailedException {

		List<Integer> targetIds = getFilteredUserIds(ids);

		targetIds.forEach(userRepository::deleteById);
	}

	/**
	 * Método auxiliar que filtra la lista de IDs recibida,
	 * excluyendo siempre al usuario logado (para impedir que se
	 * modifique o borre a sí mismo).
	 *
	 * Si tras el filtrado no queda ningún ID válido, lanza una excepción
	 * OperationFailedException con un mensaje descriptivo.
	 *
	 * @param ids Lista original de IDs recibida en la operación bulk.
	 * @return Lista de IDs válidos tras excluir al usuario actual.
	 * @throws SecurityException        si el usuario actual no tiene permisos.
	 * @throws OperationFailedException si la lista queda vacía tras el filtrado.
	 */
	private List<Integer> getFilteredUserIds(List<Integer> ids)
		throws SecurityException, OperationFailedException {

		User admin = permissionsService.checkAdminPermission();
		Integer currentUserId = admin.getId();

		List<Integer> filteredIds = ids.stream()
			.filter(id -> !id.equals(currentUserId))
			.collect(Collectors.toList());

		if (filteredIds.isEmpty()) {
			throw new OperationFailedException(
				"No hay usuarios válidos para procesar la operación. " +
				"El usuario actual no puede modificarse ni eliminarse a sí mismo."
			);
		}

		return filteredIds;
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
 * SOBRE LA SEGURIDAD
 * ----------------------------------------------------------------------------
 * Cada método invoca permissionsService.checkAdminPermission() al principio.
 * Si el usuario no tiene privilegios de administrador, se lanza una
 * SecurityException que puede ser capturada por el controlador para
 * mostrar una página de error 403 (Forbidden).
 *
 * Además, se excluye siempre al usuario logado de las operaciones en bloque.
 * Si no queda ningún usuario tras esa exclusión, se lanza una excepción
 * OperationFailedException para notificarlo explícitamente.
 *
 * Las cláusulas "throws" se incluyen explícitamente para documentar que
 * estos métodos pueden lanzar tanto errores de permisos (SecurityException)
 * como errores de validación de datos (OperationFailedException).
 *
 * ----------------------------------------------------------------------------
 * NOTA SOBRE LAS EXCEPCIONES NO COMPROBADAS (RuntimeException)
 * ----------------------------------------------------------------------------
 * Aunque SecurityException y OperationFailedException son excepciones
 * no comprobadas (unchecked), se declaran explícitamente en los métodos
 * para documentar claramente los posibles puntos de fallo y mejorar la
 * legibilidad del código, especialmente en entornos educativos.
 */