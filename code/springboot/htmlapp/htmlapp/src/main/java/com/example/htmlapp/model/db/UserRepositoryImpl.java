// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación personalizada del repositorio de usuarios.
 *
 * Se encarga de operaciones que requieren control directo sobre
 * el EntityManager, como inserciones con refresh para obtener
 * valores generados automáticamente en la base de datos.
 */
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public User insert(User user) {
		entityManager.persist(user);
		entityManager.flush();   // Fuerza la escritura inmediata del INSERT
		entityManager.refresh(user); // Recupera valores generados por la BD
		return user;
	}
}

/*
===============================================================================
NOTAS PEDAGÓGICAS
===============================================================================
1. SOBRE EL MÉTODO insert()
----------------------------
Este patrón garantiza que el objeto `User` devuelto tenga actualizados los
valores asignados por PostgreSQL en el momento de la inserción, como:

 - `creation_timestamp` (timestamp DEFAULT CURRENT_TIMESTAMP)
 - `is_admin` (boolean DEFAULT FALSE)

Sin el `refresh()`, esos campos quedarían `null` en el objeto Java hasta
la siguiente consulta.

2. SOBRE entityManager.flush()
-------------------------------
`flush()` asegura que el `INSERT` se ejecute inmediatamente, antes del
`refresh()`. De lo contrario, Hibernate podría posponer la operación.

3. DIFERENCIA ENTRE save() E insert()
-------------------------------------
- `save()` (de JpaRepository) realiza un merge completo, generando UPDATE
  si el ID no es nulo o INSERT si lo es.
- `insert()` aquí está diseñado solo para nuevas entidades y devuelve
  los valores reales que la base de datos haya generado.

4. OBJETIVO PEDAGÓGICO
------------------------
Este ejemplo enseña cómo extender un repositorio de Spring Data con
operaciones personalizadas cuando necesitamos comportamiento más fino
que el que ofrece JpaRepository.
===============================================================================
*/