---
title: Capítulo 16 — JDBC y acceso a bases de datos
description: Explicación del modelo JDBC, configuración en Maven, uso de PreparedStatement, transacciones y ejemplo completo CRUD con PostgreSQL.
---

# Capítulo 16 — JDBC y acceso a bases de datos

El **JDBC (Java Database Connectivity)** es la API estándar de Java para conectar aplicaciones con bases de datos relacionales.  
Permite ejecutar sentencias SQL, gestionar resultados y controlar transacciones.

---

## 16.1. Configuración de Maven

Para usar PostgreSQL, es necesario incluir su **driver JDBC** en el archivo `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.1</version>
    </dependency>
</dependencies>
```

---

## 16.2. Estructura general del modelo JDBC

1. Cargar el driver (automático desde Java 6).  
2. Obtener una conexión (`Connection`).  
3. Crear una sentencia (`Statement` o `PreparedStatement`).  
4. Ejecutar consultas o actualizaciones SQL.  
5. Procesar los resultados (`ResultSet`).  
6. Cerrar los recursos.

Ejemplo mínimo:

```java
import java.sql.*;

public class ConexionSimple {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mi_base";
        String usuario = "postgres";
        String password = "1234";

        try (Connection conn = DriverManager.getConnection(url, usuario, password)) {
            System.out.println("Conexión establecida.");
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }
}
```

---

## 16.3. Uso de `PreparedStatement`

`PreparedStatement` permite ejecutar sentencias SQL parametrizadas, evitando **inyecciones SQL** y mejorando el rendimiento.

```java
String sql = "INSERT INTO alumnos (nombre, edad) VALUES (?, ?)";

try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, "Ana");
    ps.setInt(2, 22);
    ps.executeUpdate();
}
```

Ventajas:
- Previene inyecciones SQL.  
- Permite reutilizar la misma sentencia con distintos valores.  
- Optimiza la ejecución en el servidor.

---

## 16.4. El objeto `ResultSet`

Representa el conjunto de resultados devuelto por una consulta.

```java
String sql = "SELECT id, nombre, edad FROM alumnos";

try (PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {

    while (rs.next()) {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        int edad = rs.getInt("edad");

        System.out.println(id + " - " + nombre + " - " + edad);
    }
}
```

Métodos comunes:
- `next()` → avanza al siguiente registro.  
- `getInt()`, `getString()`, etc. → obtienen los valores de cada columna.  
- `close()` → libera los recursos.

---

## 16.5. Transacciones y autocommit

Por defecto, JDBC usa **autocommit activado**, lo que significa que cada operación SQL se confirma automáticamente.  
Para gestionar transacciones manualmente:

```java
conn.setAutoCommit(false);

try (PreparedStatement ps1 = conn.prepareStatement("UPDATE alumnos SET edad = edad + 1 WHERE id = ?")) {
    ps1.setInt(1, 1);
    ps1.executeUpdate();

    conn.commit(); // Confirma la transacción
} catch (SQLException e) {
    conn.rollback(); // Revierte cambios en caso de error
    System.err.println("Error en transacción: " + e.getMessage());
} finally {
    conn.setAutoCommit(true);
}
```

---

## 16.6. Clase Singleton para gestión de conexión (`ConfigDB`)

### Archivo `config.properties`

```
db.url=jdbc:postgresql://localhost:5432/mi_base
db.user=postgres
db.password=1234
```

### Clase `ConfigDB`

```java
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class ConfigDB {
    private static ConfigDB instancia;
    private static Properties config = new Properties();

    private ConfigDB() {
        try (InputStream entrada = ConfigDB.class.getResourceAsStream("/config.properties")) {
            if (entrada == null)
                throw new FileNotFoundException("Archivo config.properties no encontrado.");
            config.load(entrada);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar configuración: " + e.getMessage());
        }
    }

    public static synchronized ConfigDB getInstance() {
        if (instancia == null) instancia = new ConfigDB();
        return instancia;
    }

    private Connection getNewConnection() throws SQLException {
        return DriverManager.getConnection(
            config.getProperty("db.url"),
            config.getProperty("db.user"),
            config.getProperty("db.password")
        );
    }

    public static Connection newConnection() throws SQLException {
        return getInstance().getNewConnection();
    }
}
```

Uso:

```java
try (Connection conn = ConfigDB.newConnection()) {
    System.out.println("Conexión exitosa.");
}
```

---

## 16.7. Ejemplo completo CRUD (tabla `alumnos`)

### SQL de la tabla

```sql
CREATE TABLE alumnos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    edad INT NOT NULL
);
```

### Clase `AppAlumnos`

```java
import java.sql.*;

public class AppAlumnos {
    public static void main(String[] args) {
        insertarAlumno("Ana", 22);
        listarAlumnos();
        actualizarAlumno(1, "Ana López", 23);
        eliminarAlumno(1);
    }

    static void insertarAlumno(String nombre, int edad) {
        String sql = "INSERT INTO alumnos (nombre, edad) VALUES (?, ?)";
        try (Connection conn = ConfigDB.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, edad);
            ps.executeUpdate();
            System.out.println("Alumno insertado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al insertar: " + e.getMessage());
        }
    }

    static void listarAlumnos() {
        String sql = "SELECT id, nombre, edad FROM alumnos";
        try (Connection conn = ConfigDB.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("Listado de alumnos:");
            while (rs.next()) {
                System.out.printf("%d - %s (%d años)%n",
                    rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar: " + e.getMessage());
        }
    }

    static void actualizarAlumno(int id, String nuevoNombre, int nuevaEdad) {
        String sql = "UPDATE alumnos SET nombre = ?, edad = ? WHERE id = ?";
        try (Connection conn = ConfigDB.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoNombre);
            ps.setInt(2, nuevaEdad);
            ps.setInt(3, id);
            ps.executeUpdate();
            System.out.println("Alumno actualizado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
        }
    }

    static void eliminarAlumno(int id) {
        String sql = "DELETE FROM alumnos WHERE id = ?";
        try (Connection conn = ConfigDB.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Alumno eliminado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
        }
    }
}
```

---

## 16.8. Referencia al patrón DAO

El código anterior es funcional, pero no escalable.  
En proyectos más grandes se recomienda aplicar el **patrón DAO (Data Access Object)**, que separa la lógica de acceso a datos de la lógica de negocio.  
Ese patrón se explica en detalle en el capítulo de **Patrones de diseño**.

---

## 16.9. Resumen

- JDBC permite conectar aplicaciones Java con bases de datos relacionales.  
- `PreparedStatement` evita inyecciones SQL y mejora el rendimiento.  
- `ResultSet` gestiona los resultados de las consultas.  
- Las transacciones permiten controlar confirmaciones y revertir cambios.  
- El patrón Singleton facilita la gestión centralizada de conexiones.  
- Los CRUD deben evolucionar hacia un modelo DAO en aplicaciones complejas.
