// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio responsable de la gestión de usuarios.
 *
 * Centraliza toda la lógica de negocio relacionada con los usuarios:
 *  - Creación de nuevos usuarios.
 *  - Actualización de datos personales.
 *  - Borrado de cuentas.
 *  - Asignación o retirada de privilegios de administrador.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Service
 * ----------------------------------------------------------------------------
 * Indica que esta clase pertenece a la capa de lógica de negocio.
 * Spring la detecta automáticamente y la registra como bean.
 *
 * Esto permite inyectarla fácilmente en controladores o en otros
 * servicios que necesiten manipular usuarios.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA SEPARACIÓN ENTRE REPOSITORIO Y SERVICIO
 * ----------------------------------------------------------------------------
 * El repositorio (UserRepository) se encarga exclusivamente del acceso
 * a la base de datos: buscar, insertar, actualizar o borrar registros.
 *
 * El servicio añade la "lógica de negocio": validaciones, control de
 * errores, comprobaciones de permisos, y operaciones más complejas que
 * combinan varias consultas o reglas de aplicación.
 */
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final AuthService authService;

	/**
	 * Crea un nuevo usuario con su salt y hash de contraseña.
	 *
	 * Se utiliza el patrón Builder (de Lombok) para construir
	 * el objeto de manera clara y legible.
	 *
	 * Por seguridad, el campo isAdmin se fuerza siempre a false
	 * durante el registro. Solo un administrador existente puede
	 * modificar este valor, o puede ajustarse manualmente en la
	 * base de datos para inicializar el primer administrador.
	 *
	 * @param fullName Nombre completo del usuario.
	 * @param email    Email único.
	 * @param password Contraseña en texto plano.
	 * @return El usuario recién creado.
	 * @throws OperationFailedException si ya existe un usuario con el mismo email.
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
			throw new OperationFailedException(
				"Ya existe un usuario con ese correo electrónico.", ex
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
	 * @param id   Identificador del usuario a modificar.
	 * @param user Usuario con los nuevos datos personales.
	 * @return El usuario actualizado.
	 * @throws OperationFailedException si el usuario no existe o la actualización falla.
	 */
	@Transactional
	public User updateUser(int id, User user) {
		User existing = userRepository.findById(id)
			.orElseThrow(() -> new OperationFailedException(
				"El usuario especificado no existe o fue eliminado."
			));

		// Conserva los valores originales de los campos sensibles
		user.setIsAdmin(existing.isAdmin());
		user.setSalt(existing.getSalt());
		user.setPasswordHash(existing.getPasswordHash());
		user.setId(existing.getId());

		try {
			return userRepository.save(user);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"No se pudo actualizar el usuario en la base de datos.", ex
			);
		}
	}

	/**
	 * Cambia la contraseña de un usuario.
	 *
	 * @param id       ID del usuario al que se le cambia la contraseña.
	 * @param password Nueva contraseña en texto plano.
	 * @throws OperationFailedException si el usuario no existe o la operación falla.
	 */
	@Transactional
	public void changePassword(int id, String password) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new OperationFailedException(
				"No se puede cambiar la contraseña: el usuario no existe."
			));

		String newSalt = passwordService.generateSalt();
		String newHash = passwordService.hashPassword(password, newSalt);
		user.setSalt(newSalt);
		user.setPasswordHash(newHash);

		try {
			userRepository.save(user);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"No se pudo actualizar la contraseña del usuario.", ex
			);
		}
	}

	/**
	 * Borra un usuario por su ID.
	 *
	 * @param id Identificador del usuario a eliminar.
	 * @throws OperationFailedException si el usuario no existe o no puede eliminarse.
	 */
	@Transactional
	public void deleteUser(int id) {
		if (!userRepository.existsById(id)) {
			throw new OperationFailedException(
				"No se puede eliminar: el usuario no existe."
			);
		}

		try {
			userRepository.deleteById(id);
		} catch (Exception ex) {
			throw new OperationFailedException(
				"Error al eliminar el usuario de la base de datos.", ex
			);
		}
	}

	/**
	 * Devuelve un usuario a partir de su ID.
	 *
	 * @param id Identificador.
	 * @return Optional<User> con el usuario o vacío si no existe.
	 */
	public Optional<User> findById(int id) {
		return userRepository.findById(id);
	}

	/**
	 * Cambia el estado de administrador de un usuario.
	 *
	 * Usa un estilo funcional con los métodos encadenados de Optional:
	 * map() para modificar el valor si existe,
	 * y orElseThrow() para manejar el caso en que no haya usuario.
	 *
	 * @param id      ID del usuario.
	 * @param isAdmin Nuevo valor del campo isAdmin.
	 * @return Usuario actualizado.
	 * @throws OperationFailedException si el usuario no existe o la actualización falla.
	 */
	@Transactional
	public User setAdminStatus(int id, boolean isAdmin) {
		User updatedUser = userRepository.findById(id)
			.map(user -> {
				user.setIsAdmin(isAdmin);
				return userRepository.save(user);
			})
			.orElseThrow(() -> new OperationFailedException(
				"El usuario especificado no existe o no se pudo actualizar."
			));

		return updatedUser;
	}
}

/*
 * ----------------------------------------------------------------------------
 * SOBRE EL CONTROL DE CAMPOS SENSIBLES EN UPDATE
 * ----------------------------------------------------------------------------
 *
 * El método updateUser no permite modificar los campos:
 *   - isAdmin       → privilegios del usuario.
 *   - salt          → semilla criptográfica usada para el hash.
 *   - passwordHash  → contraseña cifrada del usuario.
 *
 * Estos valores se preservan desde el registro original en la base
 * de datos antes de realizar la actualización. Esto evita que un
 * usuario malintencionado manipule el formulario para escalar sus
 * privilegios o alterar su contraseña sin pasar por los métodos
 * adecuados (como changePassword o setAdminStatus).
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE @RequiredArgsConstructor
 * ----------------------------------------------------------------------------
 * @RequiredArgsConstructor (de Lombok) genera automáticamente un constructor
 * con todos los campos declarados como final o anotados con @NonNull.
 *
 * ----------------------------------------------------------------------------
 * SOBRE Optional, map(), orElseThrow(), orElse(), orElseGet()
 * ----------------------------------------------------------------------------
 * - map(): transforma el valor si está presente.
 * - orElseThrow(): lanza una excepción si está vacío.
 * - orElse(): devuelve un valor alternativo (evaluado siempre).
 * - orElseGet(): devuelve un valor alternativo (evaluado solo si está vacío).
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DEL PATRÓN BUILDER
 * ----------------------------------------------------------------------------
 * Lombok permite crear instancias de forma legible y segura:
 *
 *   User user = User.builder()
 *       .fullName("Juan Pérez")
 *       .email("juan@example.com")
 *       .salt("abc123")
 *       .passwordHash("...")
 *       .isAdmin(false)
 *       .build();
 *
 * Ventajas:
 *  - Legibilidad y reducción de errores.
 *  - Objetos más seguros e inmutables.
 *  - Facilita mantener un diseño limpio.
 */