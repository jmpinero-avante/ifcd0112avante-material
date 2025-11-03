// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repositorio principal para la entidad User.
 *
 * Hereda de JpaRepository, proporcionando automáticamente
 * métodos CRUD básicos:
 *
 *   - findAll()
 *   - findById()
 *   - save()
 *   - deleteById()
 *   - count()
 *
 * Además, incluye operaciones personalizadas definidas en
 * UserRepositoryCustom (como insert() con refresh) e implementadas
 * en UserRepositoryImpl.
 *
 * También define métodos de ordenación y operaciones masivas (bulk).
 */
@Repository
public interface UserRepository
	extends JpaRepository<User, Integer>, UserRepositoryCustom {

	// -------------------------------------------------------------------------
	// MÉTODOS DE BÚSQUEDA
	// -------------------------------------------------------------------------

	/**
	 * Busca un usuario por su email (usado en login y registro).
	 *
	 * @param email Correo electrónico.
	 * @return Optional<User> con el usuario, si existe.
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Recupera todos los usuarios cuyos IDs estén en la lista indicada.
	 *
	 * @param ids Lista de identificadores.
	 * @return Lista de usuarios coincidentes.
	 */
	List<User> findAllByIdIn(List<Integer> ids);

	// -------------------------------------------------------------------------
	// MÉTODOS DE ORDENACIÓN PERSONALIZADA
	// -------------------------------------------------------------------------

	// Ordenar por EMAIL
	@Query("SELECT u FROM User u ORDER BY u.email ASC")
	List<User> findAllOrderByEmailAsc();

	@Query("SELECT u FROM User u ORDER BY u.email DESC")
	List<User> findAllOrderByEmailDesc();

	// Ordenar por NOMBRE COMPLETO
	@Query("SELECT u FROM User u ORDER BY u.fullName ASC")
	List<User> findAllOrderByFullNameAsc();

	@Query("SELECT u FROM User u ORDER BY u.fullName DESC")
	List<User> findAllOrderByFullNameDesc();

	// Ordenar por FECHA DE CREACIÓN
	@Query("SELECT u FROM User u ORDER BY u.creationTimestamp ASC")
	List<User> findAllOrderByCreationTimestampAsc();

	@Query("SELECT u FROM User u ORDER BY u.creationTimestamp DESC")
	List<User> findAllOrderByCreationTimestampDesc();

	// -------------------------------------------------------------------------
	// OPERACIONES MASIVAS (BULK)
	// -------------------------------------------------------------------------

	/**
	 * Elimina en bloque los usuarios cuyos IDs coincidan con la lista.
	 *
	 * @param ids Lista de IDs a eliminar.
	 */
	void deleteAllByIdIn(List<Integer> ids);

	/**
	 * Actualiza en bloque el estado de administrador de varios usuarios.
	 *
	 * Este método usa una query de actualización directa (sin cargar entidades),
	 * lo que lo hace mucho más eficiente para grandes volúmenes de datos.
	 *
	 * @param ids     Lista de IDs de usuarios.
	 * @param isAdmin Nuevo valor para el campo `isAdmin`.
	 */
	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.isAdmin = :isAdmin WHERE u.id IN :ids")
	void updateAdminStatusByIds(List<Integer> ids, boolean isAdmin);
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. SOBRE LA EXTENSIÓN DE UserRepositoryCustom
----------------------------------------------
Esta interfaz hereda también de UserRepositoryCustom, que define el método
`insert(User user)` implementado en `UserRepositoryImpl` usando persist + refresh.
Esto garantiza que al crear un usuario nuevo, los campos generados por la BD
(por ejemplo, creation_timestamp o is_admin) se devuelvan actualizados.

2. SOBRE @Modifying Y @Transactional
------------------------------------
- @Modifying indica a Spring Data que la consulta no es una SELECT, sino
  una operación de escritura (UPDATE o DELETE).
- @Transactional garantiza que la actualización se ejecute dentro de una
  transacción (necesario para confirmar los cambios).

3. EFICIENCIA DE OPERACIONES MASIVAS
-------------------------------------
Usar consultas JPQL como:
   UPDATE User u SET u.isAdmin = true WHERE u.id IN :ids
permite que la base de datos actualice todos los registros en una sola
operación SQL, sin necesidad de cargar entidades en memoria.

4. DIFERENCIA ENTRE MÉTODOS CRUD Y BULK
----------------------------------------
- Los métodos CRUD (`saveAll`, `deleteAll`) respetan el ciclo de vida JPA,
  incluyendo validaciones y sincronización de caché.
- Los métodos bulk (`updateAdminStatusByIds`, `deleteAllByIdIn`) actúan
  directamente sobre la base de datos, sin pasar por el contexto de persistencia.

5. OBJETIVO PEDAGÓGICO
------------------------
Este repositorio enseña cómo:
 - Combinar JPQL y métodos derivados automáticos.
 - Implementar operaciones masivas eficientes.
 - Aplicar buenas prácticas de transaccionalidad y diseño limpio.
===============================================================================
*/