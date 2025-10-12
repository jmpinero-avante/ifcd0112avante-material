---
title: Capítulo 10 — Utilidades del lenguaje
description: Clases y herramientas básicas incluidas en Java para operaciones matemáticas, manejo de cadenas, fechas y entrada de datos.
---

# Capítulo 10 — Utilidades del lenguaje

La biblioteca estándar de Java incluye numerosas clases utilitarias dentro de los paquetes `java.lang` y `java.util`.  
Estas clases proporcionan métodos estáticos y herramientas para realizar tareas comunes como operaciones matemáticas, generación de números aleatorios, lectura de datos o manipulación de fechas y horas.

---

## 10.1. Clase `Math`

La clase `Math` contiene métodos y constantes para realizar operaciones matemáticas básicas.  
Todos sus métodos son **estáticos**, por lo que no es necesario crear objetos.

### Constantes
```java
Math.PI      // 3.141592653589793
Math.E       // 2.718281828459045
```

### Métodos comunes

| Método | Descripción | Ejemplo |
|---------|--------------|----------|
| `abs(x)` | Valor absoluto | `Math.abs(-5)` → `5` |
| `pow(a,b)` | Potencia a^b | `Math.pow(2,3)` → `8` |
| `sqrt(x)` | Raíz cuadrada | `Math.sqrt(16)` → `4` |
| `round(x)` | Redondeo | `Math.round(3.7)` → `4` |
| `floor(x)` | Redondeo hacia abajo | `Math.floor(3.7)` → `3` |
| `ceil(x)` | Redondeo hacia arriba | `Math.ceil(3.1)` → `4` |
| `max(a,b)` / `min(a,b)` | Mayor / menor valor | `Math.max(5,10)` → `10` |
| `random()` | Número aleatorio [0,1) | `Math.random()` → `0.3478` |

---

## 10.2. Clase `Random`

La clase `java.util.Random` ofrece un control más preciso sobre la generación de números aleatorios.

```java
import java.util.Random;

Random r = new Random();
int n = r.nextInt(10);  // Entero entre 0 y 9
double d = r.nextDouble(); // Decimal entre 0.0 y 1.0
boolean b = r.nextBoolean();
```

### Diferencias entre `Math.random()` y `Random`
| Característica | `Math.random()` | `Random` |
|----------------|----------------|-----------|
| Tipo | Método estático | Clase |
| Rango | `[0.0, 1.0)` | Personalizable |
| Control de semilla | No | Sí (`new Random(seed)`) |
| Ideal para | Casos simples | Simulaciones, juegos, estadísticas |

---

## 10.3. Clase `Scanner`

La clase `java.util.Scanner` permite leer datos desde distintas fuentes, incluyendo el teclado (`System.in`).

```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);
System.out.print("Introduce tu nombre: ");
String nombre = sc.nextLine();

System.out.print("Introduce tu edad: ");
int edad = sc.nextInt();

System.out.println("Hola " + nombre + ", tienes " + edad + " años.");
sc.close();
```

### Métodos principales

| Método | Descripción |
|--------|--------------|
| `next()` | Lee una palabra |
| `nextLine()` | Lee una línea completa |
| `nextInt()` | Lee un número entero |
| `nextDouble()` | Lee un número decimal |
| `hasNext()` | Comprueba si hay más entradas |

---

## 10.4. Manipulación avanzada de cadenas (`String`, `StringBuilder`, `String.format`)

### `StringBuilder`
Ideal para concatenar cadenas repetidamente.

```java
StringBuilder sb = new StringBuilder();
for (int i = 1; i <= 3; i++) {
    sb.append("Item ").append(i).append(" ");
}
System.out.println(sb.toString()); // "Item 1 Item 2 Item 3 "
```

### `String.format()` y `printf()`

Permiten generar texto con formato controlado, compatible con `System.out.printf()`.

```java
String nombre = "Ana";
int edad = 22;
String mensaje = String.format("Mi nombre es %s y tengo %d años", nombre, edad);
System.out.println(mensaje);
System.out.printf("Edad en hexadecimal: %x%n", edad);
```

#### Principales formatos

| Formato | Tipo | Ejemplo |
|----------|------|----------|
| `%s` | Cadena | `"Hola"` |
| `%d` | Entero decimal | `25` |
| `%f` | Decimal | `3.14` |
| `%x` | Hexadecimal | `0x19` |
| `%n` | Nueva línea | salto de línea |
| `%.2f` | Decimal con 2 cifras | `3.14` → `"3.14"` |

---

## 10.5. Fechas y horas con `java.time`

El paquete `java.time` (Java 8+) reemplaza las antiguas clases `Date` y `Calendar`, ofreciendo una API moderna e inmutable.

### Clases principales

| Clase | Descripción | Ejemplo |
|--------|--------------|----------|
| `LocalDate` | Fecha (sin hora) | `LocalDate.now()` |
| `LocalTime` | Hora (sin fecha) | `LocalTime.now()` |
| `LocalDateTime` | Fecha y hora local | `LocalDateTime.now()` |
| `ZonedDateTime` | Fecha y hora con zona horaria | `ZonedDateTime.now()` |
| `Duration` | Diferencia entre dos tiempos | `Duration.between(t1, t2)` |
| `Period` | Diferencia entre fechas | `Period.between(d1, d2)` |

```java
import java.time.*;

LocalDate hoy = LocalDate.now();
LocalDate cumple = LocalDate.of(1990, 5, 20);
Period edad = Period.between(cumple, hoy);

System.out.println("Tienes " + edad.getYears() + " años.");
```

### Formato estándar ISO 8601

Los tipos `LocalDate` y `LocalDateTime` usan el formato `YYYY-MM-DD` y `YYYY-MM-DDTHH:mm:ss`.

```java
LocalDateTime fecha = LocalDateTime.now();
System.out.println(fecha); // 2025-10-12T16:30:00
```

### Formateo personalizado

```java
import java.time.format.DateTimeFormatter;

DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String salida = fecha.format(fmt);
System.out.println(salida);
```

### Zonas horarias y UTC

```java
ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
System.out.println("UTC: " + utc);
```

Guardar fechas en UTC evita errores por cambio horario o diferencias regionales.

---

## 10.6. Clase `Objects`

`java.util.Objects` ofrece métodos útiles para trabajar con referencias nulas.

| Método | Descripción | Ejemplo |
|---------|--------------|----------|
| `requireNonNull(obj)` | Lanza `NullPointerException` si el objeto es nulo | `Objects.requireNonNull(x)` |
| `equals(a,b)` | Compara con seguridad nula | `Objects.equals(a, b)` |
| `hash(a...)` | Calcula un hash combinado | `Objects.hash(nombre, edad)` |

---

## 10.7. Clase `Optional`

`Optional<T>` evita errores por referencias nulas (*NullPointerException*).

```java
import java.util.Optional;

Optional<String> nombre = Optional.of("Ana");
System.out.println(nombre.orElse("Desconocido")); // "Ana"

Optional<String> vacio = Optional.empty();
System.out.println(vacio.orElse("Sin valor")); // "Sin valor"
```

### Métodos principales

| Método | Descripción |
|---------|--------------|
| `of(valor)` | Crea un `Optional` no nulo |
| `empty()` | Crea un `Optional` vacío |
| `isPresent()` | Comprueba si hay valor |
| `orElse(valor)` | Devuelve un valor alternativo |
| `ifPresent(Consumer)` | Ejecuta acción si hay valor |

---

## 10.8. Resumen

- `Math` proporciona operaciones matemáticas estáticas.  
- `Random` permite generar números aleatorios reproducibles.  
- `Scanner` facilita la lectura desde teclado.  
- `StringBuilder` mejora el rendimiento al construir cadenas.  
- `java.time` gestiona fechas y horas modernas con soporte para zonas horarias.  
- `Objects` y `Optional` ayudan a manejar referencias nulas de forma segura.

