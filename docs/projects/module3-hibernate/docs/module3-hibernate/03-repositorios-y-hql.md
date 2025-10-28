# Repositorios, DAO y consultas con HQL y SQL nativo

## Introducción

Hasta ahora hemos ejecutado las operaciones CRUD directamente desde el método `main`.  
Esto funciona bien para pruebas rápidas, pero en una aplicación real no es una buena práctica.

Para mejorar la organización del código, crearemos **repositorios**, que son clases dedicadas exclusivamente a manejar las operaciones con la base de datos.  
Además, aprenderemos a realizar **consultas personalizadas** usando tanto HQL (Hibernate Query Language) como SQL nativo.

---

## Creación de la clase `LibroRepository`

Archivo: `src/main/java/com/ejemplo/hibernate/repositorio/LibroRepository.java`

```java
package com.ejemplo.hibernate.repositorio;

import com.ejemplo.hibernate.modelo.Libro;
import com.ejemplo.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class LibroRepository {

    public void guardar(Libro libro) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(libro);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    public Libro buscarPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Libro.class, id);
        }
    }

    public List<Libro> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Libro", Libro.class).list();
        }
    }

    public void actualizar(Libro libro) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(libro);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Libro libro = session.get(Libro.class, id);
            if (libro != null) session.remove(libro);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }
}
```

---

## Diferencias entre DAO y Repositorio

Tanto el patrón **DAO (Data Access Object)** como el patrón **Repositorio** sirven para organizar el acceso a datos, pero su **nivel de abstracción** es distinto.

### Qué es un DAO

El **DAO** se centra en el *cómo* acceder a la base de datos: encapsula las operaciones técnicas de conexión, consulta, actualización o borrado.

```java
public class LibroDAO {
    public void insertar(Libro libro) { ... }
    public Libro buscarPorId(Long id) { ... }
    public List<Libro> listarTodos() { ... }
    public void eliminar(Long id) { ... }
}
```

### Qué es un Repositorio

El **Repositorio** surge con el enfoque de Domain-Driven Design y se centra en el *qué* y el *para qué*: ofrece métodos con significado dentro del dominio de la aplicación.

```java
public class LibroRepository {
    public List<Libro> buscarDisponibles() { ... }
    public List<Libro> buscarPorEditorial(String nombreEditorial) { ... }
}
```

### Comparativa entre DAO y Repositorio

| Característica | DAO | Repositorio |
|-----------------|-----|-------------|
| Enfoque | Acceso técnico a los datos | Operaciones de dominio |
| Nivel de abstracción | Bajo | Alto |
| Preocupación principal | Cómo se accede a los datos | Qué entidades maneja y para qué |
| Tipo de operaciones | CRUD genéricas | Consultas significativas |
| Ejemplo | `insertar(Libro)` | `buscarLibrosDisponibles()` |

En frameworks modernos como **Hibernate** o **Spring Data JPA**, el Repositorio es una evolución del DAO:  
> Todo Repositorio es un DAO, pero no todo DAO es un Repositorio.

---

## Consultas personalizadas con HQL

HQL (Hibernate Query Language) es un lenguaje de consultas similar a SQL, pero orientado a objetos.  
Usa los nombres de las **clases y atributos** en lugar de tablas y columnas.

Ejemplo:

```java
public List<Libro> buscarDisponibles() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        String hql = "FROM Libro l WHERE l.disponible = true";
        return session.createQuery(hql, Libro.class).list();
    }
}
```

---

## Consultas SQL nativas

A veces necesitamos usar SQL directamente (por ejemplo, funciones específicas del motor).

```java
public List<Libro> buscarPorPrecioMayor(double minimo) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        String sql = "SELECT * FROM libros WHERE precio > :minimo";
        return session.createNativeQuery(sql, Libro.class)
                      .setParameter("minimo", minimo)
                      .list();
    }
}
```

---

## Conclusión

DAO y Repositorio son patrones complementarios.  
El DAO abstrae la conexión con la base de datos, mientras que el Repositorio se centra en las operaciones del dominio.  
Ambos permiten escribir código más limpio, mantenible y adaptable a cambios futuros.
