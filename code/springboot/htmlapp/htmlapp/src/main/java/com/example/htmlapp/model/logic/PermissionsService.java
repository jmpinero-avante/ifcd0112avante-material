// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import org.springframework.stereotype.Service;

import com.example.htmlapp.model.db.User;
import com.example.htmlapp.model.db.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de control de permisos y seguridad a nivel de aplicación.
 *
 * Gestiona la verificación de permisos de acceso para operaciones
 * que involucran a usuarios específicos o acciones restringidas
 * (como la administración de cuentas o privilegios).
 *
 * ----------------------------------------------------------------------------
 * SOBRE SU PAPEL EN LA ARQUITECTURA
 * ----------------------------------------------------------------------------
 * - `AuthService` gestiona el estado de sesión y autenticación.
 * - `PermissionsService` aplica las reglas de autorización:
 *     → quién puede acceder, editar o eliminar qué recurso.
 *
 * De este modo se mantiene una separación clara entre
 * autenticación (identificar al usuario) y autorización (validar permisos).
 */
@Service
@RequiredArgsConstructor
public class PermissionsService {

	private final AuthService authService;
	private final UserRepository userRepository;

	// -------------------------------------------------------------------------
	// PERMISOS GENERALES
	// -------------------------------------------------------------------------

	/**
	 * Verifica que haya un usuario autenticado.
	 *
	 * @return Usuario autenticado.
	 * @throws SecurityException si no hay sesión activa.
	 */
	public User checkLoggedUserPermission() {
		return authService.getUser().orElseThrow(
			() -> new SecurityException("Debe iniciar sesión para continuar.")
		);
	}

	/**
	 * Verifica que el usuario autenticado tenga privilegios de administrador.
	 *
	 * @return Usuario autenticado (que es admin).
	 * @throws SecurityException si el usuario no tiene permisos de admin.
	 */
	public User checkAdminPermission() {
		User user = checkLoggedUserPermission();
		if (!user.isAdmin()) {
			throw new SecurityException("No tiene permisos de administrador.");
		}
		return user;
	}

	/**
	 * Verifica que el usuario autenticado tenga privilegios de administrador y no
	 * 	sea el usuario al que se quiere acceder.
	 *
	 * @param  targetId ID del usuario al que se desea acceder.
	 * @return Usuario autenticado (que es admin).
	 * @throws SecurityException si el usuario no tiene permisos de admin.
	 */
	public User checkOtherAdminPermission(int targetId) {
		User target = userRepository.findById(targetId).orElseThrow(
			() -> new SecurityException("El usuario solicitado no existe.")
		);

		User current = checkLoggedUserPermission();

		if (authService.isAdmin() && !current.getId().equals(targetId)) {
			return target;
		}

		throw new SecurityException("No tiene permiso para acceder a este usuario.");
	}

	// -------------------------------------------------------------------------
	// PERMISOS SOBRE USUARIOS ESPECÍFICOS
	// -------------------------------------------------------------------------

	/**
	 * Comprueba que el usuario actual tenga permiso para acceder
	 * o modificar el perfil de un usuario específico.
	 *
	 * Está permitido si:
	 *  - El usuario autenticado es administrador, o
	 *  - El usuario autenticado es el mismo cuyo ID se solicita.
	 *
	 * @param targetId ID del usuario al que se desea acceder.
	 * @return El objeto `User` de destino (si existe y hay permiso).
	 * @throws SecurityException si no hay permiso o el usuario no existe.
	 */
	public User checkAdminOrLoggedUserPermission(int targetId) {
		User target = userRepository.findById(targetId).orElseThrow(
			() -> new SecurityException("El usuario solicitado no existe.")
		);

		User current = checkLoggedUserPermission();

		if (authService.isAdmin() || current.getId().equals(targetId)) {
			return target;
		}

		throw new SecurityException("No tiene permiso para acceder a este usuario.");
	}

	// -------------------------------------------------------------------------
	// MÉTODO OPCIONAL DE CONSULTA (SIN EXCEPCIÓN)
	// -------------------------------------------------------------------------

	/**
	 * Determina si el usuario actual puede acceder a un recurso concreto
	 * perteneciente a otro usuario. Devuelve un booleano en lugar de lanzar
	 * excepción, lo que lo hace útil en plantillas o comprobaciones visuales.
	 *
	 * @param targetId ID del usuario propietario del recurso.
	 * @return true si puede acceder; false si no.
	 */
	public boolean canAccessUser(Integer targetId) {
		return authService.isAdmin() ||
			authService.getUserId().map(id -> id.equals(targetId)).orElse(false);
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. DIFERENCIA ENTRE AUTENTICACIÓN Y AUTORIZACIÓN
------------------------------------------------
- `AuthService` → confirma quién es el usuario (sesión activa).
- `PermissionsService` → valida qué puede hacer ese usuario.

2. REGLAS DE PERMISO
---------------------
- Solo los administradores pueden acceder a todos los usuarios.
- Cada usuario puede ver o modificar su propio perfil.
- Nadie puede eliminar o degradar su propia cuenta dentro de bulk.

3. USO EN LOS CONTROLADORES
----------------------------
- `checkAdminPermission()` se usa para vistas de administración.
- `checkAdminOrLoggedUserPermission(id)` para vistas de perfil.
- `canAccessUser(id)` puede usarse en vistas Thymeleaf (condicionales).

4. SIMPLIFICACIÓN DEL CÓDIGO
------------------------------
Los métodos `checkDeletePermission()` y `checkChangePasswordPermission()`
se eliminaron por redundantes, ya que `checkAdminOrLoggedUserPermission()`
cubre ambos escenarios con la misma semántica.

5. OBJETIVO PEDAGÓGICO
------------------------
Este servicio enseña cómo:
 - Centralizar la verificación de permisos en un único punto.
 - Reutilizar helpers de `AuthService` para claridad y eficiencia.
 - Lanzar excepciones uniformes (`SecurityException`) ante accesos no permitidos.
===============================================================================
*/