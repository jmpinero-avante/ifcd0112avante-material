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
 * Versión optimizada que almacena el objeto User completo en sesión,
 * evitando consultas repetidas a la base de datos.
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

	public void logout() {
		session.invalidate();
	}

	// -------------------------------------------------------------------------
	// GETTERS Y UTILIDADES
	// -------------------------------------------------------------------------

	/**
	 * Devuelve el usuario actualmente logado (si lo hay).
	 */
	public Optional<User> getLoggedUser() {
		Object obj = session.getAttribute(SESSION_USER);
		if (obj instanceof User u) return Optional.of(u);
		return Optional.empty();
	}

	/**
	 * Indica si hay usuario logado.
	 */
	public boolean isLogged() {
		return session.getAttribute(SESSION_USER) != null;
	}

	/**
	 * Indica si NO hay usuario logado.
	 */
	public boolean isAnonymous() {
		return !isLogged();
	}

	/**
	 * Devuelve true si el usuario actual es administrador.
	 */
	public boolean isAdmin() {
		return getLoggedUser().map(User::isAdmin).orElse(false);
	}

	/**
	 * Devuelve true si el usuario actual es anónimo o admin.
	 */
	public boolean isAnonymousOrAdmin() {
		return isAnonymous() || isAdmin();
	}

	/**
	 * Devuelve el ID del usuario logado.
	 */
	public Optional<Integer> getLoggedUserId() {
		return getLoggedUser().map(User::getId);
	}

	/**
	 * Comprueba la validez de una contraseña sin efectos de sesión.
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

	// -------------------------------------------------------------------------
	// REFRESCO OPCIONAL
	// -------------------------------------------------------------------------

	/**
	 * Actualiza los datos del usuario en sesión leyendo de la base de datos.
	 * Útil tras un cambio de nombre o privilegios.
	 */
	public void refreshSessionUser() {
		getLoggedUserId().flatMap(userRepository::findById)
			.ifPresent(u -> session.setAttribute(SESSION_USER, u));
	}
}