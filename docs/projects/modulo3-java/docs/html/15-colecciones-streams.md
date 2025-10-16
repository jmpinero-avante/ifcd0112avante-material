---
title: Capítulo 15 — Colecciones y Streams
description: Explicación de la jerarquía de colecciones en Java, sus implementaciones, complejidades y uso con Streams.
---

# Capítulo 15 — Colecciones y Streams

El framework de **colecciones de Java** proporciona estructuras de datos genéricas para almacenar, acceder y manipular conjuntos de elementos.  
Se complementa con la API de **Streams**, que permite realizar operaciones funcionales sobre las colecciones.

---

## 15.1. Jerarquía general de colecciones

```
Iterable
 └── Collection
      ├── List
      │    ├── ArrayList
      │    ├── LinkedList
      │    └── Vector / Stack
      ├── Set
      │    ├── HashSet
      │    ├── LinkedHashSet
      │    └── TreeSet
      └── Queue
           ├── PriorityQueue
           └── ArrayDeque

Map (no extiende Collection)
 ├── HashMap
 ├── LinkedHashMap
 └── TreeMap
```

---

## 15.2. Interfaces principales

| Interfaz | Descripción |
|-----------|--------------|
| `Iterable` | Permite recorrer elementos con `for-each`. |
| `Collection` | Raíz común de listas, conjuntos y colas. |
| `List` | Colección ordenada y con elementos repetidos. |
| `Set` | No permite elementos duplicados. |
| `Map` | Almacena pares clave-valor. |
| `Queue` | Representa una cola FIFO o estructura de prioridad. |

### `Iterable` y `Iterator`

Cualquier clase que implemente `Iterable` puede recorrerse con un `for-each`:

```java
for (String elemento : lista) {
    System.out.println(elemento);
}
```

El `Iterator` permite recorrido manual:

```java
Iterator<String> it = lista.iterator();
while (it.hasNext()) {
    System.out.println(it.next());
}
```

---

## 15.3. Listas (`List`)

Una `List` mantiene los elementos en orden y permite duplicados.

### Implementaciones

| Clase | Características |
|--------|----------------|
| `ArrayList` | Basada en array dinámico, acceso rápido por índice. |
| `LinkedList` | Lista doblemente enlazada, eficiente en inserciones y borrados. |
| `Vector` | Sincronizada (obsoleta en la mayoría de casos). |
| `Stack` | Pila LIFO, hereda de `Vector`. |

Ejemplo:

```java
List<String> nombres = new ArrayList<>();
nombres.add("Ana");
nombres.add("Luis");
nombres.add("Ana"); // Duplicado permitido
System.out.println(nombres.get(1)); // Luis
```

---

## 15.4. Conjuntos (`Set`)

Un `Set` evita elementos duplicados.

| Clase | Características |
|--------|----------------|
| `HashSet` | Sin orden definido, búsqueda rápida (`O(1)`). |
| `LinkedHashSet` | Mantiene el orden de inserción. |
| `TreeSet` | Ordena automáticamente los elementos (usa `Comparable`). |

Ejemplo:

```java
Set<Integer> numeros = new HashSet<>(List.of(1, 2, 3, 2, 1));
System.out.println(numeros); // [1, 2, 3]
```

---

## 15.5. Mapas (`Map`)

Un `Map` almacena pares **clave → valor**.

| Clase | Características |
|--------|----------------|
| `HashMap` | Acceso rápido, sin orden. |
| `LinkedHashMap` | Mantiene el orden de inserción. |
| `TreeMap` | Ordena las claves automáticamente. |

Ejemplo:

```java
Map<String, Integer> edades = new HashMap<>();
edades.put("Ana", 25);
edades.put("Luis", 30);
System.out.println(edades.get("Ana")); // 25
```

### Iteración sobre un `Map`

```java
for (Map.Entry<String, Integer> e : edades.entrySet()) {
    System.out.println(e.getKey() + " → " + e.getValue());
}
```

---

## 15.6. Complejidad de operaciones comunes

| Estructura | Acceso | Búsqueda | Inserción | Eliminación |
|-------------|---------|-----------|------------|-------------|
| `ArrayList` | O(1) | O(n) | O(1)* | O(n) |
| `LinkedList` | O(n) | O(n) | O(1) | O(1) |
| `HashSet` | O(1) | O(1) | O(1) | O(1) |
| `TreeSet` | O(log n) | O(log n) | O(log n) | O(log n) |
| `HashMap` | O(1) | O(1) | O(1) | O(1) |
| `TreeMap` | O(log n) | O(log n) | O(log n) | O(log n) |

\* En `ArrayList`, la inserción al final suele ser O(1), salvo cuando requiere redimensionar el array.

---

## 15.7. Interfaces clave relacionadas con colecciones

### `Comparable<T>`

Define el orden natural de los objetos.

```java
public class Persona implements Comparable<Persona> {
    private String nombre;
    public Persona(String nombre) { this.nombre = nombre; }

    @Override
    public int compareTo(Persona o) {
        return this.nombre.compareTo(o.nombre);
    }
}
```

### `Comparator<T>`

Permite definir distintos criterios de orden.

```java
Comparator<Persona> porLongitud = 
    (p1, p2) -> Integer.compare(p1.getNombre().length(), p2.getNombre().length());
```

### `Iterable` / `Iterator`

Permiten recorrer elementos de una colección sin exponer su estructura interna.

---

## 15.8. Uso de Streams con colecciones

Los Streams permiten aplicar operaciones funcionales sobre las colecciones.

```java
List<Integer> numeros = List.of(1, 2, 3, 4, 5);

int suma = numeros.stream()
                  .filter(n -> n % 2 == 0)
                  .mapToInt(Integer::intValue)
                  .sum();

System.out.println("Suma de pares: " + suma);
```

### Coleccionar resultados

```java
List<String> nombres = List.of("Ana", "Luis", "Pedro", "Ana");

Set<String> unicos = nombres.stream()
                            .collect(Collectors.toSet());
System.out.println(unicos);
```

### Agrupación con `groupingBy`

```java
Map<Integer, List<String>> porLongitud = nombres.stream()
    .collect(Collectors.groupingBy(String::length));

System.out.println(porLongitud);
```

Salida:
```
{3=[Ana], 4=[Luis], 5=[Pedro]}
```

---

## 15.9. Conversión entre colecciones

```java
Set<String> conjunto = new HashSet<>(nombres);
List<String> lista = new ArrayList<>(conjunto);
```

### Desde arrays

```java
String[] array = {"A", "B", "C"};
List<String> lista = Arrays.asList(array);
```

### A arrays

```java
String[] nuevoArray = lista.toArray(new String[0]);
```

---

## 15.10. Ejemplo completo

```java
import java.util.*;
import java.util.stream.*;

public class EjemploColeccionesStreams {
    public static void main(String[] args) {
        List<String> nombres = List.of("Ana", "Pedro", "Sofía", "Luis", "Ana");

        nombres.stream()
               .distinct()
               .filter(n -> n.length() > 3)
               .sorted()
               .forEach(System.out::println);
    }
}
```

Salida:
```
Luis
Pedro
Sofía
```

---

## 15.11. Resumen

- Las colecciones son estructuras genéricas del paquete `java.util`.  
- `List`, `Set`, `Map` y `Queue` son las principales interfaces.  
- Cada implementación tiene diferentes propiedades y complejidades.  
- `Iterator` y `Iterable` permiten recorrer colecciones.  
- Los Streams proporcionan una forma declarativa y funcional de procesar colecciones.  
- `Collectors` permite reducir, agrupar y transformar datos eficientemente.
