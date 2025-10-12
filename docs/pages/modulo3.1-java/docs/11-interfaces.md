---
title: Capítulo 11 — Interfaces en Java
description: Explicación detallada del uso de interfaces en Java, diferencias con clases abstractas y ejemplos de interfaces comunes.
---

# Capítulo 11 — Interfaces en Java

## 11.1. Qué es una interfaz

Una **interfaz** en Java define un conjunto de métodos que una clase debe implementar.  
Sirve para **establecer contratos** que especifican el comportamiento que una clase debe cumplir, sin definir cómo se implementa.

```java
public interface Vehiculo {
    void arrancar();
    void detener();
}
```

Una clase que implementa la interfaz debe proporcionar el código de sus métodos:

```java
public class Coche implements Vehiculo {
    public void arrancar() {
        System.out.println("El coche ha arrancado.");
    }

    public void detener() {
        System.out.println("El coche se ha detenido.");
    }
}
```

---

## 11.2. Diferencias entre interfaces y clases abstractas

| Característica | Interfaz | Clase abstracta |
|----------------|-----------|------------------|
| Herencia múltiple | ✅ Sí (puede implementar varias) | ❌ No |
| Métodos con cuerpo | Desde Java 8 (default/static) | ✅ Sí |
| Atributos | Constantes (`public static final`) | Variables y constantes |
| Constructores | No tiene | Puede tener |
| Finalidad | Definir contratos | Proporcionar una base común |
| Palabra clave | `implements` | `extends` |

---

## 11.3. Métodos `default` y `static` en interfaces

Desde Java 8, las interfaces pueden contener métodos **con implementación**.

### Método `default`
Permite añadir nuevos métodos a interfaces sin romper las implementaciones existentes.

```java
public interface Vehiculo {
    void arrancar();
    default void frenar() {
        System.out.println("El vehículo está frenando.");
    }
}
```

Uso:
```java
Coche c = new Coche();
c.frenar(); // Llama al método por defecto
```

### Método `static`
Pertenece a la interfaz y se llama directamente desde ella.

```java
public interface Calculadora {
    static int sumar(int a, int b) {
        return a + b;
    }
}

int resultado = Calculadora.sumar(5, 3);
```

---

## 11.4. Herencia múltiple de interfaces

Una clase puede implementar varias interfaces, incluso si comparten métodos con el mismo nombre.

```java
interface A { void metodo(); }
interface B { void metodo(); }

class C implements A, B {
    public void metodo() {
        System.out.println("Implementación común para A y B");
    }
}
```

Si hay conflicto entre métodos `default`, se debe **sobrescribir** el método en la clase que los implementa.

---

## 11.5. Interfaces funcionales

Una **interfaz funcional** es una interfaz que tiene **exactamente un método abstracto**.  
Se utilizan ampliamente con **expresiones lambda** y **Streams**.

Ejemplo clásico:

```java
@FunctionalInterface
public interface Operacion {
    int aplicar(int a, int b);
}
```

Uso con lambda:

```java
Operacion suma = (a, b) -> a + b;
System.out.println(suma.aplicar(3, 4)); // 7
```

### Reglas
- Solo puede tener **un método abstracto**.  
- Puede incluir métodos `default` y `static`.  
- La anotación `@FunctionalInterface` es opcional, pero recomendable.

---

## 11.6. Interfaces funcionales comunes en Java

| Interfaz | Descripción | Método principal | Ejemplo |
|-----------|--------------|------------------|----------|
| `Predicate<T>` | Evalúa una condición sobre un valor | `test(T t)` → `boolean` | `p -> p > 0` |
| `Consumer<T>` | Realiza una acción con un valor | `accept(T t)` | `System.out::println` |
| `Supplier<T>` | Proporciona un valor sin entrada | `get()` | `() -> Math.random()` |
| `Function<T,R>` | Transforma un valor de tipo T en R | `apply(T t)` | `x -> x * 2` |
| `UnaryOperator<T>` | Variante de `Function<T,T>` | `apply(T t)` | `x -> x + 1` |
| `BinaryOperator<T>` | Variante de `BiFunction<T,T,T>` | `apply(T,T)` | `(a,b) -> a+b` |

Ejemplo con `Predicate`:

```java
import java.util.function.Predicate;

Predicate<Integer> esPar = n -> n % 2 == 0;
System.out.println(esPar.test(4)); // true
System.out.println(esPar.test(5)); // false
```

---

## 11.7. Selección de métodos y referencias a métodos

Una **referencia a método** permite usar un método existente como si fuera una expresión lambda.

```java
Consumer<String> imprimir = System.out::println;
imprimir.accept("Hola mundo");
```

También se pueden usar referencias a métodos de instancia o constructores:

```java
Function<String, Integer> convertir = Integer::parseInt;
Supplier<Persona> crearPersona = Persona::new;
```

Cualquier método que coincida en **tipo de entrada y salida** puede usarse como implementación de una interfaz funcional.

---

## 11.8. Interfaces comunes de la API de Java

### `Comparable<T>`
Define el orden natural de los objetos.

```java
public class Persona implements Comparable<Persona> {
    private String nombre;

    public Persona(String n) { nombre = n; }

    @Override
    public int compareTo(Persona otra) {
        return this.nombre.compareTo(otra.nombre);
    }
}
```

Uso:

```java
List<Persona> lista = List.of(new Persona("Ana"), new Persona("Luis"));
Collections.sort(lista);
```

### `Comparator<T>`
Permite definir distintos criterios de ordenación.

```java
Comparator<Persona> porLongitudNombre = (p1, p2) ->
    Integer.compare(p1.getNombre().length(), p2.getNombre().length());
```

### `Runnable`
Define una tarea que puede ejecutarse en un hilo.

```java
Runnable tarea = () -> System.out.println("Ejecución en hilo");
new Thread(tarea).start();
```

---

## 11.9. Ejemplo completo

```java
import java.util.function.*;

public class EjemploInterfaces {
    public static void main(String[] args) {
        Predicate<Integer> esPositivo = n -> n > 0;
        Function<Integer, String> aTexto = n -> "Número: " + n;
        Consumer<String> imprimir = System.out::println;

        int[] numeros = {-2, 0, 3, 7};
        for (int n : numeros) {
            if (esPositivo.test(n)) {
                imprimir.accept(aTexto.apply(n));
            }
        }
    }
}
```

Salida:
```
Número: 3
Número: 7
```

---

## 11.10. Resumen

- Las interfaces definen contratos que las clases deben cumplir.  
- Pueden contener métodos `default` y `static`.  
- Admiten herencia múltiple.  
- Las **interfaces funcionales** permiten usar **expresiones lambda**.  
- `Predicate`, `Function`, `Consumer`, `Supplier`, `Runnable` y `Comparable` son las más usadas en la API estándar.
