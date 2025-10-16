# 🧩 Iteración 4 — Uso de Lombok

Lombok genera automáticamente getters, setters, constructores y `toString()`.

## Dependencia Maven
```xml
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.18.42</version>
  <scope>provided</scope>
</dependency>
```

## Ejemplo
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {
    private int id;
    private String nombre;
    private double salario;
}
```

💡 Lombok simplifica el código sin alterar la estructura MVC.
