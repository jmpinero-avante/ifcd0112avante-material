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
 *   → El programa pedirá los valores por teclado.
 *
 * ----------------------------------------------------------------------------
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

	public static void main(String[] args) {
		Map<String, String> params = parseArgs(args);
		Scanner scanner = new Scanner(System.in);

		String password = params.get("password");
		String salt = params.get("salt");
		String hash = params.get("hash");

		// Si falta algún parámetro, lo pedimos por consola
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
			scanner.close();
			return;
		}

		System.out.println("=== Verificación de contraseña ===");
		System.out.println("Contraseña introducida: " + password);
		System.out.println("Salt: " + salt);
		System.out.println("Hash esperado: " + hash);
		System.out.println("Resultado: Password matches = " + matches);

		scanner.close();
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
		for (int i = 0; i < args.length - 1; i++) {
			switch (args[i]) {
				case "--password":
					map.put("password", args[++i]);
					break;
				case "--salt":
					map.put("salt", args[++i]);
					break;
				case "--hash":
					map.put("hash", args[++i]);
					break;
				case "--help":
				case "-h":
					showHelp();
					System.exit(0);
					break;
				default:
					break;
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