# 🧱 POJO y JavaBean

Antes de definir el modelo de datos, es importante entender estos dos conceptos clave.

## 🔹 ¿Qué es un POJO?
**POJO (Plain Old Java Object)** es una clase Java simple, sin dependencias de frameworks.

```java
public class Empleado {
    private int id;
    private String nombre;
    private double salario;
}
```

**Características:**
- Sin herencias de frameworks.
- Atributos, constructores y métodos simples.
- Facilita las pruebas y la reutilización.

## 🔹 ¿Qué es un JavaBean?
Un **JavaBean** es un **POJO con reglas adicionales**:
1. Constructor público sin argumentos.  
2. Atributos privados con **getters/setters** públicos.  
3. Implementa opcionalmente `Serializable`.

```java
public class Empleado implements java.io.Serializable {
    private int id;
    private String nombre;
    private double salario;

    public Empleado() {}
    public Empleado(int id, String nombre, double salario) {
        this.id = id; this.nombre = nombre; this.salario = salario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
}
```

| Característica | POJO | JavaBean |
|-----------------|------|----------|
| Constructor vacío | No | Sí |
| Getters/Setters | Opcional | Obligatorio |
| Atributos privados | Recomendado | Requerido |
| Serializable | No necesario | Recomendado |

💡 **Todo JavaBean es un POJO, pero no todo POJO es un JavaBean.**
