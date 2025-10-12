---
title: Capítulo 7 — Clases y objetos
description: Explicación de los conceptos de clase y objeto, constructores, visibilidad, encapsulamiento, métodos estáticos e internas en Java.
---

# Capítulo 7 — Clases y objetos

## 7.1. Qué es una clase y qué es un objeto

Una **clase** es una plantilla que define las características y comportamientos comunes de un conjunto de objetos.  
Un **objeto** es una instancia concreta de una clase.

```java
public class Persona {
    String nombre;
    int edad;

    void saludar() {
        System.out.println("Hola, me llamo " + nombre);
    }
}

public class Main {
    public static void main(String[] args) {
        Persona p1 = new Persona();   // Creación del objeto
        p1.nombre = "Ana";
        p1.edad = 25;
        p1.saludar();                 // "Hola, me llamo Ana"
    }
}
```

---

## 7.2. Constructores y constructor por defecto

Un **constructor** es un método especial que se ejecuta al crear un objeto.  
Sirve para inicializar sus atributos.

```java
public class Persona {
    String nombre;
    int edad;

    public Persona(String n, int e) {
        nombre = n;
        edad = e;
    }
}
```

Si no se define un constructor, Java genera automáticamente un **constructor por defecto sin parámetros**.

```java
public class Persona {
    String nombre;
    int edad;
    // Constructor por defecto generado automáticamente
}
```

---

## 7.3. Llamada entre constructores (`this()` y `super()`)

Una clase puede tener varios constructores (**sobrecarga de constructores**).  
Se puede llamar a otro constructor de la misma clase con `this()` o al de la superclase con `super()`.

```java
public class Persona {
    String nombre;
    int edad;

    public Persona() {
        this("Desconocido", 0); // Llama al otro constructor
    }

    public Persona(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
    }
}
```

En clases hijas, `super()` permite invocar el constructor del padre.

```java
public class Alumno extends Persona {
    String curso;

    public Alumno(String nombre, int edad, String curso) {
        super(nombre, edad);
        this.curso = curso;
    }
}
```

---

## 7.4. Modificadores de visibilidad

Los modificadores controlan desde dónde pueden accederse los atributos o métodos.

| Modificador | Visible desde la misma clase | Subclases | Mismo paquete | Otras clases |
|--------------|------------------------------|------------|----------------|---------------|
| `public` | ✅ | ✅ | ✅ | ✅ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| *(sin modificador)* | ✅ | ❌ | ✅ | ❌ |
| `private` | ✅ | ❌ | ❌ | ❌ |

---

## 7.5. Encapsulamiento y métodos *getter*/*setter*

El **encapsulamiento** protege los datos de una clase ocultándolos del exterior mediante atributos privados y métodos públicos.

```java
public class Persona {
    private String nombre;
    private int edad;

    public String getNombre() { return nombre; }
    public void setNombre(String n) { nombre = n; }

    public int getEdad() { return edad; }
    public void setEdad(int e) {
        if (e >= 0) edad = e;
    }
}
```

Este modelo de acceso controlado es la base de los **JavaBeans**.

---

## 7.6. Atributos y métodos estáticos

Los miembros declarados con `static` pertenecen a la **clase**, no a las instancias.

```java
public class Contador {
    public static int total = 0;

    public Contador() {
        total++;
    }
}

Contador c1 = new Contador();
Contador c2 = new Contador();
System.out.println(Contador.total); // 2
```

También los métodos pueden ser estáticos:

```java
public class Utilidad {
    public static int sumar(int a, int b) {
        return a + b;
    }
}

int resultado = Utilidad.sumar(3, 5);
```

> Los métodos estáticos no pueden acceder directamente a miembros no estáticos.

---

## 7.7. Bloques estáticos

Un **bloque estático** se ejecuta una sola vez cuando la clase se carga en memoria.

```java
public class Configuracion {
    static {
        System.out.println("Clase Configuracion cargada.");
    }
}
```

---

## 7.8. Clases internas y anónimas

Una **clase interna** es una clase definida dentro de otra.  
Se usa para agrupar lógicamente clases que solo tienen sentido dentro de otra.

```java
public class Externa {
    private int valor = 10;

    class Interna {
        void mostrar() {
            System.out.println("Valor = " + valor);
        }
    }
}
```

### Clases internas estáticas

Se pueden crear sin necesidad de una instancia de la clase externa.

```java
public class Externa {
    static class Interna {
        void saludo() {
            System.out.println("Clase interna estática.");
        }
    }
}
```

### Clases anónimas

Son clases sin nombre que implementan interfaces o heredan clases.

```java
Runnable tarea = new Runnable() {
    @Override
    public void run() {
        System.out.println("Ejecutando tarea.");
    }
};
```

---

## 7.9. Herencia (`extends` y `super`)

Una clase puede heredar atributos y métodos de otra.

```java
public class Empleado extends Persona {
    double salario;

    public Empleado(String nombre, int edad, double salario) {
        super(nombre, edad);
        this.salario = salario;
    }
}
```

`super` se utiliza para acceder a miembros de la clase padre.

```java
public void mostrar() {
    System.out.println("Nombre: " + super.getNombre());
}
```

---

## 7.10. Polimorfismo

El **polimorfismo** permite que un objeto adopte múltiples formas.

```java
Persona p = new Alumno("Ana", 20, "Informática");
p.presentarse(); // Llama a la versión del método en Alumno
```

### Polimorfismo en compilación (Overloading)

Consiste en tener varios métodos con el mismo nombre pero distintos parámetros.

```java
public int sumar(int a, int b) { return a + b; }
public double sumar(double a, double b) { return a + b; }
```

### Polimorfismo en ejecución (Overriding)

Una subclase redefine un método heredado del padre.

```java
@Override
public void presentarse() {
    System.out.println("Soy un alumno llamado " + getNombre());
}
```

### *Binding* estático y dinámico

| Tipo | Momento de decisión | Asociado a |
|-------|---------------------|-------------|
| Estático | Compilación | Overloading |
| Dinámico | Ejecución | Overriding |

---

## 7.11. Clases abstractas y métodos abstractos

Una clase **abstracta** no puede instanciarse directamente.  
Sirve como base para otras clases.

```java
public abstract class Figura {
    abstract double area();
}

public class Circulo extends Figura {
    double radio;
    public Circulo(double r) { radio = r; }
    double area() { return Math.PI * radio * radio; }
}
```

---

## 7.12. Resumen

- Una clase define atributos y métodos; los objetos son sus instancias.  
- Los constructores inicializan los objetos al crearse.  
- El encapsulamiento protege los datos mediante `private`, `get` y `set`.  
- Los miembros `static` pertenecen a la clase, no al objeto.  
- Las clases internas permiten agrupar lógica auxiliar.  
- La herencia y el polimorfismo son la base de la reutilización y extensibilidad del código.  
- Las clases abstractas definen una interfaz común para las subclases.
