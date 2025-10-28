# Relaciones entre entidades en Hibernate

## Introducción

En una base de datos relacional, las entidades no existen de forma aislada: un libro pertenece a una editorial, puede tener varios autores, o incluso detalles adicionales asociados.  
Hibernate permite modelar estas relaciones entre objetos Java y reflejarlas automáticamente en las tablas correspondientes mediante anotaciones JPA.

Cada relación define una **cardinalidad** (cuántas instancias de una entidad se asocian con cuántas de otra) y puede tener un **lado propietario** (quién controla la clave foránea) y un **lado inverso** (quién se limita a referenciarla).

---

## Relación OneToOne

Una relación **uno a uno (1:1)** significa que cada instancia de una entidad está asociada con una única instancia de otra.

### Ejemplo: `Libro` ↔ `DetalleLibro`

```java
@Entity
@Table(name = "detalle_libro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleLibro implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_paginas")
    private int numeroPaginas;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @OneToOne(mappedBy = "detalleLibro")
    private Libro libro;
}
```

Y en la entidad `Libro`:

```java
@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JoinColumn(name = "detalle_id")
private DetalleLibro detalleLibro;
```

### Explicación

- `@JoinColumn` define la columna de clave foránea en la tabla `libros`.  
- `mappedBy` indica el lado **inverso** (en este caso `DetalleLibro`).  
- `cascade = CascadeType.ALL` permite que al guardar o borrar un libro se actualice automáticamente su detalle.  
- `fetch = FetchType.LAZY` retrasa la carga del detalle hasta que se accede a él (mejor rendimiento).

### Lado propietario vs lado inverso

- El lado **propietario** es el que contiene la `@JoinColumn`.  
- El lado **inverso** usa `mappedBy` y no gestiona la clave foránea.

---

## Relación OneToMany y ManyToOne

Este es el tipo de relación más común: una editorial publica varios libros, pero cada libro pertenece a una única editorial.

### Ejemplo: `Editorial` ↔ `Libro`

#### En la clase `Editorial`:

```java
@OneToMany(mappedBy = "editorial", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Libro> libros = new ArrayList<>();
```

#### En la clase `Libro`:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "editorial_id")
private Editorial editorial;
```

### Explicación

- `mappedBy = "editorial"` indica que la relación se mapea desde el lado del libro.  
- `cascade = CascadeType.ALL` permite guardar la editorial junto con sus libros en una sola operación.  
- `fetch = FetchType.LAZY` es el comportamiento recomendado: los libros no se cargan hasta que se solicitan explícitamente.

### Ejemplo práctico

```java
Editorial editorial = new Editorial(null, "Planeta", "España", new ArrayList<>());
Libro libro1 = Libro.builder().titulo("Libro A").autor("Autor A").precio(15.0).disponible(true).editorial(editorial).build();
Libro libro2 = Libro.builder().titulo("Libro B").autor("Autor B").precio(20.0).disponible(true).editorial(editorial).build();

editorial.getLibros().add(libro1);
editorial.getLibros().add(libro2);

try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();
    session.persist(editorial);
    tx.commit();
}
```

Esto insertará una editorial y sus libros en cascada.

---

## Relación ManyToMany

Una relación **muchos a muchos (N:N)** se da cuando varias instancias de una entidad pueden estar asociadas con varias de otra.  
Por ejemplo, un libro puede tener varios autores, y un autor puede haber escrito varios libros.

### Ejemplo: `Libro` ↔ `Autor`

#### En la clase `Libro`:

```java
@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JoinTable(
    name = "libro_autor",
    joinColumns = @JoinColumn(name = "libro_id"),
    inverseJoinColumns = @JoinColumn(name = "autor_id")
)
private Set<Autor> autores = new HashSet<>();
```

#### En la clase `Autor`:

```java
@ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
private Set<Libro> libros = new HashSet<>();
```

### Explicación

- Hibernate crea automáticamente una tabla intermedia (`libro_autor`).  
- `joinColumns` define la clave del lado actual (`libro_id`).  
- `inverseJoinColumns` define la clave de la entidad relacionada (`autor_id`).  
- `fetch = FetchType.LAZY` evita cargar todos los autores hasta que se necesiten.

### Ejemplo práctico

```java
Autor autor1 = new Autor(null, "Gabriel García Márquez", new HashSet<>());
Autor autor2 = new Autor(null, "Mario Vargas Llosa", new HashSet<>());

Libro libro = Libro.builder()
    .titulo("Antología Latinoamericana")
    .autor("Varios")
    .precio(25.0)
    .disponible(true)
    .build();

libro.getAutores().add(autor1);
libro.getAutores().add(autor2);
autor1.getLibros().add(libro);
autor2.getLibros().add(libro);

try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();
    session.persist(libro);
    tx.commit();
}
```

---

## Lazy vs Eager Loading

El atributo `fetch` controla **cuándo se cargan las relaciones**.

| Tipo | Descripción | Cuándo usar |
|------|--------------|-------------|
| `LAZY` | Carga diferida: los datos se obtienen solo cuando se accede a la relación. | Recomendado por defecto; mejora el rendimiento. |
| `EAGER` | Carga inmediata: se obtienen todas las relaciones junto con la entidad principal. | Útil si siempre se van a usar las relaciones. |

### Ejemplo de carga LAZY

```java
Editorial e = session.get(Editorial.class, 1L);
// No se cargan los libros aún.
System.out.println(e.getNombre());
// Al acceder a la lista, Hibernate ejecuta una consulta adicional.
e.getLibros().forEach(System.out::println);
```

### Ejemplo de carga EAGER

```java
@OneToMany(mappedBy = "editorial", fetch = FetchType.EAGER)
private List<Libro> libros;
```
En este caso, todos los libros se cargan al obtener la editorial, lo cual puede ser costoso si hay muchas filas.

---

## Buenas prácticas con `fetch` y `cascade`

- Usa **`fetch = FetchType.LAZY`** en casi todos los casos, excepto cuando realmente necesites la información inmediatamente.  
- Usa **`cascade = CascadeType.ALL`** cuando quieras que las operaciones (guardar, eliminar, actualizar) se propaguen automáticamente.  
- Evita `EAGER` en colecciones grandes: puede generar sobrecarga y problemas de rendimiento.  
- Usa `JOIN FETCH` en HQL o Criteria cuando necesites cargar relaciones de forma controlada.

Ejemplo:

```java
String hql = "FROM Editorial e JOIN FETCH e.libros WHERE e.id = :id";
Editorial editorial = session.createQuery(hql, Editorial.class)
                             .setParameter("id", 1L)
                             .uniqueResult();
```

---

## Tabla resumen de relaciones

| Relación | Anotaciones principales | Clave foránea | Tipo de colección | Fetch recomendado |
|-----------|--------------------------|----------------|-------------------|-------------------|
| OneToOne | `@OneToOne`, `@JoinColumn` | En una tabla | Objeto simple | LAZY |
| OneToMany / ManyToOne | `@OneToMany`, `@ManyToOne`, `@JoinColumn` | En el lado *many* | `List` o `Set` | LAZY |
| ManyToMany | `@ManyToMany`, `@JoinTable` | Tabla intermedia | `Set` o `List` | LAZY |

---

## Mini ejemplo completo

```java
Editorial planeta = new Editorial(null, "Planeta", "España", new ArrayList<>());
Autor autor = new Autor(null, "Isabel Allende", new HashSet<>());

Libro libro = Libro.builder()
    .titulo("La casa de los espíritus")
    .autor("Isabel Allende")
    .precio(19.99)
    .disponible(true)
    .editorial(planeta)
    .build();

libro.getAutores().add(autor);
planeta.getLibros().add(libro);
autor.getLibros().add(libro);

try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();
    session.persist(planeta);
    tx.commit();
}
```

Este ejemplo guarda la editorial, el libro y el autor en cascada, y crea automáticamente las relaciones correspondientes.

---

## Conclusión

Hibernate simplifica el trabajo con relaciones entre entidades, convirtiendo las claves foráneas y tablas intermedias en referencias de objetos Java.  
Comprender cómo funcionan `mappedBy`, `cascade` y `fetch` es fundamental para diseñar aplicaciones eficientes y evitar problemas de rendimiento o inconsistencias en los datos.
