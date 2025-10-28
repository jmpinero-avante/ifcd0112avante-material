# Guía completa de Hibernate con Java, Lombok y PostgreSQL

## Descripción general

Esta guía introduce paso a paso el uso de **Hibernate** como framework de mapeo objeto-relacional (ORM) en Java.  

El dominio de ejemplo utilizado es una pequeña base de datos de **libros y editoriales**, con la que se muestran todas las operaciones básicas de Hibernate: configuración, mapeo de entidades, consultas HQL, Criteria, SQL nativo y buenas prácticas de manejo de transacciones.

---

## Estructura de la guía

| Capítulo | Contenido |
|-----------|------------|
| [00 - Configuración de la base de datos](00-base-datos-h2-postgresql.md) | Preparación de la base de datos PostgreSQL y alternativa H2 en memoria. |
| [01 - Introducción y configuración de Hibernate](01-introduccion-configuracion.md) | Qué es Hibernate, dependencias, configuración de conexión y Log4j2. |
| [02 - Mapeo y operaciones CRUD básicas](02-mapeo-y-crud.md) | Creación de entidades con Lombok y operaciones CRUD iniciales. |
| [03 - Repositorios, DAO y consultas HQL / SQL](03-repositorios-y-hql.md) | Repositorios, diferencias entre DAO y Repositorio, consultas personalizadas, HQL y SQL nativo. |
| [04 - Relaciones entre entidades](04-relaciones-entre-entidades.md) | Relaciones entre entidades, una a una, muchas a una, muchas a muchas. |
| [05 - Resumen y recetas prácticas](05-resumen-y-recetas.md) | Tablas resumen, buenas prácticas y patrón de operaciones seguras. |
| [06 - Consultas con Criteria API](06-consultas-criteria.md) | Consultas dinámicas y tipadas con Criteria API, uso en repositorios y comparativa con HQL. |

---

## Requisitos previos

- Java 17 o superior instalado.  
- Maven configurado en el sistema.  
- PostgreSQL en ejecución o acceso a H2 en memoria.  
- Un IDE como IntelliJ, Eclipse o NetBeans.

---

## Cómo usar esta guía

1. Crea un proyecto Maven vacío.  
2. Añade las dependencias que se indican en el capítulo 1.  
3. Crea las tablas siguiendo el capítulo 0.  
4. Copia el código de las entidades y repositorios paso a paso.  
5. Ejecuta los ejemplos desde la clase `App.java`.  
6. Consulta el capítulo 5 para aprender a realizar búsquedas dinámicas con Criteria.

