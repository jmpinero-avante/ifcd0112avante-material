---
title: Capítulo 4 — Tipos de datos y operadores
description: Explicación de los tipos primitivos, cadenas, casting, clases envolventes y operadores básicos en Java.
---

# Capítulo 4 — Tipos de datos y operadores

## 4.1. Tipos primitivos de datos

Java define ocho **tipos primitivos** que almacenan valores simples, no objetos.  
Cada tipo ocupa un tamaño fijo de memoria y tiene un rango definido.

| Tipo | Tamaño | Rango aproximado | Ejemplo |
|------|---------|------------------|----------|
| `byte` | 8 bits | -128 a 127 | `byte b = 5;` |
| `short` | 16 bits | -32.768 a 32.767 | `short s = 1000;` |
| `int` | 32 bits | -2.147.483.648 a 2.147.483.647 | `int x = 42;` |
| `long` | 64 bits | ±9 × 10¹⁸ | `long l = 123456789L;` |
| `float` | 32 bits | ±3.4 × 10³⁸ (6-7 decimales) | `float f = 3.14f;` |
| `double` | 64 bits | ±1.7 × 10³⁰⁸ (15 decimales) | `double d = 3.14159;` |
| `char` | 16 bits | Unicode (0 a 65.535) | `char c = 'A';` |
| `boolean` | 1 bit (valor lógico) | `true` o `false` | `boolean ok = true;` |

---

### Caracteres (`char`)

El tipo `char` representa un único carácter Unicode.  
Cada carácter tiene asociado un código numérico entero.

#### Conversión `char` → `int` y `int` → `char`

```java
char letra = 'A';
int codigo = letra;        // Conversión implícita
System.out.println(codigo); // 65

int numero = 66;
char siguiente = (char) numero; // Conversión explícita
System.out.println(siguiente);  // 'B'
```

#### Ejemplo con `for-each`

```java
String texto = "JAVA";
for (char c : texto.toCharArray()) {
    System.out.println(c + " → " + (int) c);
}
```

#### Tabla Unicode básica

| Carácter | Código | Carácter | Código | Carácter | Código |
|-----------|---------|-----------|---------|-----------|---------|
| A | 65 | a | 97 | 0 | 48 |
| B | 66 | b | 98 | 1 | 49 |
| C | 67 | c | 99 | 2 | 50 |
| ... | ... | ... | ... | ... | ... |
| Z | 90 | z | 122 | 9 | 57 |
| Espacio | 32 | Nueva línea (`\n`) | 10 | Tabulación (`\t`) | 9 |

---

## 4.2. Cadenas de texto (`String`)

Una cadena (`String`) es una **secuencia inmutable** de caracteres.  
Cuando concatenamos o modificamos un texto, en realidad se crea un nuevo objeto `String`.

```java
String saludo = "Hola";
saludo += " mundo";
System.out.println(saludo); // "Hola mundo"
```

### `StringBuilder`

Cuando se necesita construir cadenas de manera repetitiva, se recomienda usar `StringBuilder`,  
ya que **no crea nuevos objetos en cada concatenación**.

```java
StringBuilder sb = new StringBuilder("Hola");
sb.append(" mundo");
System.out.println(sb.toString());
```

### Formateo de cadenas

Se pueden usar los métodos `String.format()` o `System.out.printf()` para generar texto formateado:

```java
String nombre = "Juan";
int edad = 25;
System.out.printf("Mi nombre es %s y tengo %d años.%n", nombre, edad);
```

| Formato | Descripción | Ejemplo |
|----------|-------------|----------|
| `%s` | Cadena (`String`) | `"Hola"` |
| `%d` | Entero decimal | `42` |
| `%f` | Número decimal (float/double) | `3.14` |
| `%n` | Nueva línea | salto de línea |
| `%.2f` | Decimal con 2 cifras | `3.14` → `3.14` |
| `%c` | Carácter | `'A'` |

---

## 4.3. Conversión de tipos (*casting*)

El *casting* permite convertir valores entre distintos tipos numéricos.  
Existen dos tipos de conversión: **implícita** y **explícita**.

### Conversión implícita

Se realiza automáticamente cuando el tipo de destino tiene mayor capacidad.

```java
int a = 5;
double b = a; // Conversión implícita (int → double)
```

### Conversión explícita

Debe indicarse manualmente mediante paréntesis:

```java
double x = 9.7;
int y = (int) x; // Trunca el valor a 9
```

### Casting entre tipos numéricos y `char`

```java
int codigo = 65;
char letra = (char) codigo; // 'A'

char c = 'B';
int valor = c;              // 66
```

### Reglas
- Puede perderse precisión en conversiones descendentes.  
- No se puede convertir tipos no compatibles (por ejemplo, `boolean` a `int`).

---

## 4.4. Clases envolventes (Wrapper Classes)

Cada tipo primitivo tiene su **clase envolvente**, que permite tratarlo como objeto:

| Tipo primitivo | Clase envolvente |
|----------------|------------------|
| `byte` | `Byte` |
| `short` | `Short` |
| `int` | `Integer` |
| `long` | `Long` |
| `float` | `Float` |
| `double` | `Double` |
| `char` | `Character` |
| `boolean` | `Boolean` |

Ejemplo:

```java
int n = 5;
Integer objeto = n; // Autoboxing
int valor = objeto; // Unboxing
```

### Método `parse`

Permite convertir una cadena en su tipo numérico correspondiente.

```java
int edad = Integer.parseInt("25");
double pi = Double.parseDouble("3.1416");
```

### Método `valueOf()`

Devuelve un objeto envolvente:

```java
Integer i = Integer.valueOf(10);
Double d = Double.valueOf(3.14);
```

---

## 4.5. Operadores en Java

Los operadores permiten realizar operaciones sobre variables y valores.

### Aritméticos

| Operador | Descripción | Ejemplo |
|-----------|-------------|----------|
| `+` | Suma o concatenación | `a + b` |
| `-` | Resta | `a - b` |
| `*` | Multiplicación | `a * b` |
| `/` | División | `a / b` |
| `%` | Módulo (resto) | `a % b` |

> El operador módulo también puede aplicarse a números decimales.

### Incremento y decremento

| Operador | Ejemplo | Descripción |
|-----------|----------|-------------|
| `++x` | Pre-incremento | Incrementa antes de usar el valor |
| `x++` | Post-incremento | Usa el valor y luego incrementa |
| `--x`, `x--` | Decrementos equivalentes | |

Ejemplo:

```java
int x = 5;
System.out.println(++x); // 6
System.out.println(x++); // 6, luego x = 7
```

### Asignaciones compuestas

| Operador | Equivalente |
|-----------|-------------|
| `+=` | `x = x + valor` |
| `-=` | `x = x - valor` |
| `*=` | `x = x * valor` |
| `/=` | `x = x / valor` |
| `%=` | `x = x % valor` |

Ejemplo:

```java
int total = 10;
total += 5; // total = 15
```

### Relacionales

| Operador | Descripción |
|-----------|-------------|
| `==` | Igualdad |
| `!=` | Diferente |
| `>` | Mayor que |
| `<` | Menor que |
| `>=` | Mayor o igual |
| `<=` | Menor o igual |

### Lógicos

| Operador | Descripción | Ejemplo |
|-----------|-------------|----------|
| `&&` | AND lógico | `(a > 0 && b > 0)` |
| `||` | OR lógico | `(a > 0 || b > 0)` |
| `!` | NOT lógico | `!(a > b)` |

### Bit a bit

Operan sobre la representación binaria de los números.

| Operador | Descripción | Ejemplo |
|-----------|-------------|----------|
| `&` | AND binario | `a & b` |
| `|` | OR binario | `a | b` |
| `^` | XOR binario | `a ^ b` |
| `~` | NOT binario | `~a` |
| `<<` | Desplazamiento a la izquierda | `a << 1` |
| `>>` | Desplazamiento a la derecha | `a >> 1` |

---

## 4.6. Entrada, salida y error estándar

Java define tres flujos básicos:

| Flujo | Descripción | Objeto |
|--------|--------------|---------|
| Entrada estándar | Datos introducidos por teclado | `System.in` |
| Salida estándar | Salida normal por consola | `System.out` |
| Error estándar | Mensajes de error | `System.err` |

Ejemplo básico:

```java
System.out.println("Texto normal");
System.err.println("Mensaje de error");
```

> El flujo de entrada (`System.in`) se verá en detalle en el capítulo de E/S.

---

## 4.7. Resumen

- Java define 8 tipos primitivos y sus respectivas clases envolventes.  
- Las cadenas (`String`) son inmutables; para modificarlas se usa `StringBuilder`.  
- El *casting* permite convertir tipos de datos de forma implícita o explícita.  
- Existen operadores aritméticos, relacionales, lógicos y bit a bit.  
- Los flujos estándar (`System.in`, `System.out`, `System.err`) controlan la entrada y salida de datos.
