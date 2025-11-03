// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.tools;

import com.example.htmlapp.model.logic.PasswordService;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Utilidad de consola para verificar si una contraseña en texto plano coincide
 * con un hash dado y su correspondiente salt.
 *
 * ----------------------------------------------------------------------------
 * USO:
 * ----------------------------------------------------------------------------
 * Modo 1 (con flags explícitos):
 *   mvn exec:java -Dexec.mainClass="com.example.htmlapp.tools.VerifyPassword" \
 *                 -Dexec.args="--password admin1234 --salt a9BzQkR2x1tP7fW3 \
 *                               --hash 5f4dcc3b5aa765d61d8327deb882cf99"
 *
 * Modo 2 (interactivo si faltan parámetros):
 *   mvn exec:java -Dexec.mainClass="com.example.htmlapp.tools.VerifyPassword"
 *
 * Salida:
 *   Password matches: true
 *
 * Código de salida:
 *   0 -> la contraseña coincide con el hash
 *   1 -> la contraseña no coincide o hay algún error
 *
 * ----------------------------------------------------------------------------
 * Este programa utiliza el mismo algoritmo que PasswordService,
 * garantizando resultados coherentes con el login de la aplicación.
 */
public class VerifyPassword {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Map<String, String> params = parseArgs(args);
		Scanner scanner = new Scanner(System.in);

		String password = params.get("password");
		String salt = params.get("salt");
		String hash = params.get("hash");

		// Solicitud interactiva si faltan parámetros
		if (password == null || password.isEmpty()) {
			System.out.print("Introduce la contraseña: ");
			password = scanner.nextLine().trim();
		}
		if (salt == null || salt.isEmpty()) {
			System.out.print("Introduce la salt: ");
			salt = scanner.nextLine().trim();
		}
		if (hash == null || hash.isEmpty()) {
			System.out.print("Introduce el hash esperado: ");
			hash = scanner.nextLine().trim();
		}

		if (password.isEmpty() || salt.isEmpty() || hash.isEmpty()) {
			System.err.println("Error: todos los parámetros son obligatorios.");
			System.exit(1);
		}

		PasswordService passwordService = new PasswordService();
		boolean matches;

		try {
			matches = passwordService.verifyPassword(password, salt, hash);
		} catch (Exception ex) {
			System.err.println("Error verificando la contraseña: " + ex.getMessage());
			System.exit(1);
			return;
		}

		System.out.println("\n=== Verificación de contraseña ===");
		System.out.println("Longitud contraseña introducida: " + password.length());
		System.out.println("Salt: " + salt);
		System.out.println("Hash esperado: " + hash);
		System.out.println("Resultado: Password matches = " + matches);

		// No cerrar Scanner(System.in) — puede interferir con otros procesos
		System.exit(matches ? 0 : 1);
	}

	/**
	 * Analiza los argumentos pasados en línea de comandos con formato:
	 *   --password valor --salt valor --hash valor
	 *
	 * @param args array de argumentos recibidos desde main
	 * @return mapa con claves "password", "salt" y "hash"
	 */
	private static Map<String, String> parseArgs(String[] args) {
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "--password" -> {
					if (i + 1 < args.length) map.put("password", args[++i]);
				}
				case "--salt" -> {
					if (i + 1 < args.length) map.put("salt", args[++i]);
				}
				case "--hash" -> {
					if (i + 1 < args.length) map.put("hash", args[++i]);
				}
				case "--help", "-h" -> {
					showHelp();
					System.exit(0);
				}
				default -> { /* ignorar argumentos no reconocidos */ }
			}
		}
		return map;
	}

	/**
	 * Muestra un mensaje de ayuda con ejemplos de uso.
	 */
	private static void showHelp() {
		System.out.println("Uso:");
		System.out.println("  mvn exec:java -Dexec.mainClass=\"com.example.htmlapp.tools.VerifyPassword\" \\");
		System.out.println("               -Dexec.args=\"--password <pwd> --salt <salt> --hash <hash>\"");
		System.out.println("\nSi no se indican parámetros, se solicitarán por teclado.\n");
		System.out.println("Ejemplo:");
		System.out.println("  mvn exec:java -Dexec.mainClass=\"com.example.htmlapp.tools.VerifyPassword\" \\");
		System.out.println("               -Dexec.args=\"--password admin1234 "
			+ "--salt a9BzQkR2x1tP7fW3 --hash 5f4dcc3b5aa765d61d8327deb882cf99\"");
	}
}