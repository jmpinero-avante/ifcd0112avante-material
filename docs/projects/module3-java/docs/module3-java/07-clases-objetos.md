---
title: Capítulo 7 — Clases y objetos
description: Explicación de los conceptos de clase y objeto, constructores, visibilidad, encapsulamiento, métodos estáticos e internas en Java.
---

# Capítulo 7 — Clases y objetos

## 7.1. Qué es una clase y qué es un objeto

Una **clase** es una plantilla que define las características y comportamientos comunes de un conjunto de objetos. Se dice comúnmente que una clase es: `clase = datos + operaciones`.

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
Se puede llamar a otro constructor de la misma clase con `this()` o al de la superclase (clase de la que hereda) con `super()`.

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

> Cuando no hay escrito ningún mpdificador de visibilidad, se dice que el elemento es _private-package_ (porque es como un _private_ pero las clases del mismo paquete si pueden acceder).

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

Una clase interna tiene acceso a todo lo que defina más todo lo que defina su clase contenedora.

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

Una clase puede heredar de otra.

Cuando esto ocurre decimos que:

- `A extends B`

- `A hereda de B`

- `A es hija de B`

- `B es padre de A`

- `A hereda todos los atributos y métodos de B`

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

Nos referimos a los métodos de una clase. Al llamar al método de nombre _miMetodo_ de un objeto, puede haber distintas versiones del mismo.

Java llamará a la adecuada, siguiendo sus normas de ejecución. En el ejemplo siguiente se llama a la versión de _presentarse_ efectiva para la clase _Alumno_.


```java
Persona p = new Alumno("Ana", 20, "Informática");
p.presentarse(); // Llama a una versión del método presentarse
```

### Polimorfismo en compilación (Overloading)

Consiste en tener varios métodos con el mismo nombre pero distintos parámetros.

```java
public int    sumar(int    a, int    b) { return a + b; }
public double sumar(double a, double b) { return a + b; }
```

```java
sumar(10,30);
```

Java llamará a uno u otro dependiendo de los argumentos que se pasen al método _sumar_.

> **Los argumentos de llamada deben coincidir en número y ser compatible en su tipo con los parámetros formales.**

Los **argumentos de llamada** son los datos (variables, literales, expresiones...), con los que se llama al método. En el ejemplo de arriba son `(10,30)`.

Los **parámetros formales** son los que aparecen en la cabecera de la implementación del método. En el ejemplo de arriba, serán `(int a, int b)` para un método y `(double a, double b)` para el otro.

> **Si hay más de un método cuyos parámetros formales coinciden en número y son de tipos compatibles con los argumentos de llamada: se elije aquel que tenga tipos más específicos.**

> **Un tipo es más específico que otro si el primero hereda o implementa el segundo, ya sea direactmente o a través de otros tipos intermedios.**

- Si A hereda de B; A es más específico que B.
- Si A implementa la interfaz B; A es más específico que B.
- **Si** A es más específico que B **y** B es más específico que C **entonces** A es más específico que C

> **Si no se puede elegir un método sin ambigüedad, Java lanza un error de compilación.**

### Polimorfismo en ejecución (Overriding)

Una subclase redefine un método heredado del padre.

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
    
    public void presentarse() {
        System.out.println("Hola, soy el ALUMNO " + this.getNombre());
    }
}

public class Empleado extends Persona {
    double salario;

    public Empleado(String nombre, int edad, double salario) {
        super(nombre, edad);
        this.salario = salario;
    }

    public void presentarse() {
        System.out.println("Hola, soy el EMPLEADO " + this.getNombre());
    }
}
```

> **Cuando el mismo método es implementado en más de una clase de la misma línea hereditaria; java elige el de la clase del objeto que llama al método si este lo implementa. Si no, el de la primera clase padre que lo haga.**

### *Binding* estático y dinámico

> `Binding` se podría traducir con _unión_ o _unificación_. El _binding_ _une_ o _unifica_ una llamada a un método con la implementación adecuada, según las reglas del polimorfismo.

> El _binding estático_ ocurre en tiempo de compilación, simplemente mirando el código. Soluciona los posibles casos de _overloading_ que pueda haber.

- _overloading_ : varios métodos con el mismo nombre pero distintos parámetros.

> El _binding dinámico_ ocurre en tiempo de ejecución. Para poder resolver los casos de overriding que pueda haber necesita conocer el tipo real del objeto que llama al método. 

- _overriding_ : métodos implementados en varias clases de la misma jerarquía.

| Tipo | Momento de decisión | Asociado a |
|-------|---------------------|-------------|
| Estático | Compilación | Overloading |
| Dinámico | Ejecución | Overriding |

---

## 7.11. Clases abstractas y métodos abstractos

- Una clase **abstracta** no puede instanciarse directamente. No se pueden crear objetos de una clase abstracta. 

- Una clase **concreta** es aquella que no es abstracta.

- Una **clase abstracta** puede tener **métodos abstractos**. Estos no tendrán código y deberán de ser implementados por las clases concretas que hereden de ella.

- Una **clase abstracta** también puede tener **métodos concretos** (con código), para proporcionar una implementación de los mismos a las clases que hereden de ella.

- Una **clase concreta** sólo puede tener **métodos concretos**.


```java
public abstract class Figura {
    // método abstracto
    abstract double area();

    // método concreto
    public String toString() {
        return String.format("Figura de area %d.",this.area());
    }
}

public class Circulo extends Figura {
    double radio;
    public Circulo(double r) { radio = r; }

    // la clase hija tiene que implementar
    //    los métodos abstractos de la clase padre
    double area() { return Math.PI * radio * radio; }

    // No tiene porque implementar toString, pero podría hacerlo
}
```

> La clases abstractas sirven como base para otras clases.

---

## 7.12. Resumen

- Una clase define atributos y métodos; los objetos son sus instancias.  
- Los constructores inicializan los objetos al crearse.  
- El encapsulamiento protege los datos mediante `private`, `get` y `set`.  
- Los miembros `static` pertenecen a la clase, no al objeto.  
- Las clases internas permiten agrupar lógica auxiliar.  
- La herencia y el polimorfismo son la base de la reutilización y extensibilidad del código.  
- Las clases abstractas definen una interfaz común para las subclases.
