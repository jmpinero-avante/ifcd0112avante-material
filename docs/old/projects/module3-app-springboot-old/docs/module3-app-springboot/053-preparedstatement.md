# PreparedStatement e inserciones

Usamos **PreparedStatement** para prevenir inyecciones SQL y mejorar el rendimiento en las consultas a la base de datos.  
En esta versión, reutilizamos la clase **ConexionBD**, que implementa el patrón **Singleton**, para obtener la conexión de forma centralizada.

---

## Objetivo

- Comprender el uso del objeto `PreparedStatement` de JDBC.  
- Prevenir vulnerabilidades de **inyección SQL**.  
- Reutilizar sentencias SQL con parámetros dinámicos.  
- Aplicar la conexión centralizada a través de la clase `ConexionBD`.  
- Realizar operaciones **INSERT**, **SELECT filtrado**, **UPDATE** y **DELETE** directamente desde la clase principal.

---

## Contexto

En la iteración anterior creamos la clase `ConexionBD` con patrón **Singleton**, de modo que toda la aplicación comparte la misma conexión a la base de datos:

```java
Connection conn = ConexionBD.getInstancia().getConexion();
```

Hasta ahora, las consultas se ejecutaban con `Statement`, concatenando variables al texto SQL:

```java
String sql = "SELECT * FROM empleados WHERE nombre = '" + nombre + "'";
Statement st = conn.createStatement();
ResultSet rs = st.executeQuery(sql);
```

Este método es **inseguro y poco eficiente**.  
Con `PreparedStatement` definimos consultas con **parámetros seguros (`?`)** y asignamos los valores posteriormente.

---

## Ejemplo completo con `ConexionBD`

En este ejemplo, toda la lógica sigue dentro de `App.java`, pero ya se usa la conexión gestionada por el Singleton `ConexionBD`.

```java
import db.ConexionBD;
import java.sql.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             Scanner sc = new Scanner(System.in)) {

            System.out.println("1. Insertar empleado");
            System.out.println("2. Buscar por nombre");
            System.out.println("3. Actualizar salario");
            System.out.println("4. Eliminar empleado");
            System.out.println("5. Listar todos");
            System.out.print("Elige una opción: ");
            int opcion = sc.nextInt();
            sc.nextLine(); // limpiar buffer

            switch (opcion) {
                case 1 -> {
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();
                    System.out.print("Salario: ");
                    double salario = sc.nextDouble();

                    String sql = "INSERT INTO empleados (nombre, salario) VALUES (?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, nombre);
                        ps.setDouble(2, salario);
                        ps.executeUpdate();
                        System.out.println("Empleado insertado correctamente.");
                    }
                }

                case 2 -> {
                    System.out.print("Buscar nombre: ");
                    String nombre = sc.nextLine();
                    String sql = "SELECT id, nombre, salario FROM empleados WHERE nombre ILIKE ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, "%" + nombre + "%");
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            System.out.printf("%d - %s - %.2f €%n",
                                    rs.getInt("id"),
                                    rs.getString("nombre"),
                                    rs.getDouble("salario"));
                        }
                    }
                }

                case 3 -> {
                    System.out.print("ID del empleado: ");
                    int id = sc.nextInt();
                    System.out.print("Nuevo salario: ");
                    double salario = sc.nextDouble();

                    String sql = "UPDATE empleados SET salario = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setDouble(1, salario);
                        ps.setInt(2, id);
                        int filas = ps.executeUpdate();
                        System.out.println(filas > 0 ? "Salario actualizado." : "Empleado no encontrado.");
                    }
                }

                case 4 -> {
                    System.out.print("ID del empleado: ");
                    int id = sc.nextInt();
                    String sql = "DELETE FROM empleados WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, id);
                        int filas = ps.executeUpdate();
                        System.out.println(filas > 0 ? "Empleado eliminado." : "Empleado no encontrado.");
                    }
                }

                case 5 -> {
                    String sql = "SELECT id, nombre, salario FROM empleados";
                    try (PreparedStatement ps = conn.prepareStatement(sql);
                         ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            System.out.printf("%d - %s - %.2f €%n",
                                    rs.getInt("id"),
                                    rs.getString("nombre"),
                                    rs.getDouble("salario"));
                        }
                    }
                }

                default -> System.out.println("Opción no válida.");
            }

        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
        }
    }
}
```

---

## Ejercicios prácticos

1. **Modificar el programa** para que el menú se repita hasta que el usuario elija “Salir”.  
2. Añadir una opción para **mostrar los empleados con salario superior a un valor introducido**.  
3. Mostrar un mensaje si una consulta no devuelve resultados.  
4. Probar a introducir comillas u órdenes SQL maliciosas y comprobar que el programa **no se ve afectado**.  

---

## Beneficios de `PreparedStatement`

| Ventaja | Explicación |
|----------|-------------|
| **Seguridad** | Los parámetros se envían por separado, evitando inyecciones SQL. |
| **Rendimiento** | Las sentencias se precompilan y pueden reutilizarse. |
| **Legibilidad** | Se evita la concatenación de cadenas con comillas y operadores. |
| **Compatibilidad** | Admite distintos tipos de datos (`int`, `String`, `double`, `Date`, etc.). |

---

## Próximos pasos

En la siguiente unidad reorganizaremos el código creando clases dedicadas:  
- Una clase `Empleado` que representará los datos.  
- Una clase `EmpleadoDAO` que gestionará las operaciones de base de datos.  

Esto nos permitirá empezar a aplicar el **patrón MVC** y separar responsabilidades en nuestra aplicación.
