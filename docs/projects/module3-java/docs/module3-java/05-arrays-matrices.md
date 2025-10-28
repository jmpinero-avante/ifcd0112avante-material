---
title: Capítulo 5 — Arrays y matrices
description: Explicación sobre el uso de arrays unidimensionales y multidimensionales en Java, inicialización, recorrido y clase Arrays.
---

# Capítulo 5 — Arrays y matrices

## 5.1. Qué es un array

Un **array** (o *arreglo*) es una **estructura de datos homogénea** que almacena múltiples valores del mismo tipo en posiciones contiguas de memoria.

Cada elemento se identifica mediante un **índice numérico** que comienza en **0**.

```java
int[] numeros = new int[5];
numeros[0] = 10;
numeros[1] = 20;
System.out.println(numeros[0]); // 10
```

Los arrays son objetos en Java, por lo que se almacenan en el **heap** y pueden manipularse mediante referencias.

---

## 5.2. Declaración e inicialización

### Declaración
```java
int[] edades;     // Forma recomendada
int edades[];     // También válida (estilo C)
```

### Inicialización
```java
edades = new int[3];
edades[0] = 25;
edades[1] = 30;
edades[2] = 28;
```

### Declaración e inicialización en una sola línea
```java
int[] numeros = { 10, 20, 30, 40, 50 };
```

### Longitud de un array
La propiedad `length` devuelve el número de elementos.

```java
System.out.println(numeros.length); // 5
```

---

## 5.3. Recorrido de arrays

### Bucle tradicional `for`
```java
for (int i = 0; i < numeros.length; i++) {
    System.out.println("Elemento " + i + ": " + numeros[i]);
}
```

### Bucle mejorado `for-each`
```java
for (int n : numeros) {
    System.out.println(n);
}
```

El *for-each* no permite modificar directamente los valores dentro del array.

---

## 5.4. Arrays de objetos

Los arrays también pueden almacenar referencias a objetos.

```java
String[] nombres = new String[3];
nombres[0] = "Ana";
nombres[1] = "Luis";
nombres[2] = "María";

for (String nombre : nombres) {
    System.out.println(nombre);
}
```

---

## 5.5. Arrays multidimensionales (matrices)

Un **array multidimensional** es un array cuyos elementos son otros arrays.

### Ejemplo de matriz 2D
```java
int[][] matriz = new int[2][3];
matriz[0][0] = 1;
matriz[0][1] = 2;
matriz[1][2] = 3;
```

### Inicialización directa
```java
int[][] tabla = {
    {1, 2, 3},
    {4, 5, 6}
};
```

### Recorrido de una matriz
```java
for (int i = 0; i < tabla.length; i++) {
    for (int j = 0; j < tabla[i].length; j++) {
        System.out.print(tabla[i][j] + " ");
    }
    System.out.println();
}
```

---

## 5.6. Matrices irregulares

Los arrays multidimensionales pueden ser **irregulares**, donde cada fila tiene una longitud distinta.

```java
int[][] irregular = new int[3][];
irregular[0] = new int[2];
irregular[1] = new int[4];
irregular[2] = new int[1];

irregular[0][0] = 10;
irregular[1][3] = 99;
```

### Ejemplo de impresión
```java
for (int[] fila : irregular) {
    for (int valor : fila) {
        System.out.print(valor + " ");
    }
    System.out.println();
}
```

---

## 5.7. Clase `java.util.Arrays`

La clase `Arrays` proporciona métodos estáticos para trabajar con arrays.

| Método | Descripción |
|---------|--------------|
| `sort(array)` | Ordena los elementos |
| `fill(array, valor)` | Rellena el array con un valor |
| `copyOf(array, n)` | Copia los primeros `n` elementos |
| `equals(a1, a2)` | Compara si dos arrays son iguales |
| `toString(array)` | Devuelve una representación en texto |
| `binarySearch(array, valor)` | Busca un elemento en un array ordenado |

### Ejemplo de uso
```java
import java.util.Arrays;

public class EjemploArrays {
    public static void main(String[] args) {
        int[] numeros = {5, 2, 8, 1};
        Arrays.sort(numeros);
        System.out.println(Arrays.toString(numeros)); // [1, 2, 5, 8]
    }
}
```

---

## 5.8. Conversión entre arrays y colecciones

A menudo se desea convertir un array en una colección (`List`) para aprovechar las ventajas de las colecciones dinámicas.

```java
import java.util.Arrays;
import java.util.List;

String[] nombres = {"Ana", "Luis", "María"};
List<String> lista = Arrays.asList(nombres);
```

> La lista devuelta por `Arrays.asList()` tiene tamaño fijo, por lo que no admite añadir o eliminar elementos.

---

## 5.9. Introducción a `ArrayList`

`ArrayList` es una estructura de datos dinámica del paquete `java.util` que **crece automáticamente** cuando se añaden elementos.

```java
import java.util.ArrayList;

ArrayList<String> lista = new ArrayList<>();
lista.add("Ana");
lista.add("Luis");
lista.add("María");

System.out.println(lista.get(1)); // Luis
```

### Comparativa rápida

| Característica | Array | ArrayList |
|----------------|--------|------------|
| Tamaño fijo | ✅ | ❌ (dinámico) |
| Permite tipos primitivos | ✅ | ❌ (solo objetos) |
| Métodos adicionales | ❌ | ✅ (`add`, `remove`, `contains`, etc.) |
| Eficiencia de acceso | Muy alta | Alta |

---

## 5.10. Resumen

- Los arrays permiten almacenar múltiples valores del mismo tipo.  
- Los índices comienzan en 0 y la propiedad `length` indica su tamaño.  
- Pueden ser unidimensionales o multidimensionales.  
- La clase `Arrays` proporciona utilidades para ordenar, copiar y comparar.  
- `ArrayList` es una versión dinámica y flexible basada en arrays.

