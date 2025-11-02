// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.db;

/**
 * Interfaz de repositorio personalizado.
 *
 * Define métodos adicionales que no vienen incluidos en JpaRepository
 * y que requieren una implementación específica.
 *
 * En este caso, se añade un método insert() para controlar que tras
 * guardar el usuario, se refresquen los valores generados por la BD
 * (como creation_timestamp o valores DEFAULT).
 */
public interface UserRepositoryCustom {

	/**
	 * Inserta un usuario y lo refresca para obtener los valores por
	 * defecto generados por la base de datos.
	 */
	User insert(User user);
}