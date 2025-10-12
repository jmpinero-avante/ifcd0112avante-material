---
title: Capítulo 6 — Estructuras de control del flujo
description: Explicación detallada de las estructuras condicionales, bucles, operadores de control y recursividad en Java.
---

# Capítulo 6 — Estructuras de control del flujo

Las **estructuras de control del flujo** determinan el orden en que se ejecutan las instrucciones de un programa.  
Java proporciona estructuras para **tomar decisiones**, **repetir bloques de código** y **controlar el flujo de ejecución**.

---

## 6.1. Condicionales

Las estructuras condicionales permiten ejecutar distintos bloques de código dependiendo de una condición.

### `if` y `else if`

```java
int edad = 20;

if (edad >= 18) {
    System.out.println("Eres mayor de edad.");
} else {
    System.out.println("Eres menor de edad.");
}
```

También se pueden encadenar múltiples condiciones:

```java
if (nota >= 9) {
    System.out.println("Sobresaliente");
} else if (nota >= 7) {
    System.out.println("Notable");
} else if (nota >= 5) {
    System.out.println("Aprobado");
} else {
    System.out.println("Suspenso");
}
```

---

### `switch`

El `switch` permite comparar una misma variable contra varios valores posibles.

```java
int dia = 3;

switch (dia) {
    case 1:
        System.out.println("Lunes");
        break;
    case 2:
        System.out.println("Martes");
        break;
    case 3:
        System.out.println("Miércoles");
        break;
    default:
        System.out.println("Día no válido");
}
```

#### Versión moderna del `switch` (Java 14+)

Permite devolver valores directamente y evita el uso de `break`.

```java
String nombreDia = switch (dia) {
    case 1 -> "Lunes";
    case 2 -> "Martes";
    case 3 -> "Miércoles";
    default -> "Desconocido";
};

System.out.println(nombreDia);
```

Ventajas:
- Sintaxis más limpia.
- Sin necesidad de `break`.
- Puede devolver resultados.

---

### Operador ternario

El operador ternario `?:` es una forma abreviada de escribir una estructura `if-else` simple.

```java
int edad = 20;
String mensaje = (edad >= 18) ? "Mayor de edad" : "Menor de edad";
System.out.println(mensaje);
```

---

## 6.2. Bucles

Los **bucles** permiten repetir un bloque de instrucciones varias veces.

### `while`

Ejecuta un bloque **mientras** la condición sea verdadera.

```java
int i = 0;
while (i < 5) {
    System.out.println("i = " + i);
    i++;
}
```

### `do-while`

Ejecuta el bloque **al menos una vez**, y luego evalúa la condición.

```java
int i = 0;
do {
    System.out.println("i = " + i);
    i++;
} while (i < 5);
```

### `for`

Ideal cuando se conoce el número exacto de repeticiones.

```java
for (int i = 0; i < 5; i++) {
    System.out.println("Contador: " + i);
}
```

### `for-each`

Recorre los elementos de un array o colección.

```java
String[] nombres = {"Ana", "Luis", "María"};
for (String nombre : nombres) {
    System.out.println(nombre);
}
```

---

## 6.3. Palabras clave `break` y `continue`

### `break`
Sale inmediatamente del bucle más cercano.

```java
for (int i = 0; i < 10; i++) {
    if (i == 5) break;
    System.out.println(i);
}
```

### `continue`
Salta a la siguiente iteración del bucle sin ejecutar el resto del bloque actual.

```java
for (int i = 0; i < 10; i++) {
    if (i % 2 == 0) continue;
    System.out.println(i); // Solo imprime los impares
}
```

---

## 6.4. Bucles anidados

Un bucle puede estar dentro de otro.

```java
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 2; j++) {
        System.out.println("i=" + i + ", j=" + j);
    }
}
```

---

## 6.5. Recursividad

La **recursividad** consiste en que un método se llame a sí mismo para resolver un problema más pequeño.

Ejemplo clásico: cálculo del factorial de un número.

```java
public static int factorial(int n) {
    if (n == 0) return 1; // Caso base
    return n * factorial(n - 1);
}
```

```java
System.out.println(factorial(5)); // 120
```

### Importante
- Toda función recursiva debe tener un **caso base** que detenga las llamadas.  
- Una recursión sin caso base provoca un **StackOverflowError**.  
- Casi cualquier algoritmo recursivo puede expresarse mediante bucles iterativos, aunque no siempre con igual claridad.

---

## 6.6. Combinando estructuras de control

Las estructuras condicionales y los bucles pueden combinarse para resolver problemas complejos.

Ejemplo: recorrer una lista y contar los elementos que cumplen una condición.

```java
int[] numeros = {2, 5, 8, 11, 14};
int contador = 0;

for (int n : numeros) {
    if (n % 2 == 0) {
        contador++;
    }
}

System.out.println("Números pares: " + contador);
```

---

## 6.7. Resumen

- `if`, `switch` y el operador ternario permiten tomar decisiones.  
- `for`, `while` y `do-while` controlan repeticiones.  
- `break` y `continue` modifican el flujo de los bucles.  
- La recursividad permite resolver problemas repitiendo una función sobre sí misma.  
- Comprender las estructuras de control es esencial para dominar la lógica de programación en Java.
