# Iteración 1 — Arranque del proyecto, JPA y Lombok

A continuación se muestra **todo el código** necesario para crear el proyecto base con Java 21, Spring Boot 3.4.0, Lombok y PostgreSQL. Las rutas de archivo están indicadas antes de cada bloque.

---

## 1. Crear proyecto Maven en NetBeans
- **Archivo → Nuevo Proyecto → Maven → Proyecto Java simple**
- GroupId: `com.avante.demo`  
- ArtifactId: `springboot-clientes`  
- Version: `1.0-SNAPSHOT`

Genera la estructura:
```
src/
 └─ main/
     ├─ java/
     └─ resources/
pom.xml
```

---

## 2. `pom.xml` — **ruta:** `/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <groupId>com.avante.demo</groupId>
  <artifactId>springboot-clientes</artifactId>
  <version>1.0-SNAPSHOT</version>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.0</version>
    <relativePath/>
  </parent>

  <properties>
    <java.version>21</java.version>
  </properties>

  <dependencies>
    <!-- Web (controladores y servidor embebido) -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JPA / Hibernate -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Motor de plantillas Thymeleaf -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Driver PostgreSQL -->
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.34</version>
      <scope>provided</scope>
    </dependency>

    <!-- (Opcional) tests -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>

      <!-- Compilador con annotation processor de Lombok -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <source>21</source>
          <target>21</target>
          <annotationProcessorPaths>
            <path>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <version>1.18.34</version>
            </path>
          </annotationProcessorPaths>
          <compilerArgs>
            <arg>-parameters</arg>
          </compilerArgs>
        </configuration>
      </plugin>
    </plugins>
  </build>

</project>
```

---

## 3. Configuración de la BD — **ruta:** `/src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/academia
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
server.port=8080
```

---

## 4. Clase de arranque — **ruta:** `/src/main/java/com/avante/demo/SpringbootClientesApplication.java`
```java
package com.avante.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringbootClientesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootClientesApplication.class, args);
    }
}
```

**Anotaciones clave**
- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.

---

## 5. Entidades JPA con Lombok

### 5.1 Cliente — **ruta:** `/src/main/java/com/avante/demo/model/Cliente.java`
```java
package com.avante.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos;
}
```

### 5.2 Pedido — **ruta:** `/src/main/java/com/avante/demo/model/Pedido.java`
```java
package com.avante.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private Double total;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
```

---

## 6. Repositorios Spring Data

### 6.1 ClienteRepository — **ruta:** `/src/main/java/com/avante/demo/repository/ClienteRepository.java`
```java
package com.avante.demo.repository;

import com.avante.demo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNombreContainingIgnoreCase(String texto);
}
```

### 6.2 PedidoRepository — **ruta:** `/src/main/java/com/avante/demo/repository/PedidoRepository.java`
```java
package com.avante.demo.repository;

import com.avante.demo.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> { }
```

---

## 7. Carga de datos al arrancar (CommandLineRunner) — **ruta:** `/src/main/java/com/avante/demo/config/DataLoader.java`
```java
package com.avante.demo.config;

import com.avante.demo.model.Cliente;
import com.avante.demo.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final ClienteRepository repo;

    public DataLoader(ClienteRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(Cliente.builder().nombre("Juan Pérez").email("juan@correo.com").build());
            repo.save(Cliente.builder().nombre("Ana López").email("ana@correo.com").build());
        }
    }
}
```

**Qué es `CommandLineRunner`**  
Un *hook* que ejecuta su `run(...)` automática y sincrónicamente tras iniciar Spring Boot; útil para datos iniciales, verificaciones, etc.
