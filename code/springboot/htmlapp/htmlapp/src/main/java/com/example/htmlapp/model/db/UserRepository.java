// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio principal para la entidad User.
 *
 * Hereda de JpaRepository, lo que proporciona de forma automática
 * todos los métodos básicos CRUD:
 *
 *   - findAll()
 *   - findById()
 *   - save()
 *   - deleteById()
 *   - count()
 *
 * Además, Spring Data JPA es capaz de generar automáticamente
 * métodos de consulta basándose en el nombre del método, sin
 * necesidad de escribir consultas SQL o JPQL explícitas.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Repository
 * ----------------------------------------------------------------------------
 * En este caso, la anotación @Repository no es estrictamente
 * necesaria, porque Spring Data JPA ya registra automáticamente
 * todas las interfaces que extienden JpaRepository como beans
 * del contexto de Spring.
 *
 * Sin embargo, se puede incluir por claridad o si se desea reforzar
 * explícitamente que esta interfaz pertenece a la capa de acceso
 * a datos.
 *
 * Es buena práctica añadirla en proyectos educativos o en aquellos
 * donde se desee mantener una separación visual clara de capas.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA CREACIÓN AUTOMÁTICA DE MÉTODOS DE CONSULTA
 * ----------------------------------------------------------------------------
 * Spring Data JPA permite definir métodos como findByEmail() o
 * findByFullName() sin necesidad de usar @Query.
 *
 * El framework analiza el nombre del método (findBy + campo) y
 * genera automáticamente la consulta JPQL necesaria.
 *
 * Ejemplo:
 *   Optional<User> findByEmail(String email);
 *
 * generará internamente una consulta equivalente a:
 *   SELECT u FROM User u WHERE u.email = ?1
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Query (alternativa manual)
 * ----------------------------------------------------------------------------
 * También es posible definir el método de forma explícita usando
 * @Query y @Param, por ejemplo:
 *
 *   @Query("SELECT u FROM User u WHERE u.email = :email")
 *   Optional<User> findByEmail(@Param("email") String email);
 *
 * Esto se usa cuando necesitamos una consulta más compleja o con
 * condiciones personalizadas que no se pueden expresar con el
 * nombre del método.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Param
 * ----------------------------------------------------------------------------
 * La anotación @Param sirve para vincular un parámetro de un método
 * Java con un parámetro con nombre dentro de una consulta JPQL o SQL
 * definida con @Query.
 *
 * En el ejemplo anterior, ":email" dentro de la consulta se asocia
 * al argumento del método marcado con @Param("email").
 *
 * En resumen:
 *   - Sin @Query → Spring genera la consulta automáticamente.
 *   - Con @Query + @Param → se escribe la consulta manualmente.
 */
@Repository
public interface UserRepository
	extends JpaRepository<User, Integer>, UserRepositoryCustom {

	/**
	 * Busca un usuario por su email (se usa en el proceso de login).
	 *
	 * Spring Data JPA generará automáticamente la consulta JPQL:
	 *   SELECT u FROM User u WHERE u.email = ?1
	 */
	Optional<User> findByEmail(String email);
}