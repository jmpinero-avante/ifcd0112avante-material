// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.db;

/**
 * Interfaz de repositorio personalizado para operaciones específicas
 * no cubiertas por JpaRepository.
 *
 * Esta interfaz se implementa manualmente en `UserRepositoryImpl`.
 */
public interface UserRepositoryCustom {

	/**
	 * Inserta un nuevo usuario en la base de datos y actualiza el objeto
	 * con los valores por defecto generados por el motor (como timestamps
	 * o valores booleanos definidos con DEFAULT).
	 *
	 * @param user Entidad User a persistir.
	 * @return La misma entidad actualizada (refrescada desde la BD).
	 */
	User insert(User user);
}