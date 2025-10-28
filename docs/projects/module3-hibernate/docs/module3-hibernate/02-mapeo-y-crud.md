# Mapeo de entidades y operaciones CRUD básicas

## Introducción

Una vez configurado Hibernate, el siguiente paso es definir las clases que se mapearán a las tablas de la base de datos.  
Cada clase Java representará una tabla, y cada objeto una fila de esa tabla.  

Comenzaremos con una única entidad llamada `Libro`, y después incorporaremos la entidad `Editorial` para practicar las relaciones.

---

## Creación de la entidad `Libro`

Archivo: `src/main/java/com/ejemplo/hibernate/modelo/Libro.java`

```java
package com.ejemplo.hibernate.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "libros")
public class Libro implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private boolean disponible;

    @Column(name = "fecha_publicacion", nullable = false)
    private java.sql.Date fechaPublicacion;
}
```

### Explicación de las anotaciones

| Anotación | Descripción |
|------------|-------------|
| `@Entity` | Marca la clase como entidad gestionada por Hibernate. |
| `@Table(name="libros")` | Indica el nombre exacto de la tabla. |
| `@Id` | Define el campo clave primaria. |
| `@GeneratedValue` | Especifica cómo se genera el identificador (auto-incremental). |
| `@Column` | Permite personalizar las columnas (nombre, longitud, nullable, etc.). |
| `implements Serializable` | Asegura compatibilidad en entornos distribuidos y frameworks. |
| `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` | Son de Lombok y simplifican el código al generar automáticamente getters, setters, constructores y el método `toString`. |

---

## Creación de la utilidad `HibernateUtil`

Para no tener que configurar Hibernate manualmente cada vez, crearemos una clase auxiliar encargada de construir la `SessionFactory`.

Archivo: `src/main/java/com/ejemplo/hibernate/util/HibernateUtil.java`

```java
package com.ejemplo.hibernate.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error al crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
```

---

## Primera prueba: insertar un registro

Archivo: `src/main/java/com/ejemplo/hibernate/App.java`

```java
package com.ejemplo.hibernate;

import com.ejemplo.hibernate.modelo.Libro;
import com.ejemplo.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class App {
    public static void main(String[] args) {
        Libro libro = Libro.builder()
                .titulo("El Principito")
                .autor("Antoine de Saint-Exupéry")
                .precio(12.99)
                .disponible(true)
                .fechaPublicacion(java.sql.Date.valueOf("1943-04-06"))
                .build();

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(libro);
            tx.commit();
            System.out.println("Libro guardado con ID: " + libro.getId());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }

        HibernateUtil.shutdown();
    }
}
```

---

## Consultas básicas

### Buscar un libro por ID

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Libro libro = session.get(Libro.class, 1L);
    if (libro != null) {
        System.out.println("Libro encontrado: " + libro.getTitulo());
    }
}
```

### Listar todos los libros

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    var query = session.createQuery("FROM Libro", Libro.class);
    var lista = query.list();
    lista.forEach(System.out::println);
}
```

### Actualizar un libro

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();

    Libro libro = session.get(Libro.class, 1L);
    if (libro != null) {
        libro.setPrecio(14.99);
        session.merge(libro);
    }
    tx.commit();
}
```

### Eliminar un libro

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();

    Libro libro = session.get(Libro.class, 2L);
    if (libro != null) {
        session.remove(libro);
    }
    tx.commit();
}
```

---

## Resumen de las operaciones CRUD

| Operación | Método | Descripción |
|------------|---------|-------------|
| Crear | `session.persist(obj)` | Inserta un nuevo registro. |
| Leer | `session.get(Clase.class, id)` | Recupera un registro por su ID. |
| Leer todos | `session.createQuery("FROM Clase", Clase.class)` | Recupera todas las filas. |
| Actualizar | `session.merge(obj)` | Modifica los datos de una fila existente. |
| Eliminar | `session.remove(obj)` | Borra un registro. |

---

Con esto ya tenemos la base para trabajar con Hibernate de manera práctica y podemos avanzar hacia la creación de repositorios que centralicen estas operaciones.
