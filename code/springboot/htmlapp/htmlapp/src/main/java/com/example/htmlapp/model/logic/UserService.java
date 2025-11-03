// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;
import com.example.htmlapp.model.logic.exceptions.OperationFailedException;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de gestión de usuarios.
 *
 * Gestiona la creación, actualización y eliminación de usuarios,
 * así como el cambio de contraseñas. Centraliza la lógica de negocio
 * relacionada con la entidad `User`.
 *
 * ----------------------------------------------------------------------------
 * RESPONSABILIDADES
 * ----------------------------------------------------------------------------
 * - Registrar nuevos usuarios con hash seguro de contraseña.
 * - Actualizar datos personales (email, nombre completo).
 * - Cambiar contraseñas.
 * - Eliminar usuarios de la base de datos.
 * - Mantener la integridad y validación de datos.
 *
 * ----------------------------------------------------------------------------
 * SOBRE @Service
 * ----------------------------------------------------------------------------
 * Indica que esta clase pertenece a la capa de lógica de negocio.
 * Puede ser inyectada en controladores u otros servicios mediante
 * el sistema de inyección de dependencias de Spring.
 */
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordService passwordService;

	// -------------------------------------------------------------------------
	// REGISTRO DE NUEVO USUARIO
	// -------------------------------------------------------------------------

	/**
	 * Registra un nuevo usuario con salt y hash de contraseña.
	 *
	 * @param fullName  Nombre completo.
	 * @param email     Email (único).
	 * @param password  Contraseña en texto plano.
	 * @return El objeto User recién creado.
	 *
	 * @throws OperationFailedException si el email ya existe.
	 */
	@SuppressWarnings("null")
	@Transactional
	public User registerUser(String fullName, String email, String password) {
		if (userRepository.findByEmail(email).isPresent()) {
			throw new OperationFailedException("El email ya está registrado.", 400);
		}

		String salt = passwordService.generateSalt();
		String hash = passwordService.hashPassword(password, salt);

		User newUser = User.builder()
			.fullName(fullName)
			.email(email)
			.salt(salt)
			.passwordHash(hash)
			.isAdmin(false)
			.build();

		return userRepository.insert(newUser);
	}

	// -------------------------------------------------------------------------
	// ACTUALIZACIÓN DE DATOS PERSONALES
	// -------------------------------------------------------------------------

	/**
	 * Actualiza los datos de un usuario (nombre, email).
	 *
	 * @param user Usuario con los datos actualizados.
	 */
	@Transactional
	public void updateUser(User user) {
		if (user.getId() == null) {
			throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
		}

		Optional<User> existing = userRepository.findByEmail(user.getEmail());
		if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
			throw new IllegalArgumentException("El email ya está en uso por otro usuario.");
		}

		userRepository.save(user);
	}

	// -------------------------------------------------------------------------
	// CAMBIO DE CONTRASEÑA
	// -------------------------------------------------------------------------

	/**
	 * Cambia la contraseña de un usuario.
	 *
	 * @param user        Usuario al que pertenece la contraseña.
	 * @param newPassword Nueva contraseña en texto plano.
	 */
	@Transactional
	public void changePassword(User user, String newPassword) {
		String newSalt = passwordService.generateSalt();
		String newHash = passwordService.hashPassword(newPassword, newSalt);

		user.setSalt(newSalt);
		user.setPasswordHash(newHash);

		userRepository.save(user);
	}

	// -------------------------------------------------------------------------
	// GESTIÓN DE PRIVILEGIOS
	// -------------------------------------------------------------------------

	/**
	 * Asigna o revoca privilegios de administrador.
	 *
	 * @param id      ID del usuario objetivo.
	 * @param isAdmin true para otorgar privilegios, false para revocarlos.
	 */
	@Transactional
	public void setAdminStatus(int id, boolean isAdmin) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new OperationFailedException("Usuario no encontrado.", 404));

		user.setIsAdmin(isAdmin);
		userRepository.save(user);
	}

	// -------------------------------------------------------------------------
	// ELIMINACIÓN DE USUARIO
	// -------------------------------------------------------------------------

	/**
	 * Elimina un usuario por ID.
	 *
	 * @param id ID del usuario a eliminar.
	 */
	@Transactional
	public void deleteUser(int id) {
		if (!userRepository.existsById(id)) {
			throw new OperationFailedException("El usuario no existe.", 404);
		}
		userRepository.deleteById(id);
	}

	// -------------------------------------------------------------------------
	// UTILIDADES
	// -------------------------------------------------------------------------

	/**
	 * Recupera un usuario por ID.
	 *
	 * @param id ID del usuario.
	 * @return Optional<User> con el usuario si existe.
	 */
	public Optional<User> findById(int id) {
		return userRepository.findById(id);
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. SEPARACIÓN DE CAPAS
-----------------------
Este servicio actúa como intermediario entre los controladores y la capa
de acceso a datos (UserRepository). Contiene las validaciones y reglas
de negocio relacionadas con la entidad User.

2. GESTIÓN DE CONTRASEÑAS
--------------------------
Las contraseñas nunca se almacenan en texto plano:
   - Se genera un salt único por usuario.
   - Se calcula el hash con PasswordService.
   - El resultado se guarda en `password_hash`.

3. VALIDACIÓN DE EMAIL
-----------------------
Durante el registro y actualización se comprueba que el email sea único
y no pertenezca a otro usuario existente.

4. USO DE @Transactional
-------------------------
Garantiza que las operaciones de escritura (registro, actualización,
eliminación) se ejecuten de forma atómica.

5. OBJETIVO PEDAGÓGICO
------------------------
Este servicio ejemplifica buenas prácticas de diseño en una aplicación
Spring MVC:
 - Cohesión y claridad en la lógica de negocio.
 - Separación estricta de capas.
 - Buenas prácticas de seguridad en el manejo de contraseñas.
===============================================================================
*/