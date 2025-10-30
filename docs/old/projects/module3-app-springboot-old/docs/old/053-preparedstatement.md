# Iteración 4 — PreparedStatement e inserciones

Usamos **PreparedStatement** para prevenir inyecciones SQL y mejorar rendimiento.

## Objetivo

- Comprender el uso del objeto `PreparedStatement` de JDBC.  
- Prevenir vulnerabilidades de **inyección SQL**.  
- Reutilizar sentencias SQL con parámetros dinámicos.  
- Implementar operaciones **INSERT**, **SELECT filtrado**, **UPDATE** y **DELETE**.

---

```java
PreparedStatement ps = conn.prepareStatement(
    "INSERT INTO empleados(nombre, salario) VALUES (?, ?)"
);
ps.setString(1, "Ana");
ps.setDouble(2, 1800);
ps.executeUpdate();
```

Ventajas:
- Seguridad contra SQL injection.
- Reutilización de consultas.
- Mejor rendimiento en ejecuciones repetidas.



## Contexto

Hasta ahora, las consultas se hacían con un `Statement`, lo que implica concatenar texto SQL con variables:

```java
String sql = "SELECT * FROM empleados WHERE nombre = '" + nombre + "'";
```

Este enfoque tiene dos problemas:

1. **Seguridad:** permite inyección SQL (por ejemplo, si el usuario introduce `' OR '1'='1`).
2. **Eficiencia:** el motor debe compilar cada consulta de nuevo.

Con `PreparedStatement`, la consulta se precompila y se pueden asignar valores **parametrizados** mediante métodos tipo `setString()`, `setInt()`, etc.

---

## Estructura del proyecto

```
src/
 ├─ db/
 │   └─ ConexionBD.java
 ├─ app/
 │   ├─ Empleado.java
 │   ├─ EmpleadoDAO.java
 │   └─ App.java
 └─ resources/
     └─ config.properties
```

---

## 1. Clase modelo `Empleado.java`

```java
package app;

public class Empleado {
    private int id;
    private String nombre;
    private double salario;

    public Empleado(int id, String nombre, double salario) {
        this.id = id;
        this.nombre = nombre;
        this.salario = salario;
    }

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

## 2. Clase `EmpleadoDAO.java`

Esta clase gestiona las operaciones CRUD usando `PreparedStatement`.

```java
package app;

import db.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    // Insertar un nuevo empleado
    public void insertar(Empleado e) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, salario) VALUES (?, ?)";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setDouble(2, e.getSalario());
            ps.executeUpdate();
        }
    }

    // Listar todos los empleados
    public List<Empleado> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, salario FROM empleados";
        List<Empleado> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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

    // Buscar por nombre (parámetro)
    public List<Empleado> buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id, nombre, salario FROM empleados WHERE nombre ILIKE ?";
        List<Empleado> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            ResultSet rs = ps.executeQuery();

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

    // Actualizar salario
    public void actualizarSalario(int id, double nuevoSalario) throws SQLException {
        String sql = "UPDATE empleados SET salario = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setDouble(1, nuevoSalario);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // Eliminar empleado
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM empleados WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
```

---

## 3. Clase `App.java`

```java
package app;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        EmpleadoDAO dao = new EmpleadoDAO();

        try {
            System.out.println("1. Insertar empleado");
            System.out.println("2. Buscar por nombre");
            System.out.println("3. Actualizar salario");
            System.out.println("4. Eliminar empleado");
            System.out.println("5. Listar todos");
            System.out.print("Opción: ");
            int opcion = sc.nextInt();
            sc.nextLine(); // limpiar buffer

            switch (opcion) {
                case 1 -> {
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();
                    System.out.print("Salario: ");
                    double salario = sc.nextDouble();
                    dao.insertar(new Empleado(0, nombre, salario));
                }
                case 2 -> {
                    System.out.print("Buscar: ");
                    String nombre = sc.nextLine();
                    List<Empleado> empleados = dao.buscarPorNombre(nombre);
                    empleados.forEach(System.out::println);
                }
                case 3 -> {
                    System.out.print("ID: ");
                    int id = sc.nextInt();
                    System.out.print("Nuevo salario: ");
                    double salario = sc.nextDouble();
                    dao.actualizarSalario(id, salario);
                }
                case 4 -> {
                    System.out.print("ID: ");
                    int id = sc.nextInt();
                    dao.eliminar(id);
                }
                case 5 -> dao.listarTodos().forEach(System.out::println);
                default -> System.out.println("Opción no válida.");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}
```

---

## 4. Beneficios del uso de `PreparedStatement`

| Ventaja | Explicación |
|----------|-------------|
| **Seguridad** | Los parámetros se envían por separado, evitando inyecciones SQL. |
| **Rendimiento** | La sentencia SQL se compila una sola vez y se reutiliza con distintos parámetros. |
| **Legibilidad** | Se evita la concatenación de cadenas largas con comillas y operadores. |
| **Compatibilidad** | Admite valores de distintos tipos (`int`, `String`, `double`, `Date`, etc.) mediante métodos `setX()`. |

---

## 5. Siguiente paso

En la **Iteración 2 (Patrón MVC)**, las operaciones del DAO (`EmpleadoDAO`) se integrarán dentro de la capa **Modelo**, mientras que el flujo del programa pasará a una clase **Controlador**, separando así la lógica de presentación (menú) del acceso a datos.
