---
title: Capítulo 12 — Excepciones y manejo de errores
description: Explicación detallada sobre las excepciones en Java, su jerarquía, bloques try-catch, finally, excepciones personalizadas y try-with-resources.
---

# Capítulo 12 — Excepciones y manejo de errores

El sistema de excepciones en Java permite **detectar y manejar errores** de forma estructurada, evitando que un fallo inesperado detenga la ejecución del programa.

---

## 12.1. Qué es una excepción

Una **excepción** es un evento que ocurre durante la ejecución del programa y que interrumpe su flujo normal.  
Cuando se produce una excepción, se crea un **objeto** que contiene información sobre el error (tipo, mensaje, pila de llamadas, etc.).

```java
int x = 10 / 0; // Lanza ArithmeticException
```

---

## 12.2. Jerarquía de excepciones en Java

```
Throwable
 ├── Error
 │    ├── StackOverflowError
 │    ├── OutOfMemoryError
 │    └── ...
 └── Exception
      ├── RuntimeException
      │    ├── ArithmeticException
      │    ├── NullPointerException
      │    ├── IndexOutOfBoundsException
      │    └── ...
      └── IOException
           ├── FileNotFoundException
           ├── EOFException
           └── ...
```

### Tipos principales
- **Errores (`Error`)** → problemas graves del sistema (no deben capturarse normalmente).  
- **Excepciones comprobadas (`checked`)** → deben manejarse o declararse.  
- **Excepciones no comprobadas (`unchecked`)** → pueden manejarse, pero no es obligatorio.

---

## 12.3. Manejo de excepciones: `try-catch`

Para capturar y manejar una excepción se usa un bloque `try-catch`.

```java
try {
    int resultado = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Error: división entre cero.");
}
```

El código dentro de `try` se ejecuta normalmente hasta que ocurre una excepción.  
Si se lanza una excepción, se busca un bloque `catch` compatible con su tipo.

---

## 12.4. Múltiples `catch`

```java
try {
    int[] numeros = {1, 2, 3};
    System.out.println(numeros[5]);
} catch (ArithmeticException e) {
    System.out.println("Error aritmético.");
} catch (ArrayIndexOutOfBoundsException e) {
    System.out.println("Índice fuera de rango.");
} catch (Exception e) {
    System.out.println("Error genérico.");
}
```

> El orden importa: los bloques más específicos deben ir antes que los generales.

---

## 12.5. Bloque `finally`

El bloque `finally` se ejecuta siempre, **haya o no excepción**.  
Se usa para cerrar recursos, liberar memoria o realizar tareas finales.

```java
try {
    int resultado = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Error: " + e.getMessage());
} finally {
    System.out.println("Fin del bloque try-catch.");
}
```

Salida:
```
Error: / by zero
Fin del bloque try-catch.
```

---

## 12.6. Lanzar excepciones manualmente (`throw`)

El programador puede lanzar excepciones explícitamente usando `throw`.

```java
public void dividir(int a, int b) {
    if (b == 0) {
        throw new ArithmeticException("No se puede dividir entre cero.");
    }
    System.out.println(a / b);
}
```

---

## 12.7. Declarar excepciones (`throws`)

Un método puede **declarar** que lanza excepciones comprobadas con `throws`.

```java
public void leerArchivo(String ruta) throws IOException {
    FileReader fr = new FileReader(ruta);
    fr.close();
}
```

El método que lo llama debe manejar o propagar la excepción:

```java
try {
    leerArchivo("datos.txt");
} catch (IOException e) {
    System.out.println("Error de lectura: " + e.getMessage());
}
```

---

## 12.8. Excepciones personalizadas

Se pueden crear excepciones propias extendiendo la clase `Exception` o `RuntimeException`.

```java
public class EdadInvalidaException extends Exception {
    public EdadInvalidaException(String mensaje) {
        super(mensaje);
    }
}
```

Uso:

```java
public void registrarEdad(int edad) throws EdadInvalidaException {
    if (edad < 0 || edad > 120) {
        throw new EdadInvalidaException("Edad no válida: " + edad);
    }
    System.out.println("Edad registrada: " + edad);
}
```

```java
try {
    registrarEdad(-5);
} catch (EdadInvalidaException e) {
    System.out.println(e.getMessage());
}
```

---

## 12.9. Try con recursos (`try-with-resources`)

Introducido en Java 7, garantiza que los recursos (archivos, sockets, etc.) se cierren automáticamente.

```java
import java.io.*;

public class LecturaArchivo {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("datos.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                System.out.println(linea);
            }
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        }
    }
}
```

El recurso declarado dentro del paréntesis debe implementar la interfaz `AutoCloseable`.

Ventajas:
- Cierre automático de recursos.  
- Código más limpio y seguro.  
- Evita fugas de memoria o bloqueos de archivo.

---

## 12.10. Ejemplo completo

```java
import java.io.*;

public class EjemploExcepciones {
    public static void main(String[] args) {
        try {
            procesarArchivo("datos.txt");
        } catch (IOException e) {
            System.err.println("Error al procesar el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        } finally {
            System.out.println("Operación finalizada.");
        }
    }

    static void procesarArchivo(String ruta) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea = br.readLine();
            System.out.println("Primera línea: " + linea);
        }
    }
}
```

---

## 12.11. Buenas prácticas

- Capturar solo las excepciones que se puedan manejar.  
- Usar excepciones personalizadas para errores de negocio específicos.  
- Evitar `catch (Exception e)` genéricos sin necesidad.  
- Siempre cerrar recursos (preferiblemente con `try-with-resources`).  
- Incluir mensajes descriptivos en las excepciones.

---

## 12.12. Resumen

- Las excepciones representan errores controlables durante la ejecución.  
- Se manejan con bloques `try-catch` y opcionalmente `finally`.  
- Se pueden lanzar (`throw`) o declarar (`throws`).  
- Las personalizadas permiten controlar errores propios del dominio.  
- El `try-with-resources` simplifica la gestión de archivos y flujos.  
- Comprender las excepciones ayuda a escribir código robusto y mantenible.
