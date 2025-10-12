---
title: Capítulo 3 — Introducción a Java y la JVM
description: Explicación del funcionamiento de la JVM, la compilación, el bytecode y el ciclo de ejecución de programas Java.
---

# Capítulo 3 — Introducción a Java y la JVM

## 3.1. Qué es la JVM y cómo funciona

La **JVM (Java Virtual Machine)** es el componente central de la plataforma Java.  
Su función principal es **ejecutar el bytecode** generado por el compilador Java, garantizando que un mismo programa pueda ejecutarse en distintos sistemas operativos.

El lema de Java, *"Write once, run anywhere"*, se cumple gracias a la JVM.

### Componentes principales de la plataforma Java

1. **JDK (Java Development Kit)**  
   Incluye herramientas de desarrollo (compilador `javac`, debugger, utilidades, etc.).

2. **JRE (Java Runtime Environment)**  
   Contiene las bibliotecas estándar y la JVM necesarias para ejecutar programas Java.

3. **JVM (Java Virtual Machine)**  
   Interpreta el bytecode y lo ejecuta en el sistema real.

---

## 3.2. Compilación y ejecución de un programa Java

El flujo de trabajo en Java sigue tres etapas principales:

1. **Escribir el código fuente** en un archivo con extensión `.java`.  
2. **Compilarlo** con el compilador `javac`, que genera un archivo `.class` con **bytecode**.  
3. **Ejecutarlo** con la JVM usando el comando `java`.

### Ejemplo

```bash
javac HolaMundo.java
java HolaMundo
```

**Archivo fuente:**
```java
public class HolaMundo {
    public static void main(String[] args) {
        System.out.println("Hola mundo");
    }
}
```

**Resultado de la compilación:**
```
HolaMundo.class
```

Este archivo contiene el **bytecode**, un conjunto de instrucciones intermedias que la JVM entiende.

---

## 3.3. Estructura de un archivo `.class`

Los archivos `.class` son binarios y contienen:

- **Constantes y literales** utilizados en el programa.
- **Métodos** y sus instrucciones en bytecode.
- **Información de depuración** (nombres de variables, etc.).

Para inspeccionar su contenido, se puede usar la herramienta `javap`:

```bash
javap -c HolaMundo.class
```

Esto muestra las instrucciones bytecode que ejecuta la JVM.

---

## 3.4. Tipos de programas en Java

Java permite desarrollar distintos tipos de aplicaciones:

| Tipo de aplicación | Descripción | Ejemplo |
|--------------------|-------------|----------|
| **Aplicaciones de consola** | Programas que se ejecutan en terminal. | Herramientas, utilidades, scripts. |
| **Aplicaciones de escritorio** | Usan bibliotecas gráficas como Swing o JavaFX. | Calculadoras, editores de texto. |
| **Aplicaciones web** | Se ejecutan en servidores con frameworks como Spring o Jakarta EE. | Portales web, APIs REST. |
| **Aplicaciones móviles** | Basadas en Android (usa una variante de la JVM llamada ART). | Apps para teléfonos Android. |

---

## 3.5. Carga y ejecución de clases

La JVM no carga todas las clases al inicio. Utiliza un sistema dinámico llamado **ClassLoader**.

### Fases de ejecución

1. **Carga:** se lee el archivo `.class` y se convierte en una clase interna de la JVM.  
2. **Verificación:** se comprueba que el bytecode sea seguro y cumpla las reglas de Java.  
3. **Preparación:** se asigna memoria a las variables estáticas.  
4. **Inicialización:** se ejecutan los bloques `static` y constructores.  
5. **Ejecución:** se invoca el método `main()` u otro punto de entrada.

---

## 3.6. El método `main`: punto de entrada

Todo programa Java comienza su ejecución en el método `main`:

```java
public static void main(String[] args)
```

### Significado de cada parte:
- `public`: puede ser accedido por la JVM desde fuera de la clase.  
- `static`: puede ejecutarse sin crear una instancia de la clase.  
- `void`: no devuelve ningún valor.  
- `String[] args`: recibe los argumentos de la línea de comandos.

### Ejemplo con argumentos

```java
public class Argumentos {
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println("Argumento: " + arg);
        }
    }
}
```

Ejecutar con:
```bash
java Argumentos hola mundo
```

Salida:
```
Argumento: hola
Argumento: mundo
```

---

## 3.7. Argumentos de línea de comandos

Los argumentos son útiles para pasar configuraciones o parámetros al programa sin modificar el código fuente.

Ejemplo práctico:

```java
public class Suma {
    public static void main(String[] args) {
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        System.out.println("Suma = " + (a + b));
    }
}
```

Ejecución:
```bash
java Suma 4 5
```
Salida:
```
Suma = 9
```

---

## 3.8. Recolección de basura (Garbage Collector)

La JVM gestiona la memoria automáticamente mediante un proceso llamado **garbage collection**.

### Concepto
Cada vez que se crea un objeto con `new`, se reserva memoria en el **heap**.  
Cuando un objeto deja de tener referencias activas, la JVM lo marca para ser eliminado.

```java
Persona p = new Persona("Juan");
p = null; // El objeto podrá ser recolectado
```

### Características del recolector
- Se ejecuta de forma automática y periódica.  
- Libera memoria sin intervención del programador.  
- No garantiza cuándo se destruirá un objeto.

### Método `System.gc()`
Puede sugerir a la JVM que ejecute el recolector, aunque **no se garantiza** su ejecución inmediata.

```java
System.gc();
```

---

## 3.9. Resumen

- La JVM ejecuta el bytecode generado por el compilador `javac`.  
- El flujo de trabajo básico es: escribir → compilar → ejecutar.  
- El método `main()` es el punto de entrada de cualquier aplicación Java.  
- La memoria es gestionada automáticamente mediante el recolector de basura.  
- Comprender la JVM es esencial para optimizar el rendimiento y la portabilidad del código Java.
