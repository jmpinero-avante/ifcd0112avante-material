// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio responsable de la gestión de usuarios.
 *
 * Centraliza toda la lógica de negocio relacionada con los usuarios:
 *  - Creación de nuevos usuarios.
 *  - Actualización de datos personales.
 *  - Cambio de contraseña.
 *  - Borrado de cuentas.
 *  - Asignación o retirada de privilegios de administrador.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Service
 * ----------------------------------------------------------------------------
 * Indica que esta clase pertenece a la capa de lógica de negocio.
 * Spring la detecta automáticamente y la registra como bean.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA SEPARACIÓN ENTRE REPOSITORIO Y SERVICIO
 * ----------------------------------------------------------------------------
 * El repositorio (UserRepository) se encarga exclusivamente del acceso
 * a la base de datos: buscar, insertar, actualizar o borrar registros.
 *
 * El servicio añade la "lógica de negocio": validaciones, control de
 * errores, comprobaciones de permisos y operaciones más complejas.
 */
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final PermissionsService permissionsService;

	/**
	 * Crea un nuevo usuario con su salt y hash de contraseña.
	 *
	 * Por seguridad, el campo isAdmin se fuerza siempre a false
	 * durante el registro. Solo un administrador existente puede
	 * modificar este valor, o puede ajustarse manualmente en la
	 * base de datos para inicializar el primer administrador.
	 */
	@Transactional
	public User registerUser(String fullName, String email, String password) {
		String salt = passwordService.generateSalt();
		String hash = passwordService.hashPassword(password, salt);

		User user = User.builder()
			.fullName(fullName)
			.email(email)
			.salt(salt)
			.passwordHash(hash)
			.isAdmin(false)
			.build();

		try {
			return userRepository.insert(user);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException(
				"Ya existe un usuario con ese correo electrónico."
			);
		}
	}

	/**
	 * Actualiza los datos personales de un usuario.
	 *
	 * Este método ignora cualquier intento de modificar los campos
	 * sensibles (isAdmin, salt o passwordHash), copiando siempre
	 * sus valores originales desde la base de datos antes de guardar.
	 *
	 * @param id   ID del usuario.
	 * @param user Objeto con los nuevos datos personales.
	 * @return El usuario actualizado.
	 */
	@Transactional
	public User updateUser(int id, User user) {
		User existing = permissionsService.checkAdminOrLoggedUserPermission(id);

		// Conserva los valores originales de los campos sensibles
		user.setId(existing.getId());
		user.setIsAdmin(existing.isAdmin());
		user.setSalt(existing.getSalt());
		user.setPasswordHash(existing.getPasswordHash());

		return userRepository.save(user);
	}

	/**
	 * Cambia la contraseña de un usuario.
	 *
	 * @param id       ID del usuario.
	 * @param password Nueva contraseña en texto plano.
	 */
	@Transactional
	public void changePassword(int id, String password) {
		User user = permissionsService.checkAdminOrLoggedUserPermission(id);

		String newSalt = passwordService.generateSalt();
		String newHash = passwordService.hashPassword(password, newSalt);
		user.setSalt(newSalt);
		user.setPasswordHash(newHash);

		userRepository.save(user);
	}

	/**
	 * Borra un usuario por su ID.
	 *
	 * @param id Identificador del usuario a eliminar.
	 */
	@Transactional
	public void deleteUser(int id) {
		permissionsService.checkAdminOrLoggedUserPermission(id);
		userRepository.deleteById(id);
	}

	/**
	 * Devuelve un usuario a partir de su ID.
	 *
	 * @param id Identificador.
	 * @return Optional<User> con el usuario o vacío si no existe.
	 */
	public Optional<User> findById(int id) {
		permissionsService.checkAdminOrLoggedUserPermission(id);
		return userRepository.findById(id);
	}

	/**
	 * Cambia el estado de administrador de un usuario.
	 *
	 * Solo puede realizarse por un administrador.
	 *
	 * @param id      ID del usuario.
	 * @param isAdmin Nuevo valor del campo isAdmin.
	 * @return Usuario actualizado.
	 */
	@Transactional
	public User setAdminStatus(int id, boolean isAdmin) {
		permissionsService.checkAdminPermission();

		return userRepository.findById(id)
			.map(user -> {
				user.setIsAdmin(isAdmin);
				return userRepository.save(user);
			})
			.orElseThrow(() ->
				new IllegalArgumentException("El usuario no existe.")
			);
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE EL CONTROL DE PERMISOS
 * ----------------------------------------------------------------------------
 * Este servicio delega todas las comprobaciones de seguridad a
 * PermissionsService. Los métodos de dicho servicio devuelven el
 * usuario validado, lanzando una excepción si no existe o si el
 * usuario logado no tiene permisos suficientes.
 *
 * Esto evita duplicar lógica y garantiza un flujo coherente:
 *   - updateUser(int id, User user)    → admin o propietario.
 *   - changePassword(int id, String)   → admin o propietario.
 *   - deleteUser(int id)               → admin o propietario.
 *   - setAdminStatus(int id, boolean)  → solo admin.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL CONTROL DE CAMPOS SENSIBLES EN UPDATE
 * ----------------------------------------------------------------------------
 * El método updateUser no permite modificar los campos:
 *   - isAdmin       → privilegios del usuario.
 *   - salt          → semilla criptográfica usada para el hash.
 *   - passwordHash  → contraseña cifrada del usuario.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE @RequiredArgsConstructor
 * ----------------------------------------------------------------------------
 * @RequiredArgsConstructor (de Lombok) genera automáticamente un constructor
 * con todos los campos final, permitiendo la inyección sin @Autowired.
 */