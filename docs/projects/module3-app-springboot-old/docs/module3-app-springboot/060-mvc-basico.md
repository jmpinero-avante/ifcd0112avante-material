# MVC básico (consola)

Aplicamos MVC en una app de consola.

## Estructura
- **Modelo:** `Empleado.java`
- **Vista:** imprime la lista de empleados.
- **Controlador:** obtiene datos desde la base de datos.

```java
public class EmpleadoController {
    public List<Empleado> listar() {
        // consulta SELECT * FROM empleados
    }
}
```

## 1. Qué es el patrón MVC

El patrón **MVC (Model–View–Controller)** es una arquitectura de software que separa la lógica de negocio, la presentación y el control de flujo.  
Su objetivo es desacoplar las responsabilidades para mejorar la organización, mantenibilidad y escalabilidad del código.

| Capa | Responsabilidad principal | Ejemplo en nuestra app |
|------|---------------------------|-------------------------|
| **Modelo (Model)** | Representa los datos y las reglas de negocio. | La clase `Empleado` y las clases de conexión JDBC (`ConexionBD`, `EmpleadoRepository`). |
| **Vista (View)** | Interactúa con el usuario, mostrando la información y recibiendo sus acciones. | Una interfaz de consola, menús de texto o, más adelante, una interfaz web o Swing/JavaFX. |
| **Controlador (Controller)** | Coordina la interacción entre la Vista y el Modelo. Recibe peticiones, las interpreta y actualiza los datos o la vista. | La clase `EmpleadoController`, que decide qué operación ejecutar según la opción del usuario. |

---

## 2. Estructura general del proyecto MVC con JDBC

```
src/
 ├─ model/
 │   ├─ Empleado.java
 │   ├─ ConexionBD.java
 │   └─ EmpleadoRepository.java
 ├─ controller/
 │   └─ EmpleadoController.java
 ├─ view/
 │   └─ ConsolaView.java
 └─ App.java
```

### model/Empleado.java
POJO (Plain Old Java Object) que representa una fila de la tabla `empleados`.

```java
package model;

public class Empleado {
    private int id;
    private String nombre;
    private double salario;

    // Constructores
    public Empleado(int id, String nombre, double salario) {
        this.id = id;
        this.nombre = nombre;
        this.salario = salario;
    }

    // Getters y setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getSalario() { return salario; }

    @Override
    public String toString() {
        return String.format("%d - %s - %.2f €", id, nombre, salario);
    }
}
```

---

### model/ConexionBD.java
Singleton responsable de abrir y cerrar la conexión a la base de datos.

```java
package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class ConexionBD {
    private static ConexionBD instancia;
    private Connection conexion;

    private ConexionBD() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String usuario = props.getProperty("db.user");
            String clave = props.getProperty("db.password");

            conexion = DriverManager.getConnection(url, usuario, clave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConexionBD getInstancia() {
        if (instancia == null) instancia = new ConexionBD();
        return instancia;
    }

    public Connection getConexion() { return conexion; }
}
```

---

### model/EmpleadoRepository.java
Encapsula las operaciones CRUD mediante JDBC.

```java
package model;

import java.sql.*;
import java.util.*;

public class EmpleadoRepository {

    public List<Empleado> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, salario FROM empleados";
        Connection conn = ConexionBD.getInstancia().getConexion();
        List<Empleado> lista = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Empleado(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getDouble("salario")
                ));
            }
        }
        return lista;
    }

    public void insertar(Empleado e) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, salario) VALUES (?, ?)";
        Connection conn = ConexionBD.getInstancia().getConexion();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setDouble(2, e.getSalario());
            ps.executeUpdate();
        }
    }
}
```

---

### view/ConsolaView.java
La interfaz textual que muestra menús y recoge la entrada del usuario.

```java
package view;

import java.util.List;
import java.util.Scanner;
import model.Empleado;

public class ConsolaView {
    private Scanner sc = new Scanner(System.in);

    public int mostrarMenu() {
        System.out.println("\n--- Menú Empleados ---");
        System.out.println("1. Listar empleados");
        System.out.println("2. Insertar empleado");
        System.out.println("0. Salir");
        System.out.print("Elige una opción: ");
        return sc.nextInt();
    }

    public Empleado pedirEmpleado() {
        sc.nextLine(); // limpiar buffer
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Salario: ");
        double salario = sc.nextDouble();
        return new Empleado(0, nombre, salario);
    }

    public void mostrarEmpleados(List<Empleado> empleados) {
        System.out.println("\nListado de empleados:");
        empleados.forEach(System.out::println);
    }
}
```

---

### controller/EmpleadoController.java
El “director de orquesta” que une la vista y el modelo.

```java
package controller;

import model.*;
import view.*;
import java.sql.SQLException;

public class EmpleadoController {
    private final ConsolaView vista;
    private final EmpleadoRepository repo;

    public EmpleadoController() {
        vista = new ConsolaView();
        repo = new EmpleadoRepository();
    }

    public void iniciar() {
        int opcion;
        do {
            opcion = vista.mostrarMenu();
            try {
                switch (opcion) {
                    case 1 -> vista.mostrarEmpleados(repo.listarTodos());
                    case 2 -> repo.insertar(vista.pedirEmpleado());
                    case 0 -> System.out.println("Saliendo...");
                    default -> System.out.println("Opción no válida");
                }
            } catch (SQLException e) {
                System.err.println("Error en BD: " + e.getMessage());
            }
        } while (opcion != 0);
    }
}
```

---

### App.java
Punto de entrada del programa.

```java
import controller.EmpleadoController;

public class App {
    public static void main(String[] args) {
        new EmpleadoController().iniciar();
    }
}
```

---

## 3. Flujo de datos MVC con JDBC

```
Usuario
  ↓
ConsolaView (Vista)
  ↓
EmpleadoController (Controlador)
  ↓
EmpleadoRepository → ConexionBD (Modelo)
  ↓
Base de Datos (PostgreSQL)
```

- El usuario elige una acción.  
- El controlador interpreta esa acción.  
- El modelo ejecuta la operación correspondiente (consulta SQL, inserción, etc.).  
- El controlador recibe los resultados y los envía de vuelta a la vista.  
- La vista los muestra en pantalla.

---

## 4. Ventajas del patrón MVC en el proyecto

1. Separación clara de responsabilidades: facilita la depuración y el mantenimiento.  
2. Reutilización de código: el modelo puede usarse en una app web o escritorio sin tocar la lógica.  
3. Escalabilidad: permite sustituir la vista de consola por una interfaz web o gráfica sin cambiar la lógica.  
4. Facilidad para pruebas unitarias: cada capa puede probarse de manera independiente.
