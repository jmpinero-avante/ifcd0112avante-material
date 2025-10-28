# Arranque del proyecto Spring Boot y conexión a PostgreSQL (NetBeans)

## Objetivos
- Crear un proyecto Spring Boot en NetBeans.
- Configurar la conexión con PostgreSQL.
- Crear entidades y repositorios básicos.

## Contenidos

### 1. Creación del proyecto en NetBeans

#### Opción 1 — Usando el asistente de NetBeans
1. Archivo → Nuevo Proyecto → Maven → Proyecto Java desde Archetype.
2. Elegir arquetipo `org.springframework.boot:spring-boot-sample-simple-archetype` o proyecto Maven vacío.
3. Group Id: `com.avante.demo`, Artifact Id: `springboot-clientes`.
4. Finalizar el asistente.

#### Opción 2 — Usando Spring Initializr
1. Ir a https://start.spring.io
2. Configurar:
   - Project: Maven
   - Language: Java
   - Spring Boot: 3.x
   - Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Thymeleaf
3. Descargar el ZIP y abrirlo desde NetBeans.

### 2. Configuración de `pom.xml`
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

### 3. Configuración de `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/academia
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
server.port=8080
```

### 4. Entidades base
Ejemplo: Cliente y Pedido con relación OneToMany / ManyToOne.

### 5. Repositorios
Interfaces que extienden `JpaRepository` para Cliente y Pedido.

### 6. Ejecución
Ejecutar el proyecto con **Run** en NetBeans y comprobar la conexión en consola.

### Actividad práctica
Crear un `CommandLineRunner` para insertar datos de prueba en la base de datos.
