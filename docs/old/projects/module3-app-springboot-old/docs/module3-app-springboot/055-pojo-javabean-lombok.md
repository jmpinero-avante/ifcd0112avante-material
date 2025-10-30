# POJOs, JavaBeans y Lombok

## Objetivo

- Comprender qué es un **POJO** y cómo se usa para representar entidades del dominio.  
- Distinguir entre **POJO** y **JavaBean**.  
- Usar **Lombok** para reducir el código repetitivo (constructores, getters/setters, `toString()`, etc.).  
- Preparar el modelo de datos que se usará en la siguiente iteración con patrón MVC.

---

## 1. Qué es un POJO

**POJO** significa *Plain Old Java Object* (“viejo objeto Java simple”).  
Se trata de una **clase Java sin dependencias externas ni restricciones**, usada para representar datos de forma sencilla.

Ejemplo clásico sin Lombok:

```java
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
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    @Override
    public String toString() {
        return String.format("%d - %s - %.2f €", id, nombre, salario);
    }
}
```

Un **POJO** se caracteriza por:
- Tener **atributos privados**.
- Proveer **constructores** y **getters/setters**.
- No heredar de ninguna clase especial (como `HttpServlet` o `EntityBean`).
- No implementar interfaces obligatorias del framework.
- Poder serializarse fácilmente o usarse en colecciones (`List`, `Map`, etc.).

---

## 2. Qué es un JavaBean

Un **JavaBean** es una evolución del POJO, con unas normas más formales.  
Está pensado para ser usado por frameworks, bibliotecas o entornos de desarrollo que detectan sus propiedades automáticamente.

| Característica | POJO | JavaBean |
|----------------|------|-----------|
| Atributos privados | ✔️ | ✔️ |
| Getters y Setters | ✔️ | ✔️ |
| Constructor sin argumentos | Opcional | Obligatorio |
| Serializable (`implements Serializable`) | Opcional | Recomendado |
| Convenciones de nombres (`getNombre`, `setNombre`) | Recomendadas | Obligatorias |
| Dependencias de framework | Ninguna | Ninguna (pero preparado para integrarse) |

Ejemplo de JavaBean:

```java
import java.io.Serializable;

public class Empleado implements Serializable {
    private int id;
    private String nombre;
    private double salario;

    public Empleado() {} // Constructor vacío obligatorio

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
}
```

En la práctica, un **POJO puede convertirse en un JavaBean** simplemente añadiendo el constructor vacío y los getters/setters.

---

## 3. Introducción a Lombok

El problema de los POJOs y Beans tradicionales es el **código repetitivo** (getters, setters, `toString`, etc.).  
Aquí entra **Lombok**, una biblioteca que **genera automáticamente el código repetitivo durante la compilación**.

### 3.1 Dependencia Maven

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.42</version>
    <scope>provided</scope>
</dependency>
```

### 3.2 Ejemplo con Lombok

```java
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data                   // Genera getters, setters, toString, equals y hashCode
@AllArgsConstructor     // Genera constructor con todos los argumentos
@NoArgsConstructor      // Genera constructor vacío
public class Empleado {
    private int id;
    private String nombre;
    private double salario;
}
```

**Código generado automáticamente por Lombok:**
- `getId()`, `setId()`  
- `getNombre()`, `setNombre()`  
- `getSalario()`, `setSalario()`  
- `toString()`, `equals()`, `hashCode()`  
- Constructores vacío y completo

---

## 4. Comparación práctica

| Aspecto | POJO manual | Con Lombok |
|----------|-------------|-------------|
| Código fuente | Largo y repetitivo | Compacto y legible |
| Mantenimiento | Mayor esfuerzo | Menor esfuerzo |
| Errores humanos | Posibles (olvido de getter/setter) | Eliminados |
| Rendimiento | Igual | Igual (el código se genera en compilación) |

---

## 5. Ejemplo de uso combinado con JDBC

Una vez que tenemos nuestra clase `Empleado` con Lombok, podemos usarla directamente en nuestros `PreparedStatement`:

```java
Empleado e = new Empleado(0, "Ana", 2000.0);
PreparedStatement ps = conn.prepareStatement("INSERT INTO empleados (nombre, salario) VALUES (?, ?)");
ps.setString(1, e.getNombre());
ps.setDouble(2, e.getSalario());
ps.executeUpdate();
```

---

## 6. Instalación y configuración de Lombok

1. **Descarga o incluye Lombok** en tu proyecto (por Maven o manualmente).  
2. En **NetBeans o IntelliJ**, asegúrate de habilitar el *annotation processing*.  
   - En IntelliJ: *Settings → Build → Annotation Processors → Enable*.  
   - En NetBeans: *Project Properties → Build → Compiling → Enable Annotation Processing*.  
3. Si usas **VSCode** con el plugin de Java, se activa automáticamente.  

---

## 7. Conclusión

A partir de ahora, todos los objetos del modelo (como `Empleado`, `Cliente`, `Producto`, etc.) se definirán con **Lombok** para:
- Reducir el código repetitivo.  
- Mejorar la legibilidad.  
- Facilitar la integración con frameworks (Spring, Hibernate, etc.).  

La próxima iteración aplicará el **patrón MVC**, integrando estos POJOs con los `PreparedStatement` y el controlador que gestionará las operaciones de la base de datos.

---

## 8. Siguiente paso

A partir de aqui...
- El modelo (`Empleado`) será un POJO con Lombok.  
- Se usará un repositorio (`EmpleadoRepository`) que gestiona las consultas con `PreparedStatement`.  
- El controlador y la vista (consola) estarán desacoplados, siguiendo la arquitectura MVC.
