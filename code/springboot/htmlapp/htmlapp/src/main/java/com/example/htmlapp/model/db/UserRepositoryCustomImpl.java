// vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

package com.example.htmlapp.model.db;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Implementación del repositorio personalizado de usuarios.
 *
 * Esta clase implementa la interfaz UserRepositoryCustom y
 * proporciona la lógica adicional que no forma parte de los
 * métodos estándar de JpaRepository.
 *
 * En este caso, se define el método insert() para permitir que
 * PostgreSQL aplique los valores por defecto (DEFAULT) definidos
 * en la tabla y, a continuación, refrescar la entidad para recuperar
 * esos valores (como el campo creation_datetime).
 *
 * ----------------------------------------------------------------------------
 * SOBRE EL NOMBRE UserRepositoryCustomImpl
 * ----------------------------------------------------------------------------
 * Spring Data JPA asocia automáticamente las interfaces de repositorio
 * personalizadas con sus implementaciones si estas terminan en "Impl"
 * y comienzan con el mismo prefijo.
 *
 * Es decir:
 *   - Interfaz: UserRepositoryCustom
 *   - Clase:    UserRepositoryCustomImpl
 *
 * Gracias a esta convención, no es necesario registrar manualmente
 * esta clase ni usar @Component: Spring la detectará automáticamente
 * y la integrará dentro del repositorio principal UserRepository.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Repository
 * ----------------------------------------------------------------------------
 * No es estrictamente necesaria aquí, ya que Spring integrará esta
 * clase automáticamente por su nombre, pero es recomendable incluirla
 * por claridad, compatibilidad y conversión de excepciones.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @Transactional
 * ----------------------------------------------------------------------------
 * Indica que los métodos de esta clase deben ejecutarse dentro de un
 * contexto de transacción. Si ocurre un error, Spring realiza un
 * rollback automático.
 *
 * También puede aplicarse a nivel de método para tener control más
 * granular. Aquí se aplica a nivel de clase para afectar a todos los
 * métodos.
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA ANOTACIÓN @PersistenceContext
 * ----------------------------------------------------------------------------
 * Esta anotación indica a Spring (a través de JPA) que debe inyectar
 * un objeto EntityManager gestionado por el contenedor.
 *
 * El EntityManager es la clase principal de JPA para interactuar con
 * la base de datos: permite persistir, buscar, actualizar y eliminar
 * entidades.
 *
 * Spring detecta esta anotación y, en tiempo de ejecución, inyecta la
 * instancia adecuada de EntityManager, configurada según los valores
 * del datasource definidos en application.yml.
 *
 * Es decir, no creamos manualmente el EntityManager: Spring lo obtiene
 * del EntityManagerFactory configurado y lo proporciona automáticamente
 * (inyección de dependencias).
 *
 * ----------------------------------------------------------------------------
 * SOBRE LA INYECCIÓN Y EL USO DE FINAL
 * ----------------------------------------------------------------------------
 * No se marca el campo entityManager como final porque su valor es
 * inyectado por el contenedor de Spring después de construir el objeto.
 *
 * Si lo marcáramos como final, exigiríamos que se inicializara en el
 * constructor, lo que entraría en conflicto con la inyección automática
 * que realiza @PersistenceContext.
 *
 * En caso de usar inyección por constructor (por ejemplo, con @Autowired
 * o @RequiredArgsConstructor), sí podría ser final, pero aquí la inyección
 * es por campo, gestionada directamente por el contenedor JPA.
 *
 * Además, como esta clase no se construye manualmente (Spring se encarga
 * de crear y gestionar la instancia), no necesitamos excluir el campo del
 * constructor ni crear constructores personalizados.
 *
 * En resumen:
 *   - @PersistenceContext inyecta el EntityManager automáticamente.
 *   - No debe ser final porque su valor se asigna tras la construcción.
 *   - No hace falta modificar constructores ni usar Lombok aquí.
 */
@Repository
@Transactional
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public User insert(User user) {
		entityManager.persist(user);
		entityManager.flush();     // fuerza la escritura inmediata en la BD
		entityManager.refresh(user); // actualiza los campos generados
		return user;
	}
}