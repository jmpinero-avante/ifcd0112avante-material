# JDBC y SELECT simple

Conectamos Java con PostgreSQL usando **JDBC** (API de acceso a bases de datos).

## Conceptos clave
- `DriverManager` administra conexiones.
- `Connection` representa una sesión con la base de datos.
- `Statement` ejecuta SQL.
- `ResultSet` gestiona los resultados.

## Ejemplo básico
```java
Connection conn = DriverManager.getConnection(
    "jdbc:postgresql://localhost:5432/empresa", "postgres", "postgres"
);
Statement st = conn.createStatement();
ResultSet rs = st.executeQuery("SELECT * FROM empleados");
while(rs.next()) {
    System.out.println(rs.getString("nombre"));
}
```
