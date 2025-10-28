# Spring Boot + JdbcTemplate

Migramos a **Spring Boot** para simplificar la configuración.

## Dependencias
- `spring-boot-starter-web`
- `spring-boot-starter-jdbc`
- `postgresql`

## Controlador REST
```java
@RestController
@RequestMapping("/empleados")
public class EmpleadoController {
    @Autowired
    private JdbcTemplate jdbc;

    @GetMapping
    public List<Empleado> listar() {
        return jdbc.query("SELECT * FROM empleados",
            (rs,i)-> new Empleado(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("salario")));
    }
}
```

💡 Ahora la vista es JSON: `GET /empleados` devuelve los empleados en formato JSON.
