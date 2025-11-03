// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Servicio responsable de la autenticación y gestión de sesión.
 *
 * Gestiona:
 *  - Inicio y cierre de sesión.
 *  - Almacenamiento del usuario actual en sesión.
 *  - Comprobación de permisos (login, admin, anónimo).
 *
 * ----------------------------------------------------------------------------
 * ESTRUCTURA GENERAL
 * ----------------------------------------------------------------------------
 * La sesión almacena directamente el objeto `User` completo para evitar
 * consultas repetidas a la base de datos. Esto simplifica el acceso
 * a propiedades como nombre o rol desde controladores y plantillas.
 *
 * Ejemplo de uso en Thymeleaf:
 *     <span th:text="${session.user.fullName}">Usuario</span>
 *     <span th:if="${session.user.isAdmin}">[ADMIN]</span>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String SESSION_USER = "user";

	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final HttpSession session;

	// -------------------------------------------------------------------------
	// LOGIN / LOGOUT
	// -------------------------------------------------------------------------

	/**
	 * Inicia sesión validando las credenciales del usuario.
	 *
	 * Si la autenticación es correcta, guarda el objeto User completo en la
	 * sesión HTTP para su uso en controladores y plantillas.
	 *
	 * @param email    Email del usuario.
	 * @param password Contraseña en texto plano.
	 * @return true si el login es correcto, false si no.
	 */
	public boolean login(String email, String password) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) return false;

		User user = userOpt.get();
		if (verifyPassword(user.getId(), password)) {
			session.setAttribute(SESSION_USER, user);
			return true;
		}
		return false;
	}

	/**
	 * Cierra la sesión actual, eliminando todos los datos almacenados.
	 */
	public void logout() {
		session.invalidate();
	}

	// -------------------------------------------------------------------------
	// INFORMACIÓN DE SESIÓN
	// -------------------------------------------------------------------------

	/**
	 * Devuelve el usuario actualmente en sesión (si lo hay).
	 */
	public Optional<User> getUser() {
		Object userObj = session.getAttribute(SESSION_USER);
		if (userObj instanceof User user) {
			return Optional.of(user);
		}
		return Optional.empty();
	}

	/**
	 * Devuelve el ID del usuario actualmente logado (si lo hay).
	 */
	public Optional<Integer> getUserId() {
		return getUser().map(User::getId);
	}

	/**
	 * Indica si hay un usuario logado.
	 */
	public boolean isLogged() {
		return getUser().isPresent();
	}

	/**
	 * Indica si no hay usuario logado (estado anónimo).
	 */
	public boolean isAnonymous() {
		return getUser().isEmpty();
	}

	/**
	 * Indica si el usuario logado es administrador.
	 */
	public boolean isAdmin() {
		return getUser().map(User::isAdmin).orElse(false);
	}

	/**
	 * Indica si la sesión pertenece a un usuario anónimo o a un administrador.
	 */
	public boolean isAnonymousOrAdmin() {
		return isAnonymous() || isAdmin();
	}

	// -------------------------------------------------------------------------
	// SINCRONIZACIÓN Y VALIDACIÓN
	// -------------------------------------------------------------------------

	/**
	 * Revalida y actualiza el objeto `User` almacenado en sesión desde la base
	 * de datos. Se usa tras modificar datos del perfil o privilegios.
	 */
	@SuppressWarnings("null")
	public void refreshUser() {
		getUser().ifPresent(u -> {
			userRepository.findById(u.getId()).ifPresent(fresh -> {
				session.setAttribute(SESSION_USER, fresh);
			});
		});
	}

	/**
	 * Comprueba si la contraseña proporcionada coincide con la almacenada
	 * para el usuario especificado.
	 *
	 * Este método no modifica la sesión (no inicia sesión ni cierra sesión),
	 * y sirve para verificar contraseñas actuales, por ejemplo, en un cambio
	 * de contraseña.
	 *
	 * @param id          ID del usuario.
	 * @param rawPassword Contraseña introducida por el usuario.
	 * @return true si coincide, false si no.
	 */
	public boolean verifyPassword(int id, String rawPassword) {
		return userRepository.findById(id)
			.map(user -> passwordService.verifyPassword(
				rawPassword,
				user.getSalt(),
				user.getPasswordHash()
			))
			.orElse(false);
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. SEPARACIÓN DE RESPONSABILIDADES
----------------------------------
   - `AuthService` gestiona la sesión y autenticación.
   - `PasswordService` encapsula la lógica criptográfica.
   - `UserService` gestiona la persistencia y validaciones de usuario.

2. SESIÓN Y RENDIMIENTO
------------------------
   Guardar el objeto completo `User` en la sesión evita consultas repetidas.
   El método `refreshUser()` sincroniza los cambios en memoria con la base
   de datos cuando el perfil o privilegios se actualizan.

3. USO DESDE CONTROLADORES
---------------------------
   - `authService.isLogged()` → saber si hay sesión activa.
   - `authService.isAdmin()`  → controlar acceso a rutas administrativas.
   - `authService.getUser()`  → obtener el usuario actual.

4. USO DESDE PLANTILLAS THYMELEAF
----------------------------------
   - `${session.user.fullName}` → muestra el nombre del usuario.
   - `<span th:if="${session.user.isAdmin}">[ADMIN]</span>` → muestra etiqueta admin.

5. OBJETIVO PEDAGÓGICO
------------------------
   Este servicio demuestra cómo estructurar la autenticación en Spring MVC
   de forma clara y modular, manteniendo la lógica de negocio separada del
   manejo de sesiones HTTP.
===============================================================================
*/