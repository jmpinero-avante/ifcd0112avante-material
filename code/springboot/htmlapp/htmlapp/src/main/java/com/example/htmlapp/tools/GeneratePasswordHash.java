// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.tools;

import com.example.htmlapp.model.logic.PasswordService;
import java.util.Scanner;

/**
 * Utilidad de consola para generar el salt y el hash de una contraseña.
 *
 * Si se pasa la contraseña como argumento en la línea de comandos, la utiliza
 * directamente. Si no, pedirá la contraseña por teclado.
 *
 * ----------------------------------------------------------------------------
 * USO:
 * ----------------------------------------------------------------------------
 *   mvn exec:java -Dexec.mainClass="com.example.htmlapp.tools.GeneratePasswordHash" \
 *                 -Dexec.args="mi_contraseña_segura"
 *
 *   (o sin argumentos para introducirla manualmente)
 *
 * ----------------------------------------------------------------------------
 * Ejemplo de salida:
 *
 *   Salt generado: a9BzQkR2x1tP7fW3
 *   Hash generado: 5f4dcc3b5aa765d61d8327deb882cf99
 *
 *   INSERT INTO users (email, full_name, salt, password_hash, is_admin)
 *   VALUES ('usuario@example.com', 'Nombre Apellido',
 *           'a9BzQkR2x1tP7fW3', '5f4dcc3b5aa765d61d8327deb882cf99', FALSE);
 *
 * ----------------------------------------------------------------------------
 * NOTA:
 * Este programa utiliza el mismo algoritmo definido en PasswordService,
 * garantizando que la contraseña generada sea coherente con el sistema de
 * autenticación de la aplicación.
 */
public class GeneratePasswordHash {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String password;

		System.out.println("=== Generador de hash de contraseña ===");

		// Si no se pasa argumento, pedimos la contraseña por teclado
		if (args.length == 0) {
			Scanner scanner = new Scanner(System.in);
			System.out.print("Introduce la contraseña: ");
			password = scanner.nextLine().trim();
			// No cerrar System.in para evitar interferencias
		} else {
			password = args[0].trim();
			System.out.println("(Contraseña recibida como argumento)");
		}

		if (password.isEmpty()) {
			System.err.println("Error: la contraseña no puede estar vacía.");
			System.exit(1);
		}

		PasswordService passwordService = new PasswordService();
		String salt = passwordService.generateSalt();
		String hash = passwordService.hashPassword(password, salt);

		System.out.println("\nSalt generado: " + salt);
		System.out.println("Hash generado: " + hash);

		System.out.println("\n=== Ejemplo de INSERT SQL ===\n");
		System.out.printf(
			"INSERT INTO users (email, full_name, salt, password_hash, is_admin)\n" +
			"VALUES ('usuario@example.com', 'Nombre Apellido', '%s', '%s', FALSE);\n",
			salt, hash
		);

		System.out.println("\nProceso completado correctamente.");
	}
}