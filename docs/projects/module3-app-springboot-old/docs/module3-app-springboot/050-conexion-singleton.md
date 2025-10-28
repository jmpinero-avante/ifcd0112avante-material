# Conexión JDBC centralizada con Singleton y fichero `.properties`

## Objetivo

- Evitar repetir el código de conexión en cada clase.  
- Externalizar los parámetros de conexión a un fichero de configuración.  
- Asegurar que toda la aplicación use una única conexión compartida mediante un patrón **Singleton**.

---

## Estructura del proyecto

```
src/
 ├─ db/
 │   └─ ConexionBD.java
 ├─ app/
 │   └─ App.java
 └─ resources/
     └─ config.properties
```

---

## 1. Fichero de propiedades (`resources/config.properties`)

```properties
db.url=jdbc:postgresql://localhost:5432/empresa
db.user=postgres
db.password=postgres
```

> Este archivo debe estar en el **classpath** del proyecto (por ejemplo, dentro de `src/main/resources` si usas Maven).

---

## 2. Clase Singleton `ConexionBD.java`

```java
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class ConexionBD {
    // Instancia única
    private static ConexionBD instancia;
    private Connection conexion;

    // Constructor privado
    private ConexionBD() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            conexion = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión establecida correctamente.");

        } catch (IOException e) {
            System.err.println("Error al leer el archivo de configuración: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    // Método de acceso público y estático
    public static ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }
}
```

---

## 3. Clase principal `App.java`

```java
package app;

import db.ConexionBD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class App {
    public static void main(String[] args) {
        try {
            Connection conn = ConexionBD.getInstancia().getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nombre, salario FROM empleados");

            while (rs.next()) {
                System.out.printf("%d - %s - %.2f%n",
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getDouble("salario"));
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

---

## 4. Explicación de la mejora

| Concepto | Qué aporta |
|-----------|-------------|
| **Singleton** | Garantiza que solo exista una instancia de conexión, evitando abrir múltiples conexiones innecesarias. |
| **Archivo `.properties`** | Facilita cambiar parámetros (URL, usuario, contraseña) sin recompilar el programa. |
| **Centralización** | Permite que todas las clases que necesiten conexión usen el mismo objeto `ConexionBD.getInstancia()`. |

---

## 5. Flujo de ejecución

1. Al iniciar el programa, `ConexionBD.getInstancia()` crea la conexión si no existe.  
2. Carga los parámetros desde `config.properties`.  
3. Establece la conexión con `DriverManager`.  
4. Devuelve el mismo objeto `Connection` a cualquier parte de la app.

---

## 6. Siguiente paso

A partir de esta base, ya podrás:
- Separar las responsabilidades en **Modelo, Vista y Controlador (MVC)**.
- Encapsular las operaciones CRUD en una clase `EmpleadoRepository`.
- Dejar que el controlador se encargue de coordinar la lógica de la app.
