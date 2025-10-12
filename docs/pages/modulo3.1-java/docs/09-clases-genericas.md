---
title: Capítulo 9 — Clases genéricas
description: Explicación del uso de clases e interfaces genéricas en Java, sus restricciones y ejemplos prácticos.
---

# Capítulo 9 — Clases genéricas

Las **clases genéricas** permiten definir estructuras que funcionan con diferentes tipos de datos sin duplicar código.  
Se introdujeron en **Java 5** para mejorar la seguridad de tipos y evitar conversiones manuales (*casting*).

---

## 9.1. Qué es una clase genérica

Una clase genérica declara **parámetros de tipo** entre los símbolos `< >`, que se sustituyen por tipos concretos al usar la clase.

```java
public class Caja<T> {
    private T contenido;

    public void setContenido(T valor) {
        contenido = valor;
    }

    public T getContenido() {
        return contenido;
    }
}
```

Uso:

```java
Caja<String> c1 = new Caja<>();
c1.setContenido("Hola");

Caja<Integer> c2 = new Caja<>();
c2.setContenido(123);

System.out.println(c1.getContenido()); // Hola
System.out.println(c2.getContenido()); // 123
```

Aquí `T` es un **parámetro de tipo** que se reemplaza por `String` o `Integer` según el caso.

---

## 9.2. Beneficios de la programación genérica

- Evita errores de tipo en tiempo de ejecución.  
- El compilador verifica los tipos en tiempo de compilación.  
- Reduce la necesidad de *casting* explícito.  
- Reutiliza código de forma más segura y flexible.

---

## 9.3. Interfaces genéricas

También pueden definirse interfaces genéricas.

```java
public interface Contenedor<T> {
    void agregar(T elemento);
    T obtener();
}
```

```java
public class ContenedorSimple<T> implements Contenedor<T> {
    private T elemento;
    public void agregar(T elemento) { this.elemento = elemento; }
    public T obtener() { return elemento; }
}
```

---

## 9.4. Métodos genéricos

Los métodos también pueden tener sus propios parámetros de tipo.

```java
public class Util {
    public static <T> void imprimir(T valor) {
        System.out.println("Valor: " + valor);
    }
}
```

Uso:

```java
Util.imprimir("Hola");
Util.imprimir(123);
```

El parámetro `<T>` en el método **no tiene por qué coincidir** con el de la clase.

---

## 9.5. Múltiples parámetros de tipo

Una clase o interfaz puede tener más de un tipo genérico.

```java
public class Par<K, V> {
    private K clave;
    private V valor;

    public Par(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public K getClave() { return clave; }
    public V getValor() { return valor; }
}
```

Uso:

```java
Par<Integer, String> p = new Par<>(1, "Uno");
System.out.println(p.getClave() + ": " + p.getValor());
```

---

## 9.6. Limitaciones de los tipos genéricos

Dentro de una clase genérica **no se puede**:

1. Crear instancias del tipo genérico:
   ```java
   // No permitido:
   // T obj = new T();
   ```

2. Usar `instanceof` con el tipo genérico:
   ```java
   // No permitido:
   // if (obj instanceof T) ...
   ```

Esto se debe a que la información del tipo genérico se **elimina en tiempo de ejecución** (mecanismo llamado *type erasure*).

---

## 9.7. Restricciones de tipo (*bounded types*)

Se puede restringir el parámetro genérico a un tipo o jerarquía concreta.

```java
public class CajaNumerica<T extends Number> {
    private T valor;
    public CajaNumerica(T valor) { this.valor = valor; }
    public double doble() { return valor.doubleValue() * 2; }
}
```

Uso:

```java
CajaNumerica<Integer> c1 = new CajaNumerica<>(10);
CajaNumerica<Double> c2 = new CajaNumerica<>(3.14);
System.out.println(c1.doble()); // 20.0
System.out.println(c2.doble()); // 6.28
```

> No se puede usar un tipo que no herede de `Number` (por ejemplo, `String`).

---

## 9.8. Clases genéricas con reflexión

Aunque no se puede instanciar directamente `T`, puede hacerse si se pasa el tipo de clase como parámetro.

```java
public class Fabrica<T> {
    private Class<T> tipo;

    public Fabrica(Class<T> tipo) {
        this.tipo = tipo;
    }

    public T crearInstancia() throws Exception {
        return tipo.getDeclaredConstructor().newInstance();
    }
}
```

Uso:

```java
Fabrica<Persona> fabrica = new Fabrica<>(Persona.class);
Persona p = fabrica.crearInstancia();
```

---

## 9.9. *Wildcards* (`?`)

El símbolo `?` representa un **tipo desconocido**.  
Permite declarar métodos que acepten colecciones genéricas sin conocer su tipo exacto.

### Ejemplo sin *wildcard*
```java
void imprimir(List<Object> lista) { ... }
```
No permite pasar `List<String>` ni `List<Integer>`.

### Con *wildcard*
```java
void imprimir(List<?> lista) {
    for (Object o : lista) {
        System.out.println(o);
    }
}
```

### *Wildcards* acotados

- `<? extends T>` → cualquier subtipo de `T`  
- `<? super T>` → cualquier supertipo de `T`

```java
List<? extends Number> lista1 = List.of(1, 2.5, 3f);
List<? super Integer> lista2 = new ArrayList<Number>();
```

---

## 9.10. Comparación con colecciones sin genéricos

Antes de Java 5, las colecciones almacenaban objetos `Object`, y era necesario hacer *casting*.

```java
List lista = new ArrayList();
lista.add("Texto");
String s = (String) lista.get(0);
```

Con genéricos:

```java
List<String> lista = new ArrayList<>();
lista.add("Texto");
String s = lista.get(0); // Sin casting
```

---

## 9.11. Ejemplo completo

```java
import java.util.*;

public class CajaDemo {
    public static void main(String[] args) {
        Caja<String> c1 = new Caja<>();
        c1.setContenido("Genéricos en Java");

        Caja<Integer> c2 = new Caja<>();
        c2.setContenido(123);

        List<Caja<?>> lista = List.of(c1, c2);

        for (Caja<?> c : lista) {
            System.out.println("Contenido: " + c.getContenido());
        }
    }
}
```

Salida:
```
Contenido: Genéricos en Java
Contenido: 123
```

---

## 9.12. Resumen

- Los genéricos permiten escribir clases e interfaces independientes del tipo de dato.  
- Mejoran la seguridad y evitan errores de *casting*.  
- Se pueden limitar los tipos permitidos con `extends` o `super`.  
- No se puede usar `new T()` ni `instanceof T` dentro de una clase genérica.  
- Los *wildcards* (`?`) permiten flexibilidad en métodos con colecciones.  
- Son la base de las colecciones modernas de Java.
