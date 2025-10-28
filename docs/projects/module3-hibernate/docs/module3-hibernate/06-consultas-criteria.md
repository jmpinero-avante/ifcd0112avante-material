# Consultas con Criteria API en Hibernate

## Introducción

El API de **Criteria** de Hibernate ofrece una forma **programática y tipada** de construir consultas.  
A diferencia del HQL, que se escribe como texto, Criteria permite crear consultas mediante **objetos Java**, proporcionando verificación en tiempo de compilación y flexibilidad para generar consultas dinámicas.

Este enfoque resulta ideal cuando:

- Se necesita construir consultas condicionales de forma dinámica.
- Se desea evitar errores de sintaxis en consultas HQL escritas como cadenas.
- Se trabaja con filtros que dependen de variables del usuario.

---

## Estructura general de una consulta Criteria

```java
Session session = HibernateUtil.getSessionFactory().openSession();
CriteriaBuilder cb = session.getCriteriaBuilder();
CriteriaQuery<Libro> cq = cb.createQuery(Libro.class);
Root<Libro> root = cq.from(Libro.class);
cq.select(root);
List<Libro> resultados = session.createQuery(cq).getResultList();
session.close();
```

En este ejemplo se realiza el equivalente a la consulta:
```sql
SELECT * FROM libros;
```

---

## Consulta con condición (WHERE)

Ejemplo: obtener todos los libros con precio mayor que 20.

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Libro> cq = cb.createQuery(Libro.class);
    Root<Libro> root = cq.from(Libro.class);

    cq.select(root).where(cb.greaterThan(root.get("precio"), 20.0));

    List<Libro> libros = session.createQuery(cq).getResultList();
    libros.forEach(System.out::println);
}
```

Equivalente en HQL:
```java
FROM Libro l WHERE l.precio > 20
```

---

## Consulta con múltiples condiciones (AND / OR)

Ejemplo: obtener libros disponibles cuyo precio sea menor que 25.

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Libro> cq = cb.createQuery(Libro.class);
    Root<Libro> root = cq.from(Libro.class);

    Predicate disponible = cb.isTrue(root.get("disponible"));
    Predicate precioMaximo = cb.lessThan(root.get("precio"), 25.0);

    cq.select(root).where(cb.and(disponible, precioMaximo));

    session.createQuery(cq).getResultList().forEach(System.out::println);
}
```

---

## Consulta ordenada (ORDER BY)

Ejemplo: listar todos los libros ordenados por fecha de publicación descendente.

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Libro> cq = cb.createQuery(Libro.class);
    Root<Libro> root = cq.from(Libro.class);

    cq.select(root).orderBy(cb.desc(root.get("fechaPublicacion")));

    session.createQuery(cq).getResultList().forEach(System.out::println);
}
```

---

## Consulta con JOIN (Libro ↔ Editorial)

Ejemplo: obtener todos los libros junto con su editorial.

```java
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
    Root<Libro> libroRoot = cq.from(Libro.class);
    Join<Libro, Editorial> joinEditorial = libroRoot.join("editorial");

    cq.multiselect(libroRoot.get("titulo"), joinEditorial.get("nombre"));

    session.createQuery(cq).getResultList()
           .forEach(obj -> System.out.println(obj[0] + " - " + obj[1]));
}
```

Equivalente en HQL:
```java
SELECT l.titulo, e.nombre FROM Libro l JOIN l.editorial e
```

---

## Uso en el repositorio

Podemos incluir consultas Criteria en nuestro `LibroRepository` para crear filtros dinámicos.  
Por ejemplo, un método que busque libros según varios criterios opcionales:

```java
public List<Libro> buscarConFiltros(String autor, Boolean disponible, Double precioMax) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Libro> cq = cb.createQuery(Libro.class);
        Root<Libro> root = cq.from(Libro.class);

        List<Predicate> condiciones = new ArrayList<>();

        if (autor != null) condiciones.add(cb.equal(root.get("autor"), autor));
        if (disponible != null) condiciones.add(cb.equal(root.get("disponible"), disponible));
        if (precioMax != null) condiciones.add(cb.lessThan(root.get("precio"), precioMax));

        cq.select(root).where(cb.and(condiciones.toArray(new Predicate[0])));

        return session.createQuery(cq).getResultList();
    }
}
```

Este método genera dinámicamente las condiciones `WHERE` según los parámetros no nulos.

---

## Comparativa: HQL vs Criteria

| Aspecto | HQL | Criteria API |
|----------|-----|---------------|
| Sintaxis | Texto tipo SQL | Código Java tipado |
| Validación | En tiempo de ejecución | En tiempo de compilación |
| Flexibilidad | Menor (consulta fija) | Alta (consulta dinámica) |
| Legibilidad | Más compacta | Más verbosa |
| Uso recomendado | Consultas estáticas | Consultas dinámicas y condicionales |

---

## Clases principales del API de Criteria

| Clase | Descripción |
|--------|--------------|
| `CriteriaBuilder` | Fábrica de consultas; permite crear expresiones, predicados y órdenes. |
| `CriteriaQuery<T>` | Representa la consulta que se va a ejecutar, con tipo de retorno `T`. |
| `Root<T>` | Representa la entidad principal (equivalente a la tabla base). |
| `Predicate` | Representa una condición lógica (`WHERE`, `AND`, `OR`). |
| `Join<X,Y>` | Representa una relación entre dos entidades (`JOIN`). |

---

## Conclusión

El API de Criteria proporciona una manera segura, flexible y tipada de construir consultas en Hibernate.  
Es especialmente útil cuando los filtros de búsqueda varían o cuando las condiciones dependen de la entrada del usuario.  
En combinación con los repositorios, permite mantener un código limpio, reutilizable y fácil de mantener.
