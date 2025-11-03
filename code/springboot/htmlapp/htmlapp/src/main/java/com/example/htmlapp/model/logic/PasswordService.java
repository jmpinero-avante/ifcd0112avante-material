// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la gestión de contraseñas:
 * generación de salts, hashing y verificación.
 *
 * ----------------------------------------------------------------------------
 * RESPONSABILIDADES
 * ----------------------------------------------------------------------------
 * - Generar salts aleatorios seguros (SecureRandom).
 * - Generar el hash de una contraseña combinada con su salt.
 * - Verificar si una contraseña en texto plano coincide con el hash almacenado.
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL ALGORITMO DE HASHING
 * ----------------------------------------------------------------------------
 * Se usa SHA-256 como algoritmo de hash, combinando la contraseña
 * y el salt:  hash = SHA256(password + salt)
 *
 * El resultado se codifica en Base64 para facilitar su almacenamiento
 * en base de datos (campo VARCHAR).
 *
 * ----------------------------------------------------------------------------
 * CONSIDERACIONES DE SEGURIDAD
 * ----------------------------------------------------------------------------
 * - SHA-256 es un hash rápido; para entornos de producción se recomienda
 *   usar algoritmos más resistentes a fuerza bruta (como bcrypt, scrypt o Argon2).
 * - En este contexto educativo se mantiene SHA-256 por simplicidad y portabilidad.
 */
@Service
public class PasswordService {

	// -------------------------------------------------------------------------
	// GENERACIÓN DE SALTS
	// -------------------------------------------------------------------------

	/**
	 * Genera una cadena aleatoria segura (salt) en Base64.
	 *
	 * @return Salt aleatorio de 16 bytes, codificado en Base64.
	 */
	public String generateSalt() {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	// -------------------------------------------------------------------------
	// HASHING DE CONTRASEÑAS
	// -------------------------------------------------------------------------

	/**
	 * Genera el hash de una contraseña combinada con su salt.
	 *
	 * @param password Contraseña en texto plano.
	 * @param salt     Salt asociado al usuario.
	 * @return Hash resultante en Base64.
	 */
	public String hashPassword(String password, String salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest((password + salt).getBytes());
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Algoritmo SHA-256 no disponible.", e);
		}
	}

	// -------------------------------------------------------------------------
	// VERIFICACIÓN DE CONTRASEÑAS
	// -------------------------------------------------------------------------

	/**
	 * Verifica si una contraseña coincide con el hash almacenado.
	 *
	 * @param rawPassword  Contraseña introducida por el usuario (texto plano).
	 * @param salt         Salt almacenado en la base de datos.
	 * @param storedHash   Hash almacenado en la base de datos.
	 * @return true si la contraseña es válida, false si no coincide.
	 */
	public boolean verifyPassword(String rawPassword, String salt, String storedHash) {
		String computedHash = hashPassword(rawPassword, salt);
		return computedHash.equals(storedHash);
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. SEPARACIÓN DE RESPONSABILIDADES
----------------------------------
   Este servicio encapsula toda la lógica relacionada con contraseñas,
   manteniendo AuthService y UserService centrados en la sesión y la
   persistencia respectivamente.

2. ALGORITMO DE HASH
--------------------
   SHA-256 ofrece una base sólida y fácil de entender. En un entorno real
   debería reemplazarse por bcrypt o Argon2 para resistir ataques de fuerza
   bruta.

3. USO TÍPICO
--------------
   - En registro:
         String salt = passwordService.generateSalt();
         String hash = passwordService.hashPassword(plainPassword, salt);

   - En login o cambio de contraseña:
         boolean ok = passwordService.verifyPassword(plainPassword, salt, hash);

4. OBJETIVO PEDAGÓGICO
-----------------------
   Mostrar un flujo completo de autenticación con salts y hash seguro,
   ilustrando buenas prácticas sin añadir complejidad innecesaria.
===============================================================================
*/