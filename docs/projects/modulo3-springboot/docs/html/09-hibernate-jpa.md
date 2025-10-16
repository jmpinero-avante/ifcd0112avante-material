# 🏗️ Iteración 6 — Hibernate (JPA)

Introducimos JPA para automatizar la persistencia de datos.

## Entidad
```java
@Entity
@Table(name="empleados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private double salario;
}
```

## Repositorio
```java
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {}
```

💡 Con Spring Data JPA ya no escribimos SQL manualmente.
