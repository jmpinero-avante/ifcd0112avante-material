# Validaciones y transacciones

Agregamos validaciones y transacciones automáticas con Spring.

## Validaciones
```java
@Data
public class Empleado {
    @NotBlank private String nombre;
    @PositiveOrZero private double salario;
}
```

## Transacción
```java
@Transactional
public void subirSalarios(double porcentaje) {
    // aumenta salarios dentro de una transacción
}
```

Las transacciones garantizan atomicidad: todo o nada.
