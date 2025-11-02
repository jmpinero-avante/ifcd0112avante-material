// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.db;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * NOTA SOBRE EL USO DE @Data EN ENTIDADES JPA
 * --------------------------------------------
 *
 * Aunque la anotación @Data de Lombok genera automáticamente todos los métodos
 * getter, setter, toString, equals y hashCode, su uso en entidades JPA no se
 * recomienda.
 *
 * El motivo principal es que JPA gestiona las entidades por identidad (a través
 * del id o de la referencia en memoria), y los métodos equals() y hashCode()
 * generados por @Data incluyen todos los campos del objeto.
 *
 * Esto puede causar comportamientos inesperados en situaciones como:
 *   - Comparaciones entre entidades que aún no se han persistido (id = null).
 *   - Uso de las entidades dentro de colecciones (Set, Map) donde equals/
 *     hashCode se basan en valores que pueden cambiar tras la persistencia.
 *   - Posibles bucles infinitos en relaciones bidireccionales al generar
 *     toString().
 *
 * Por ello, se recomienda usar solo las anotaciones individuales:
 *   @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor y @Builder
 * para tener un control más preciso sobre qué métodos se generan y evitar
 * conflictos con el ciclo de vida de las entidades JPA.
 */

/*
 * NOTA SOBRE EL USO DE Serializable EN ENTIDADES JPA
 * ---------------------------------------------------
 *
 * Aunque muchas veces las entidades JPA funcionan correctamente sin implementar
 * la interfaz Serializable, es una buena práctica hacerlo por varios motivos:
 *
 * 1. Compatibilidad con JPA/Hibernate:
 *    El estándar JPA recomienda que las entidades sean serializables para
 *    permitir que el proveedor de persistencia (Hibernate) pueda guardar el
 *    estado de los objetos en caché o en sesión si lo necesita.
 *
 * 2. Transferencia de objetos:
 *    Si en el futuro las entidades se van a enviar entre capas o sistemas,
 *    la serialización evita errores al transmitirlas.
 *
 * 3. Buenas prácticas de interoperabilidad:
 *    Añadir Serializable mejora la compatibilidad con librerías externas,
 *    herramientas de depuración y frameworks que serializan objetos por
 *    defecto (como Spring Session o Redis).
 *
 * Por ello, se suele implementar de forma preventiva:
 *
 *     public class User implements Serializable {
 *         private static final long serialVersionUID = 1L;
 *     }
 *
 * El campo serialVersionUID es opcional, pero recomendable para mantener
 * compatibilidad entre versiones de la clase.
 */

/*
 * NOTA SOBRE EL USO DE @DynamicInsert
 * -----------------------------------
 *
 * La anotación @DynamicInsert pertenece a Hibernate (no forma parte del
 * estándar JPA) y su función es indicar al framework que, al realizar una
 * inserción (INSERT), genere dinámicamente la sentencia SQL incluyendo solo
 * los campos cuyo valor no sea nulo.
 *
 * Esto resulta especialmente útil cuando en la base de datos existen columnas
 * con valores por defecto definidos mediante DEFAULT, como:
 *
 *     creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 *     is_admin BOOLEAN DEFAULT FALSE
 *
 * Si @DynamicInsert no estuviera presente, Hibernate incluiría estos campos
 * en la sentencia INSERT con valor NULL, lo cual impediría que el motor de
 * base de datos aplicara su valor por defecto.
 */

@Entity
@Table(name = "users")
@DynamicInsert
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	// ---------- CAMPOS Y MAPEOS A LA TABLA ----------

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_user")
	private Integer id;
	// Campo clave primaria. En la BD: id_user SERIAL PRIMARY KEY

	@Column(name = "email", nullable = false, unique = true, length = 255)
	private String email;
	// Email del usuario (único y obligatorio)

	@Column(name = "full_name", length = 255)
	private String fullName;
	// Nombre completo del usuario

	@Column(name = "salt", nullable = false, length = 60)
	private String salt;
	// Salt generada aleatoriamente para aumentar la seguridad del hash

	@Column(name = "password_hash", nullable = false, length = 80)
	private String passwordHash;
	// Hash resultante de aplicar la función de hashing a (contraseña + salt)

	@Column(name = "is_admin", nullable = false, insertable = false,
	        columnDefinition = "boolean default false")
	private Boolean isAdmin;
	// Indica si el usuario tiene privilegios de administrador
	// insertable=false para que PostgreSQL aplique su DEFAULT (false)
	// nullable=false para garantizar que nunca existan valores nulos

	@Column(name = "creation_datetime", insertable = false,
	        columnDefinition = "timestamp default current_timestamp")
	private LocalDateTime creationTimestamp;
	// Fecha y hora de creación del usuario (puesta automáticamente por la BD)

	// ---------- MÉTODOS AUXILIARES ----------

	/**
	 * Método de utilidad que devuelve directamente el valor del campo isAdmin.
	 * Se incluye por comodidad y compatibilidad con plantillas Thymeleaf.
	 */
	public boolean isAdmin() {
		return getIsAdmin();
	}

	@Override
	public String toString() {
		return String.format(
			"""
			User {
				id=%d,
				email="%s",
				fullName="%s",
				isAdmin=%s,
				creationTimestamp=%s
			}
			""",
			id, email, fullName, isAdmin, creationTimestamp
		);
	}
}