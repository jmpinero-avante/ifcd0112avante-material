---
title: Capítulo 14 — Interfaces funcionales y programación lambda
description: Introducción a las expresiones lambda, interfaces funcionales y uso de Streams en Java.
---

# Capítulo 14 — Interfaces funcionales y programación lambda

Las **expresiones lambda** y las **interfaces funcionales** introducidas en **Java 8** permiten escribir código más conciso y expresivo.  
Son la base de la programación funcional en Java, y se usan ampliamente en los **Streams** y las APIs modernas del lenguaje.

---

## 14.1. Qué es una expresión lambda

Una **lambda** es una forma compacta de definir una función anónima, es decir, una función sin nombre.

### Sintaxis general

```java
(parametros) -> expresión
```

O con bloque de código:

```java
(parametros) -> {
    // cuerpo
    return resultado;
}
```

### Ejemplo

```java
Runnable tarea = () -> System.out.println("Ejecutando una tarea");
tarea.run();
```

---

## 14.2. Interfaces funcionales

Una **interfaz funcional** tiene exactamente **un método abstracto**.  
Las expresiones lambda son su implementación directa.

Ejemplo:

```java
@FunctionalInterface
public interface Operacion {
    int aplicar(int a, int b);
}
```

Uso:

```java
Operacion suma = (a, b) -> a + b;
System.out.println(suma.aplicar(3, 4)); // 7
```

### Regla clave
> Toda expresión lambda implementa automáticamente el único método abstracto de una interfaz funcional.

---

## 14.3. Interfaces funcionales comunes (`java.util.function`)

| Interfaz | Método principal | Descripción | Ejemplo |
|-----------|------------------|--------------|----------|
| `Predicate<T>` | `boolean test(T t)` | Evalúa una condición | `x -> x > 0` |
| `Consumer<T>` | `void accept(T t)` | Ejecuta una acción | `System.out::println` |
| `Supplier<T>` | `T get()` | Proporciona un valor | `() -> Math.random()` |
| `Function<T,R>` | `R apply(T t)` | Transforma un valor | `x -> x * 2` |
| `UnaryOperator<T>` | `T apply(T t)` | Operación unaria sobre un valor | `x -> x + 1` |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | Combina dos valores del mismo tipo | `(a, b) -> a + b` |

Ejemplo práctico:

```java
import java.util.function.*;

public class EjemploFunciones {
    public static void main(String[] args) {
        Predicate<Integer> esPar = n -> n % 2 == 0;
        Function<Integer, String> convertir = n -> "Número: " + n;
        Consumer<String> imprimir = System.out::println;

        for (int i = 1; i <= 5; i++) {
            if (esPar.test(i)) {
                imprimir.accept(convertir.apply(i));
            }
        }
    }
}
```

Salida:
```
Número: 2
Número: 4
```

---

## 14.4. Referencias a métodos

Una **referencia a método** permite reutilizar un método existente en lugar de escribir una lambda equivalente.

### Tipos de referencias
| Forma | Ejemplo | Equivalente lambda |
|--------|----------|-------------------|
| `Clase::metodoEstatico` | `Math::sqrt` | `(x) -> Math.sqrt(x)` |
| `objeto::metodoInstancia` | `System.out::println` | `(x) -> System.out.println(x)` |
| `Clase::new` (constructor) | `Persona::new` | `() -> new Persona()` |

Ejemplo:

```java
List<String> nombres = List.of("Ana", "Luis", "Pedro");
nombres.forEach(System.out::println);
```

---

## 14.5. Streams en Java

Un **Stream** es una secuencia de datos sobre la que se pueden aplicar operaciones funcionales de forma encadenada.  
No modifica la colección original y facilita la programación declarativa.

```java
List<String> nombres = List.of("Ana", "Luis", "Pedro");

nombres.stream()
       .filter(n -> n.length() > 3)
       .map(String::toUpperCase)
       .forEach(System.out::println);
```

Salida:
```
LUIS
PEDRO
```

---

## 14.6. Conversión de colecciones y arrays a Stream

### Desde una colección

```java
List<Integer> numeros = List.of(1, 2, 3, 4, 5);
Stream<Integer> flujo = numeros.stream();
```

### Desde un array

```java
int[] datos = {10, 20, 30};
IntStream flujo = Arrays.stream(datos);
```

### Desde valores explícitos

```java
Stream<String> letras = Stream.of("A", "B", "C");
```

### Desde un `Stream.Builder`

```java
Stream.Builder<String> builder = Stream.builder();
builder.add("Uno").add("Dos").add("Tres");
Stream<String> flujo = builder.build();
flujo.forEach(System.out::println);
```

---

## 14.7. Operaciones sobre Streams

### Operaciones intermedias (devuelven un Stream)

| Método | Descripción |
|---------|--------------|
| `filter(Predicate)` | Filtra elementos según una condición |
| `map(Function)` | Transforma cada elemento |
| `sorted()` | Ordena los elementos |
| `distinct()` | Elimina duplicados |
| `limit(n)` / `skip(n)` | Limita o salta elementos |

### Operaciones terminales (devuelven un resultado)

| Método | Descripción |
|---------|--------------|
| `forEach(Consumer)` | Ejecuta una acción sobre cada elemento |
| `count()` | Cuenta los elementos |
| `collect(Collectors.toList())` | Convierte a lista |
| `findFirst()` / `findAny()` | Devuelve un elemento |
| `reduce()` | Combina todos los elementos en uno solo |

Ejemplo:

```java
List<Integer> lista = List.of(1, 2, 3, 4, 5);
int suma = lista.stream()
                .filter(n -> n % 2 == 0)
                .reduce(0, Integer::sum);
System.out.println("Suma de pares: " + suma);
```

Salida:
```
Suma de pares: 6
```

---

## 14.8. Uso de `Stream.Builder`

`Stream.Builder` permite construir un stream de forma incremental.

```java
Stream.Builder<String> sb = Stream.builder();
sb.add("A").add("B").add("C");
Stream<String> flujo = sb.build();
flujo.map(String::toLowerCase)
     .forEach(System.out::println);
```

---

## 14.9. Paralelización de Streams

Los streams pueden ejecutarse en paralelo para aprovechar varios núcleos del procesador.

```java
List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6);
int suma = numeros.parallelStream()
                  .mapToInt(n -> n * 2)
                  .sum();
System.out.println("Suma total: " + suma);
```

> Los streams paralelos mejoran el rendimiento en grandes volúmenes de datos, pero no siempre son convenientes para operaciones pequeñas o con efectos secundarios.

---

## 14.10. Ejemplo completo

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class EjemploLambdaStream {
    public static void main(String[] args) {
        List<String> nombres = List.of("Ana", "Luis", "Pedro", "Sofía");

        nombres.stream()
               .filter(n -> n.length() > 3)
               .map(String::toUpperCase)
               .sorted()
               .forEach(System.out::println);
    }
}
```

Salida:
```
LUIS
PEDRO
SOFÍA
```

---

## 14.11. Resumen

- Una **lambda** implementa el método abstracto de una interfaz funcional.  
- Las interfaces funcionales (`Predicate`, `Function`, `Consumer`, etc.) son esenciales para trabajar con Streams.  
- Los **Streams** permiten operar sobre datos de forma funcional y fluida.  
- Se pueden crear desde colecciones, arrays o mediante `Stream.Builder`.  
- Las operaciones se dividen en **intermedias** (transforman) y **terminales** (devuelven resultados).  
- `Stream.parallel()` permite ejecutar operaciones de forma concurrente.
