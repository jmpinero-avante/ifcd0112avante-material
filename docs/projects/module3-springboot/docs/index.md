# Aplicación web con Spring Boot

Esta documentación integra, en un itinerario didáctico único, todo lo necesario para construir una aplicación con **Spring Boot 3.4.0**, **Java 21**, **Hibernate/JPA**, **Thymeleaf** y **PostgreSQL**, usando como ejemplo la **gestión de clientes y pedidos**. No hay tareas ni evaluaciones: es una guía para **entender** y **construir**. Todo el código aparece completo, con rutas de archivo claras para reproducirlo en NetBeans.

## Objetivos
- Comprender HTTP y REST (métodos, cabeceras, cuerpos, tipos de contenido).
- Crear y configurar un proyecto Spring Boot (Maven) con Lombok y PostgreSQL.
- Modelar entidades JPA y repositorios Spring Data.
- Desarrollar una aplicación **MVC** con Thymeleaf.
- Exponer una **API REST JSON**.

## Índice
1. [Teoría HTTP y REST](010-teoria-http-rest.md)
2. [Iteración 1 — Arranque del proyecto, JPA, Lombok, PostgreSQL](020-iteracion-1-arranque-proyecto.md)
3. [Iteración 2 — MVC con Thymeleaf (CRUD de clientes)](030-iteracion-2-mvc-thymeleaf.md)
4. [Iteración 3 — API REST (CRUD JSON)](040-iteracion-3-api-rest.md)
5. [Thymeleaf avanzado](050-thymeleaf-avanzado.md)
6. [Anexos: Lombok y plugins](060-anexos-lombok-y-plugins.md)

## Prerrequisitos
- **Java 21 (JDK 21)**, **Maven 3.9+**, **NetBeans 19/20+**
- **PostgreSQL 14+** y una BD, por ejemplo `academia`
