# Aplicación web con Spring Boot

Esta documentación unifica, en un único itinerario didáctico y progresivo, todo lo necesario para construir una aplicación web completa con **Spring Boot 3.4.0**, **Java 21**, **Hibernate/JPA**, **Thymeleaf** y **PostgreSQL**. El ejemplo práctico es la **gestión de clientes y pedidos**. No incluye tareas ni entregables; está pensada para **explicar y construir**, con **código completo** y comentado.

## Objetivos
- Comprender HTTP y REST (métodos, cabeceras, cuerpos, tipos de contenido).
- Crear y configurar un proyecto Spring Boot (NetBeans + Maven), con Lombok y PostgreSQL.
- Modelar entidades JPA (Cliente, Pedido) y repositorios Spring Data.
- Desarrollar una aplicación **MVC con Thymeleaf** (listado y formularios).
- Exponer una **API REST JSON** con Spring Web y probarla con Postman/curl.

## Índice
1. [Teoría HTTP y REST (GET/POST, JSON, x-www-form-urlencoded, multipart)](010-teoria-http-rest.md)
2. [Iteración 1 — Arranque del proyecto, JPA, Lombok, conexión a PostgreSQL](020-iteracion-1-arranque-proyecto.md)
3. [Iteración 2 — MVC con Thymeleaf (CRUD de clientes)](030-iteracion-2-mvc-thymeleaf.md)
4. [Iteración 3 — API REST (CRUD JSON)](040-iteracion-3-api-rest.md)
5. [Thymeleaf avanzado (fragmentos, layouts, validación, utilidades)](050-thymeleaf-avanzado.md)
6. [Anexos (Lombok, maven-compiler-plugin, alternativas y buenas prácticas)](060-anexos-lombok-y-plugins.md)

## Prerrequisitos técnicos
- **Java 21** (JDK 21)
- **Apache NetBeans** (19/20 o superior)
- **Maven 3.9+**
- **PostgreSQL** 14+ y una base de datos por ejemplo `academia`
