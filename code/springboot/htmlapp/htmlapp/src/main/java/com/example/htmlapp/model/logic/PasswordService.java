// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.logic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

/**
 * Servicio responsable de la gestión y cifrado de contraseñas.
 *
 * En esta versión didáctica, el hash se calcula manualmente
 * usando un algoritmo de MessageDigest (SHA-256).
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Service
 * ----------------------------------------------------------------------------
 * Indica que esta clase pertenece a la capa de negocio o de
 * "servicios" (lógica de aplicación).
 *
 * Spring detecta automáticamente las clases anotadas con @Service
 * durante el escaneo de componentes (@ComponentScan) y las registra
 * como beans dentro del contexto de la aplicación.
 *
 * Esto permite inyectarlas fácilmente en otras clases mediante
 * constructor o campo (@Autowired, @RequiredArgsConstructor, etc.).
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL USO DE BASE64
 * ----------------------------------------------------------------------------
 * Base64 es una forma de codificar datos binarios (como bytes o
 * caracteres no imprimibles) en texto plano seguro para transmitir
 * o almacenar.
 *
 * Cada grupo de 3 bytes binarios se convierte en 4 caracteres de
 * texto, utilizando solo letras, números y los símbolos '+' y '/'.
 *
 * Ejemplo:
 *   - Bytes originales:  [ 72, 111, 108, 97 ]   (que representa "Hola")
 *   - Codificado Base64: "SG9sYQ=="
 *
 * En este servicio, usamos Base64 para representar la salt (una
 * secuencia de bytes aleatorios) en forma de texto, de modo que
 * se pueda almacenar sin problemas en la base de datos o enviarse
 * por JSON sin perder información.
 *
 * El método Base64.getEncoder().encodeToString(byte[])
 * crea exactamente esta representación textual, devolviendo una
 * cadena de caracteres ASCII imprimibles (sin bytes binarios),
 * lo que hace posible guardar o transmitir los datos de forma
 * segura y portable.
 */
@Service
public class PasswordService {

	private static final int SALT_LENGTH = 16; // bytes

	/**
	 * Genera una salt aleatoria codificada en Base64.
	 *
	 * encodeToString convierte los bytes en texto ASCII seguro,
	 * ideal para almacenar en una columna VARCHAR.
	 */
	public String generateSalt() {
		byte[] saltBytes = new byte[SALT_LENGTH];
		new SecureRandom().nextBytes(saltBytes);
		return Base64.getEncoder().encodeToString(saltBytes);
	}

	/**
	 * Calcula el hash de una contraseña concatenada con la salt.
	 *
	 * En este ejemplo usamos el algoritmo SHA-256 de forma manual
	 * para que los alumnos vean el proceso paso a paso.
	 *
	 * @param password Contraseña en texto plano.
	 * @param salt     Salt generada y almacenada junto al usuario.
	 * @return Hash resultante codificado en Base64.
	 */
	public String hashPassword(String password, String salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String combined = password + salt;
			byte[] hashBytes = digest.digest(
				combined.getBytes(StandardCharsets.UTF_8)
			);
			return Base64.getEncoder().encodeToString(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error: algoritmo SHA-256 no disponible", e);
		}
	}

	/**
	 * Verifica si una contraseña introducida coincide con su hash.
	 *
	 * @param rawPassword Contraseña en texto plano introducida por
	 *                    el usuario.
	 * @param salt        Salt asociada al usuario.
	 * @param hash        Hash almacenado en la base de datos.
	 * @return true si coincide, false en caso contrario.
	 */
	public boolean verifyPassword(String rawPassword, String salt, String hash) {
		String candidateHash = hashPassword(rawPassword, salt);
		return candidateHash.equals(hash);
	}
}

/*
 * ----------------------------------------------------------------------------
 * ALTERNATIVA: USANDO BCryptPasswordEncoder (PRODUCCIÓN)
 * ----------------------------------------------------------------------------
 *
 * En un entorno real, en lugar de calcular el hash manualmente, se recomienda
 * usar el componente BCryptPasswordEncoder de Spring Security.
 *
 * ----------------------------------------------------------------------------
 * POR QUÉ BCRYPT ES MÁS SEGURO QUE SHA-256
 * ----------------------------------------------------------------------------
 * 1. BCrypt es un algoritmo lento a propósito. Esto hace que los ataques
 *    de fuerza bruta (probar millones de contraseñas por segundo) sean
 *    mucho más difíciles que con SHA-256, que es extremadamente rápido.
 *
 * 2. BCrypt incluye automáticamente su propia salt interna, distinta para
 *    cada contraseña. Por eso, incluso si dos usuarios tienen la misma
 *    contraseña, los hashes serán diferentes.
 *
 * 3. BCrypt usa una función de coste configurable (por defecto 10 rondas)
 *    que determina cuántas veces se ejecuta el algoritmo. Aumentar el coste
 *    hace el hash más lento y más seguro frente a ataques modernos.
 *
 * ----------------------------------------------------------------------------
 * GESTIÓN DE LA SALT EN BCRYPT
 * ----------------------------------------------------------------------------
 * No es necesario guardar la salt aparte en la base de datos.
 *
 * BCrypt ya incluye su propia salt dentro del hash resultante.
 * La salt se codifica como parte del string del hash (entre el
 * coste y el hash final).
 *
 * Cuando se llama a encoder.matches(), Spring Security extrae
 * automáticamente esa salt del propio hash y la usa para
 * recalcular y verificar la contraseña.
 *
 * Esto simplifica la gestión de usuarios: basta con almacenar
 * un único campo password_hash.
 *
 * ----------------------------------------------------------------------------
 * VERSIÓN DE LA CLASE USANDO BCRYPT
 * ----------------------------------------------------------------------------
 *
 * import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 * import org.springframework.stereotype.Service;
 *
 * @Service
 * public class PasswordService {
 *
 *     private final BCryptPasswordEncoder encoder =
 *         new BCryptPasswordEncoder();
 *
 *     public String hashPassword(String password) {
 *         return encoder.encode(password);
 *     }
 *
 *     public boolean verifyPassword(String rawPassword, String hash) {
 *         return encoder.matches(rawPassword, hash);
 *     }
 * }
 *
 * ----------------------------------------------------------------------------
 * RESUMEN
 * ----------------------------------------------------------------------------
 * - SHA-256 (manual): bueno para entender el proceso.
 * - BCrypt: más seguro, más lento y con salt interna automática.
 * - En producción, BCrypt o Argon2 son las opciones recomendadas.
 */