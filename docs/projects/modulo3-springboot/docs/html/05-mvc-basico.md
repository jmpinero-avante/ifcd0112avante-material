# 🧱 Iteración 2 — MVC básico (consola)

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
