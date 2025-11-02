// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;

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
	 * El usuario logado (si es admin) también se devuelve en el listado.
	 * En caso de que se quiera excluir, puede filtrarse en el controlador.
	 *
	 * @param orderBy   Campo por el que ordenar (email, fullName, creationDatetime...).
	 * @param direction Dirección del orden (ASC o DESC).
	 * @return Lista ordenada de usuarios.
	 */
	public List<User> listAllUsers(String orderBy, String direction) {
		// Solo verifica que el usuario actual sea administrador.
		permissionsService.checkAdminPermission();

		List<User> users = userRepository.findAll();

		Comparator<User> comparator;

		switch (orderBy != null ? orderBy : "") {
			case "email" -> comparator =
				Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER);
			case "fullName" -> comparator =
				Comparator.comparing(User::getFullName,
					String.CASE_INSENSITIVE_ORDER);
			case "creationDatetime" -> comparator =
				Comparator.comparing(User::getCreationTimestamp);
			default -> comparator =
				Comparator.comparing(User::getCreationTimestamp);
		}

		if ("DESC".equalsIgnoreCase(direction)) {
			comparator = comparator.reversed();
		}

		return users.stream().sorted(comparator).collect(Collectors.toList());
	}

	/**
	 * Cambia el estado de administrador de una lista de usuarios.
	 *
	 * Ignora al usuario actualmente logado para evitar que se
	 * elimine o modifique a sí mismo.
	 *
	 * @param ids     Lista de IDs de usuario.
	 * @param isAdmin Nuevo valor para el campo isAdmin.
	 */
	@Transactional
	public void setAdminStatusBulk(List<Integer> ids, boolean isAdmin) {
		User admin = permissionsService.checkAdminPermission();
		Integer currentUserId = admin.getId();

		List<Integer> filteredIds = ids.stream()
			.filter(id -> !id.equals(currentUserId))
			.collect(Collectors.toList());

		filteredIds.forEach(id -> userRepository.findById(id).ifPresent(user -> {
			user.setIsAdmin(isAdmin);
			userRepository.save(user);
		}));
	}

	/**
	 * Borra una lista de usuarios.
	 *
	 * Este método no permite borrar al usuario logado.
	 *
	 * @param ids Lista de IDs de usuario a eliminar.
	 */
	@Transactional
	public void deleteUsersByIds(List<Integer> ids) {
		User admin = permissionsService.checkAdminPermission();
		Integer currentUserId = admin.getId();

		List<Integer> filteredIds = ids.stream()
			.filter(id -> !id.equals(currentUserId))
			.collect(Collectors.toList());

		filteredIds.forEach(userRepository::deleteById);
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
 */