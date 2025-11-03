// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de validación de permisos.
 *
 * Controla el acceso a operaciones que dependen del rol del usuario
 * (administrador o usuario normal) y del contexto (propietario del recurso).
 *
 * ----------------------------------------------------------------------------
 * RESPONSABILIDADES
 * ----------------------------------------------------------------------------
 * - Verificar si el usuario tiene sesión activa.
 * - Comprobar si el usuario es administrador.
 * - Validar si un usuario puede operar sobre su propia cuenta.
 * - Lanzar excepciones de seguridad en caso de acceso no autorizado.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL DISEÑO
 * ----------------------------------------------------------------------------
 * Este servicio no accede directamente a la base de datos. Su función es
 * exclusivamente validar permisos, basándose en la información de sesión
 * gestionada por `AuthService`.
 */
@Service
@RequiredArgsConstructor
public class PermissionsService {

	private final AuthService authService;

	// -------------------------------------------------------------------------
	// COMPROBACIONES DE PERMISOS
	// -------------------------------------------------------------------------

	/**
	 * Comprueba si hay un usuario logado. Si no lo hay, lanza una excepción 403.
	 *
	 * @return El usuario logado.
	 * @throws SecurityException Si no hay sesión activa.
	 */
	public User checkLoggedUser() {
		return authService.getUser()
			.orElseThrow(() -> new SecurityException("Debe iniciar sesión para acceder."));
	}

	/**
	 * Comprueba si el usuario actual tiene privilegios de administrador.
	 *
	 * @throws SecurityException Si el usuario no es admin o no está logado.
	 */
	public void checkAdminPermission() {
		if (!isAdmin()) {
			throw new SecurityException("Acceso denegado. Solo los administradores pueden realizar esta acción.");
		}
	}

	/**
	 * Comprueba si el usuario logado es el mismo que el usuario objetivo
	 * o si tiene privilegios de administrador.
	 *
	 * @param targetId ID del usuario sobre el que se quiere operar.
	 * @return El usuario logado si tiene permiso.
	 * @throws SecurityException Si no tiene permiso para acceder.
	 */
	public User checkAdminOrLoggedUserPermission(Integer targetId) {
		User logged = checkLoggedUser();

		if (logged.isAdmin() || logged.getId().equals(targetId)) {
			return logged;
		}

		throw new SecurityException("Acceso denegado. No tiene permiso para acceder a este recurso.");
	}

	// -------------------------------------------------------------------------
	// MÉTODOS AUXILIARES (PARA USO INTERNO O DIDÁCTICO)
	// -------------------------------------------------------------------------

	/**
	 * Indica si el usuario logado puede operar sobre el usuario dado.
	 *
	 * @param target Usuario objetivo.
	 * @return true si puede acceder (admin o mismo usuario).
	 */
	public boolean canAccessUser(User target) {
		return isAdmin() ||
			authService.getUserId().map(id -> id.equals(target.getId())).orElse(false);
	}

	/**
	 * Indica si hay un usuario logado y si es administrador.
	 *
	 * @return true si el usuario logado es admin.
	 */
	public boolean isAdmin() {
		return authService.isAdmin();
	}

	/**
	 * Indica si hay un usuario logado.
	 *
	 * @return true si hay sesión activa.
	 */
	public boolean isLogged() {
		return authService.isLogged();
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. SEPARACIÓN DE RESPONSABILIDADES
----------------------------------
   - `AuthService`: gestiona sesión e información del usuario actual.
   - `PermissionsService`: valida qué puede o no puede hacer un usuario.
   - `UserService`: aplica la lógica de negocio tras las validaciones.

2. MODELO DE AUTORIZACIÓN
--------------------------
   - Usuario anónimo → no puede acceder a rutas protegidas.
   - Usuario normal  → solo puede acceder/modificar su propia cuenta.
   - Administrador   → tiene acceso a cualquier recurso o usuario.

3. EXCEPCIONES DE SEGURIDAD
----------------------------
   Las comprobaciones lanzan `SecurityException`, que es interceptada por
   `ErrorControllerAdvice` y renderiza la plantilla `error/403.html`.

4. USO DESDE CONTROLADORES
---------------------------
   - `permissionsService.checkAdminPermission();`
   - `permissionsService.checkLoggedUser();`
   - `permissionsService.checkAdminOrLoggedUserPermission(id);`
   - `permissionsService.canAccessUser(user);`

5. OBJETIVO PEDAGÓGICO
------------------------
   Mostrar cómo separar la lógica de permisos de la lógica de sesión,
   siguiendo principios de claridad, reutilización y responsabilidad única.
===============================================================================
*/