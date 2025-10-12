---
title: Capítulo 8 — Tipos especiales en Java
description: Explicación de enumerados, records, POJO y JavaBeans, con ejemplos y buenas prácticas.
---

# Capítulo 8 — Tipos especiales

Los **tipos especiales** en Java amplían las posibilidades de modelado de datos y organización del código.  
Entre ellos destacan los **enumerados (`enum`)**, los **records**, y las convenciones de clases tipo **POJO** y **JavaBean**.

---

## 8.1. Clases `final`

Una clase declarada como `final` **no puede heredarse**.  
Esto se usa para impedir modificaciones en el comportamiento de una clase.

```java
public final class Constantes {
    public static final double PI = 3.14159;
}
```

Intentar extenderla provocará un error de compilación:

```java
public class SubConstantes extends Constantes {} // Error
```

---

## 8.2. Enumerados (`enum`)

Un **enumerado** es un tipo especial de clase que define un conjunto fijo de valores constantes.

### Enumerado simple

```java
public enum DiaSemana {
    LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
}
```

Uso:

```java
DiaSemana hoy = DiaSemana.MARTES;
System.out.println(hoy);
```

### Métodos incorporados
- `values()` → devuelve un array con todos los valores del enumerado.  
- `ordinal()` → devuelve la posición del elemento (empezando en 0).  
- `valueOf(String)` → devuelve el valor del nombre indicado.

```java
for (DiaSemana d : DiaSemana.values()) {
    System.out.println(d + " (" + d.ordinal() + ")");
}
```

> No se recomienda usar `ordinal()` en código de producción, ya que cambia si se modifica el orden de los valores.

---

### Enumerado con atributos y constructor

Los `enum` pueden tener atributos, métodos y constructores privados.

```java
public enum EstadoPedido {
    PENDIENTE("En espera"),
    EN_PROCESO("Procesando"),
    ENVIADO("Enviado"),
    ENTREGADO("Completado");

    private String descripcion;

    private EstadoPedido(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
```

Uso:

```java
EstadoPedido e = EstadoPedido.EN_PROCESO;
System.out.println(e.getDescripcion()); // Procesando
```

Cada elemento del `enum` es una instancia única de la clase `EstadoPedido`.

---

### Enumerado con comportamiento específico por valor

Cada elemento puede **sobrescribir métodos** si es necesario.

```java
public enum Operacion {
    SUMA {
        public double aplicar(double a, double b) { return a + b; }
    },
    RESTA {
        public double aplicar(double a, double b) { return a - b; }
    };

    public abstract double aplicar(double a, double b);
}
```

Uso:

```java
double resultado = Operacion.SUMA.aplicar(5, 3);
System.out.println(resultado); // 8.0
```

---

## 8.3. Records

Los **records** son una forma compacta de definir clases **inmutables** que solo almacenan datos.  
Introducidos en **Java 16**, reemplazan patrones comunes como las clases de datos o POJOs simples.

### Ejemplo básico

```java
public record Persona(String nombre, int edad) {}
```

Esto equivale a una clase con:
- Campos `private final`
- Un constructor público
- Métodos `equals()`, `hashCode()` y `toString()` generados automáticamente

Uso:

```java
Persona p = new Persona("Ana", 30);
System.out.println(p.nombre());
System.out.println(p); // Persona[nombre=Ana, edad=30]
```

### Características
- Todos los campos son **`final`** (inmutables).  
- No se pueden extender (`record` es implícitamente `final`).  
- Implementan automáticamente `equals()` y `hashCode()` de forma coherente.

### Constructor personalizado

```java
public record Producto(String nombre, double precio) {
    public Producto {
        if (precio < 0) throw new IllegalArgumentException("Precio inválido");
    }
}
```

### Métodos adicionales

```java
public record Punto(int x, int y) {
    public double distanciaAlOrigen() {
        return Math.sqrt(x * x + y * y);
    }
}
```

---

### Clase base `java.lang.Record`

Todos los records heredan implícitamente de la clase `java.lang.Record`.  
Por tanto, pueden utilizarse en contextos donde se espera un objeto de tipo `Record`, pero **no pueden heredarse entre sí**.

---

## 8.4. POJO (Plain Old Java Object)

Un **POJO** es una clase Java simple que:
- No hereda de clases especiales.  
- No implementa interfaces específicas del framework.  
- No tiene dependencias externas ni configuraciones adicionales.

Ejemplo:

```java
public class Alumno {
    private String nombre;
    private int edad;

    public Alumno() {}

    public Alumno(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
}
```

Los POJOs son la base de la mayoría de clases de modelo en Java.

Un POJO **puede implementar interfaces** y **usar herencia**, pero **no debe depender** de un framework o contener anotaciones especiales.

---

## 8.5. JavaBean

Un **JavaBean** es una evolución de un POJO con una estructura más estricta.  
Se usa habitualmente en entornos donde es necesario acceder dinámicamente a las propiedades (por ejemplo, en herramientas gráficas o frameworks de persistencia).

### Requisitos de un JavaBean

1. Constructor público sin argumentos.  
2. Atributos privados.  
3. Métodos `get` y `set` para cada propiedad.  
4. La clase debe ser pública y no `final`.  
5. Puede implementar la interfaz `Serializable`.

Ejemplo:

```java
import java.io.Serializable;

public class Empleado implements Serializable {
    private String nombre;
    private double salario;

    public Empleado() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
}
```

### Diferencias entre POJO, JavaBean y Record

| Característica | POJO | JavaBean | Record |
|-----------------|-------|-----------|---------|
| Constructor sin argumentos | Opcional | Obligatorio | Automático (con parámetros) |
| Getters/Setters | Recomendado | Obligatorio | No aplica (campos `final`) |
| Inmutabilidad | Opcional | No | Sí |
| Serializable | Opcional | Recomendado | Implícito |
| Herencia | Permitida | Permitida | No permitida |
| Ideal para | Modelos simples | Aplicaciones Java EE / frameworks | Clases de datos inmutables |

---

## 8.6. Resumen

- Las clases `final` impiden la herencia.  
- Los `enum` definen conjuntos fijos de valores y pueden tener comportamiento propio.  
- Los `record` simplifican la creación de clases inmutables con métodos generados automáticamente.  
- Los **POJO** son clases simples sin dependencias de frameworks.  
- Los **JavaBeans** siguen una convención estricta y suelen usarse en entornos empresariales.
