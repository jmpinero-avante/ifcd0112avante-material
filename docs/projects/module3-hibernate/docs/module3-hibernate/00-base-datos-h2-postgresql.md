# Configuración de la base de datos

## Introducción

Antes de empezar a trabajar con Hibernate, necesitamos un entorno de base de datos sobre el que realizar nuestras pruebas.  
En esta guía utilizaremos **PostgreSQL** como base de datos principal, aunque también veremos cómo usar **H2**, una base de datos en memoria ideal para desarrollo y pruebas rápidas.

La idea es que, desde el primer momento, nuestro proyecto pueda funcionar tanto con una base de datos real (PostgreSQL) como con una temporal (H2), sin necesidad de cambiar el código Java.  
Solo será necesario ajustar la configuración del archivo `hibernate.cfg.xml`.

---

## Creación de la base de datos en PostgreSQL

### 1. Crear la base de datos

Abrimos la consola de PostgreSQL (`psql`) y ejecutamos:

```sql
CREATE DATABASE biblioteca;
```

Podemos comprobar que se ha creado correctamente con:

```sql
\l
```

y entrar en ella con:

```sql
\c biblioteca;
```

---

## Tablas del proyecto

A continuación crearemos las tablas que usaremos en toda la guía:  
`editoriales` y `libros`.

---

### Tabla `editoriales`

```sql
CREATE TABLE editoriales (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    pais VARCHAR(60) NOT NULL
);
```

Esta tabla representa a las editoriales que publican los libros.  
Es sencilla pero suficiente para establecer relaciones con la tabla `libros` más adelante.

---

### Tabla `libros`

```sql
CREATE TABLE libros (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    precio NUMERIC(6,2) NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_publicacion DATE NOT NULL,
    editorial_id INTEGER,
    CONSTRAINT fk_editorial
        FOREIGN KEY (editorial_id)
        REFERENCES editoriales (id)
        ON DELETE SET NULL
);
```

#### Explicación de los campos:

- `id`: clave primaria autoincremental.
- `titulo`, `autor`, `precio`: información básica del libro.
- `disponible`: nos permitirá practicar consultas booleanas en HQL y SQL.
- `fecha_publicacion`: será útil para consultas con condiciones de rango de fechas.
- `editorial_id`: clave foránea que enlaza cada libro con su editorial.

---

## Alternativa: base de datos H2 en memoria

Durante el desarrollo, es muy común querer probar el código sin tener que depender de un servidor PostgreSQL activo.  
Para eso podemos usar **H2**, una base de datos en memoria que se borra automáticamente al cerrar la aplicación.

### Configuración en `hibernate.cfg.xml`

Podemos sustituir la configuración de PostgreSQL por esta:

```xml
<hibernate-configuration>
    <session-factory>
        <property name="hibernate.connection.driver_class">org.h2.Driver</property>
        <property name="hibernate.connection.url">jdbc:h2:mem:biblioteca;DB_CLOSE_DELAY=-1</property>
        <property name="hibernate.dialect">org.hibernate.dialect.H2Dialect</property>
        <property name="hibernate.hbm2ddl.auto">create-drop</property>
        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>
    </session-factory>
</hibernate-configuration>
```

Con esta configuración, Hibernate creará automáticamente las tablas en memoria cada vez que se ejecute la aplicación y las eliminará al cerrarse.

Esto es ideal para:

- Ejecutar pruebas rápidas sin instalar PostgreSQL.
- Hacer demostraciones en clase.
- Automatizar pruebas unitarias de mapeo o consultas.

---

## Cambiar entre PostgreSQL y H2

Una buena práctica es mantener **dos archivos de configuración**:

- `hibernate-postgres.cfg.xml`
- `hibernate-h2.cfg.xml`

Y elegir cuál usar según el entorno.  
En desarrollo podemos usar H2, y en despliegue o prácticas reales, PostgreSQL.

---

Con esto tenemos todo preparado para comenzar la guía con Hibernate.
