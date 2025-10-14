---
title: Capítulo 3 — Introducción a Java y la JVM
description: Explicación del funcionamiento de la JVM, la compilación, el bytecode y el ciclo de ejecución de programas Java.
---

# Capítulo 3 — Introducción a Java y la JVM

## 3.1. Qué es la JVM y cómo funciona

La **JVM (Java Virtual Machine)** (Máquina Virtual de Java) es el componente central de la plataforma Java.
Su función principal es **ejecutar el bytecode** generado por el compilador Java, independientemente del sistema operativo en el que se haya instalado la JVM.

Esto garantiza que un mismo programa pueda ejecutarse en distintos sistemas operativos. La única condición es que exista una versión de la JVM instalada.

El bytecode es el código máquina o código binario que resulta al compilar código fuente.

El bytecode es distinto dependiendo de la máquina y el sistema operativo para el que se haya compilado. Esto ocurre porque el bytecode es un código a bajo nivel que trata directamente con elementos del sistema operativo y del procesador de la maquina donde se ejecuta. Cada sistema operativo tiene sus formas de usar sus elementos, y cada procesador tiene su propio juego de instrucciones y sus propios elementos físicos (como los registros).

Por ello la JVM que instalamos en Windows no es la misma que la que instalamos en Linux o en Mac. El bytecode de la JVM será el que tenga que ser para cada máquina.

Pero un programa Java no se ejecuta directamente sobre la máquina física, si no sobre la JVM. Y los elementos y operaciones que ofrece una JVM de una versión númerica determinada (ej. JVM 25) para un sistema son exactamente los mismos que los que ofrezca otra JVM con la misma versión numérica en otro sistema. O lo que es lo mismo, que todas las JVM con la misma versión númerica son compatibles entre sí.

Por tanto, el ejecutable de un programa Java que se ejecute en una versión numérica determinada de JVM para un sistema, se ejecutará sin problemas en cualquier JVM de cualquier sistema con el mismo número de versión.

**Un programa Java compilado funciona igual en cualquier dispositivo que tenga una JVM instalada, siempre que sean de la misma versión numérica.**

**Por tanto podemos copiar un programa Java compilado (un archivo `.jar` por ejemplo) de un Windows a un Linux o Mac y funcionarán igual.**

De hecho, el lema de Java es: *"Write once, run anywhere"* (*"Escribe [el código] una vez, ejecútalo en cualquier sitio"*).


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
public static void main(String[] args) {
    ...
}
```

### Significado de cada parte:
- `public`: puede ser accedido por la JVM desde fuera de la clase.  
- `static`: puede ejecutarse sin crear una instancia de la clase.  
- `void`: no devuelve ningún valor.  
- `String[] args`: recibe los argumentos de la línea de comandos.

### Ejemplo con argumentos

Argumentos.java
```java
public class Argumentos {
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println("Argumento: " + arg);
        }
    }
}
```

Compilar con con:
```bash
javac ./Argumentos.java
```
_(produce un archivo Argumentos.class con el código compilado)._


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
