// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.tools;

import com.example.htmlapp.model.logic.PasswordService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Genera una sentencia SQL INSERT INTO users, calculando automáticamente
 * los campos salt y password_hash a partir de una contraseña dada.
 *
 * Si se pasa el flag --execute, la sentencia se ejecutará directamente
 * contra una base de datos PostgreSQL usando JDBC.
 *
 * ----------------------------------------------------------------------------
 * USO:
 * ----------------------------------------------------------------------------
 * mvn exec:java -Dexec.mainClass="com.example.htmlapp.tools.GenerateUserInsert" \
 *   -Dexec.args="--email user@example.com --password secreta123 \
 *                --full-name 'Nombre Opcional' --is-admin true \
 *                --execute --db-url jdbc:postgresql://localhost:5432/mi_bd \
 *                --db-user postgres --db-password 1234"
 *
 * ----------------------------------------------------------------------------
 * Si no se usa --execute, el programa solo imprimirá el SQL generado.
 * ----------------------------------------------------------------------------
 */
public class GenerateUserInsert {

	public static void main(String[] args) {
		Map<String, String> params = parseArgs(args);

		String email = params.get("email");
		String password = params.get("password");
		String fullName = params.getOrDefault("full-name", "Usuario sin nombre");
		String isAdminStr = params.getOrDefault("is-admin", "false");

		boolean execute = params.containsKey("execute");
		String dbUrl = params.get("db-url");
		String dbUser = params.get("db-user");
		String dbPass = params.get("db-password");

		// Validación básica
		if (email == null || password == null) {
			System.err.println("Error: faltan argumentos obligatorios (--email y --password).");
			showHelp();
			System.exit(1);
		}

		boolean isAdmin = Boolean.parseBoolean(isAdminStr);

		// Generamos salt y hash con PasswordService
		PasswordService passwordService = new PasswordService();
		String salt = passwordService.generateSalt();
		String hash = passwordService.hashPassword(password, salt);

		// SQL generado (escape de comillas simples para mostrarlo correctamente)
		String sql = String.format(
			"INSERT INTO users (email, full_name, salt, password_hash, is_admin)\n" +
			"VALUES ('%s', '%s', '%s', '%s', %s);",
			escapeSql(email), escapeSql(fullName), salt, hash, isAdmin ? "TRUE" : "FALSE"
		);

		System.out.println("=== Generador de INSERT INTO users ===\n");
		System.out.println(sql);

		// Si no se pide ejecutar, terminamos aquí
		if (!execute) return;

		// Verificación de parámetros de conexión
		if (dbUrl == null || dbUser == null || dbPass == null) {
			System.err.println("Error: faltan parámetros de conexión.");
			System.err.println("Debe especificar --db-url, --db-user y --db-password.");
			System.exit(1);
		}

		try {
			// Registrar driver PostgreSQL explícitamente (por compatibilidad)
			Class.forName("org.postgresql.Driver");

			try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
				System.out.println("\nConexión establecida con la base de datos.");

				// Verificamos si el email ya existe
				String checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
				try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
					checkStmt.setString(1, email);
					try (ResultSet rs = checkStmt.executeQuery()) {
						if (rs.next() && rs.getInt(1) > 0) {
							System.err.println("Error: ya existe un usuario con ese email.");
							System.exit(1);
						}
					}
				}

				// Insertamos el usuario
				try (PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO users (email, full_name, salt, password_hash, is_admin) " +
					"VALUES (?, ?, ?, ?, ?);"
				)) {
					stmt.setString(1, email);
					stmt.setString(2, fullName);
					stmt.setString(3, salt);
					stmt.setString(4, hash);
					stmt.setBoolean(5, isAdmin);
					int rows = stmt.executeUpdate();
					System.out.println("Inserción completada correctamente. Filas afectadas: " + rows);
				}
			}
		} catch (Exception ex) {
			System.err.println("Error al insertar el usuario: " + ex.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Analiza los argumentos tipo --clave valor o flags booleanos.
	 */
	private static Map<String, String> parseArgs(String[] args) {
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("--")) {
				String key = arg.substring(2);
				// Si es un flag sin valor (ej. --execute)
				if (i + 1 == args.length || args[i + 1].startsWith("--")) {
					map.put(key, "true");
				} else {
					map.put(key, args[++i]);
				}
			}
		}
		return map;
	}

	/**
	 * Escapa comillas simples en valores SQL para impresión segura.
	 */
	private static String escapeSql(String value) {
		return value == null ? "" : value.replace("'", "''");
	}

	/**
	 * Muestra la ayuda con ejemplos de uso.
	 */
	private static void showHelp() {
		System.out.println("Parámetros disponibles:");
		System.out.println("  --email <correo>        (obligatorio)");
		System.out.println("  --password <contraseña> (obligatorio)");
		System.out.println("  --full-name <nombre>    (opcional, por defecto 'Usuario sin nombre')");
		System.out.println("  --is-admin <true|false> (opcional, por defecto FALSE)");
		System.out.println("  --execute               (opcional, ejecuta el INSERT)");
		System.out.println("  --db-url <url>          (requerido si se usa --execute)");
		System.out.println("  --db-user <usuario>     (requerido si se usa --execute)");
		System.out.println("  --db-password <clave>   (requerido si se usa --execute)");
		System.out.println("\nEjemplo:");
		System.out.println("  mvn exec:java -Dexec.mainClass=\"com.example.htmlapp.tools.GenerateUserInsert\" "
			+ "-Dexec.args=\"--email usuario@example.com --password secreta123 "
			+ "--full-name 'Juan Pérez' --is-admin false --execute "
			+ "--db-url jdbc:postgresql://localhost:5432/mibd "
			+ "--db-user postgres --db-password 1234\"");
	}
}