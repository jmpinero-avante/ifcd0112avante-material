# Resumen y recetas prácticas de Hibernate

## Introducción

Este capítulo reúne las ideas clave de la guía, con resúmenes de anotaciones, comandos, patrones de diseño y recetas para escribir código seguro con Hibernate.

---

## DAO y Repositorio

El patrón **DAO (Data Access Object)** y el **Repositorio** tienen el mismo objetivo: organizar y aislar la lógica de acceso a los datos.  
Sin embargo, se diferencian en su nivel de abstracción.

| Característica | DAO | Repositorio |
|-----------------|-----|-------------|
| Enfoque | Acceso técnico a la base de datos | Operaciones del dominio |
| Nivel de abstracción | Bajo | Alto |
| Ejemplo de método | `insertar(Libro)` | `buscarLibrosDisponibles()` |

- El **DAO** encapsula las operaciones CRUD básicas.  
- El **Repositorio** añade una capa semántica que representa el dominio de la aplicación.  
- En frameworks modernos como **Hibernate** o **Spring Data JPA**, los repositorios heredan las funciones del DAO y se consideran su evolución natural.

> Todo Repositorio es un DAO, pero no todo DAO es un Repositorio.

---

## Principales anotaciones de mapeo

| Anotación | Descripción |
|------------|-------------|
| `@Entity` | Marca una clase como entidad. |
| `@Table` | Define el nombre de la tabla. |
| `@Id` | Indica la clave primaria. |
| `@GeneratedValue` | Estrategia de generación de IDs. |
| `@Column` | Configura las columnas. |
| `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany` | Define relaciones entre tablas. |
| `@JoinColumn` | Especifica la columna de enlace. |
| `@Transient` | Excluye un atributo del mapeo. |

---

## Valores de `hibernate.hbm2ddl.auto`

| Valor | Descripción | Uso recomendado |
|--------|-------------|-----------------|
| `create` | Borra y crea tablas cada vez. | Desarrollo inicial |
| `create-drop` | Crea al iniciar y borra al cerrar. | Pruebas |
| `update` | Actualiza el esquema. | Desarrollo continuo |
| `validate` | Solo valida la estructura. | Producción |
| `none` | No hace nada. | Producción |

---

## Operación CRUD básica

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();
    session.persist(entidad);
    tx.commit();
}
```

---

## HQL más usado

| HQL | Descripción |
|------|-------------|
| `FROM Clase` | Selecciona todas las filas. |
| `WHERE` | Filtro de resultados. |
| `ORDER BY` | Ordenación. |
| `COUNT` | Conteo. |
| `JOIN FETCH` | Carga relaciones perezosas. |
| `UPDATE` | Actualización masiva. |
| `DELETE` | Borrado masivo. |

---

## Lazy y Eager Loading

| Tipo | Descripción | Cuándo usar |
|------|--------------|-------------|
| Lazy | Carga diferida | Cuando no siempre se necesita la relación |
| Eager | Carga inmediata | Cuando se sabe que se usará la relación |
| JOIN FETCH | Carga forzada | En consultas complejas |

---

## Plantilla universal para operaciones seguras

```java
public void operacionSegura(Object entidad, java.util.function.Consumer<Session> operacion) {
    Transaction tx = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        tx = session.beginTransaction();
        operacion.accept(session);
        tx.commit();
    } catch (Exception e) {
        if (tx != null && tx.isActive()) tx.rollback();
        logger.error("Error en operación Hibernate segura", e);
    }
}
```

---

## Conclusión

DAO y Repositorio son pilares del diseño limpio en acceso a datos.  
Hibernate los implementa fácilmente, permitiendo una arquitectura clara, reutilizable y alineada con los principios de la programación orientada a objetos.
