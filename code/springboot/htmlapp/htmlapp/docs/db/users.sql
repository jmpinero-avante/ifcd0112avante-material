-- vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

-- ============================================================================
-- CREACIÓN DE LA TABLA "users"
-- ============================================================================
-- Esta tabla almacena la información básica de los usuarios de la aplicación.
--
-- Incluye credenciales cifradas (salt + hash), los datos personales y el
-- rol de administrador (is_admin). Los campos con valores por defecto están
-- configurados para que PostgreSQL los asigne automáticamente al insertar
-- nuevos registros.
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
		id_user SERIAL PRIMARY KEY,
		-- Identificador único de cada usuario (clave primaria)

		email VARCHAR(255) NOT NULL UNIQUE,
		-- Correo electrónico del usuario (debe ser único y obligatorio)

		full_name VARCHAR(255),
		-- Nombre completo del usuario

		salt VARCHAR(60) NOT NULL,
		-- Cadena aleatoria (salt) utilizada para proteger el hash de la contraseña

		password_hash VARCHAR(80) NOT NULL,
		-- Hash de la contraseña cifrada (contraseña + salt)

		is_admin BOOLEAN NOT NULL DEFAULT FALSE,
		-- Indica si el usuario tiene privilegios de administrador.
		-- NOT NULL evita valores nulos y DEFAULT FALSE asegura que todos los
		-- usuarios nuevos sean no administradores por defecto.

		creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
		-- Fecha y hora de creación del registro (puesta automáticamente por la BD)
);

-- ============================================================================
-- COMENTARIOS DIDÁCTICOS
-- ============================================================================
-- 1. Los valores por defecto (DEFAULT) se aplican automáticamente si el campo
--    no se incluye en el INSERT.
--
-- 2. El uso de NOT NULL en campos booleanos evita tener que tratar valores
--    nulos en la aplicación Java, simplificando la lógica del modelo.
--
-- 3. SERIAL crea una secuencia interna en PostgreSQL para autoincrementar el
--    campo id_user. Si se usa en un entorno moderno, podría sustituirse por:
--       id_user INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY;
--
-- 4. Esta estructura está pensada para integrarse con Hibernate/JPA usando la
--    entidad com.example.htmlapp.model.db.User con anotaciones equivalentes.
--
-- 5. Si se desea reiniciar la tabla (por ejemplo, para un entorno de pruebas),
--    puede hacerse con:
--       TRUNCATE TABLE users RESTART IDENTITY CASCADE;
-- ============================================================================


-- ============================================================================
-- USUARIO ADMINISTRADOR INICIAL (EJEMPLO)
-- ============================================================================
-- Este INSERT crea el primer usuario con privilegios de administrador.
-- Los valores de 'salt' y 'password_hash' deben generarse mediante el
-- PasswordService de la aplicación para asegurar coherencia y seguridad.
-- ============================================================================

INSERT INTO users (email, full_name, salt, password_hash, is_admin)
VALUES (
	'admin@example.com',
	'Administrador del sistema',
	'REEMPLAZAR_SALT',
	'REEMPLAZAR_HASH',
	TRUE
);

-- Una vez creado el primer administrador, se recomienda iniciar sesión con
-- esta cuenta para gestionar los privilegios de los demás usuarios.