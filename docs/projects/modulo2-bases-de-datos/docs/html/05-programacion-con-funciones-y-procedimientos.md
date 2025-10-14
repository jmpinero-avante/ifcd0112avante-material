---
title: "05 – Programación con funciones y procedimientos"
---

# 5 – Programación con funciones y procedimientos
## Diferencias entre función y procedimiento
| Elemento | Función | Procedimiento |
| --- | --- | --- |
| Devuelve un valor | ✅ Sí, con RETURN | ❌ No |
| Llamada | En una SELECT o expresión | Con el comando CALL |
| Se puede usar en SQL | ✅ Sí | ❌ No directamente |
| Tiene OUT | ❌ Generalmente no | ✅ Sí |

## Ejemplo de llamada
Llamada a función:

```sql
SELECT mi_funcion(5);
```

Llamada a procedimiento:

```sql
CALL mi_procedimiento('dato');
```

## Esqueleto de una función o procedimiento
Ambos comienzan con CREATE OR REPLACE y pueden tener:

- Sección DECLARE: para definir variables internas.
- Bloque BEGIN ... END: cuerpo principal.
- Sección EXCEPTION: captura de errores.

### Función
```sql
CREATE OR REPLACE FUNCTION suma(a INT, b INT)
RETURNS INT AS $$
DECLARE
  resultado INT;
BEGIN
  resultado := a + b;
  RETURN resultado;
END;
$$ LANGUAGE plpgsql;
```

### Procedimiento
```sql
CREATE OR REPLACE PROCEDURE imprimir_suma(a INT, b INT)
AS $$
DECLARE
  resultado INT;
BEGIN
  resultado := a + b;
  RAISE NOTICE 'La suma es: %', resultado;
END;
$$ LANGUAGE plpgsql;
```

## Parámetros IN, OUT e INOUT
| Tipo | Descripción |
| --- | --- |
| IN | Valor de entrada (por defecto) |
| OUT | Parámetro que devuelve un valor |
| INOUT | Sirve como entrada y salida |

```sql
CREATE OR REPLACE PROCEDURE dame_doble(IN entrada INT, OUT salida INT)
AS $$
BEGIN
  salida := entrada * 2;
END;
$$ LANGUAGE plpgsql;
```

Llamada desde consola:
CALL dame_doble(5, x);  -- x = 10

## Nota importante sobre excepciones
Cada procedimiento o función en PostgreSQL se ejecuta automáticamente dentro de una transacción.
Por eso NO es necesario iniciar manualmente una transacción con BEGIN.
Sin embargo, si se lanza una excepción (RAISE EXCEPTION) y no se captura con EXCEPTION, se produce un rollback implícito de toda la función o procedimiento.

## Condicionales
### IF / ELSIF / ELSE
```sql
IF total > 100 THEN
  RAISE NOTICE 'Total alto';
ELSIF total = 100 THEN
  RAISE NOTICE 'Total exacto';
ELSE
  RAISE NOTICE 'Total bajo';
END IF;
```

### CASE
```sql
CASE tipo_producto
  WHEN 'A' THEN
    precio := 10;
  WHEN 'B' THEN
    precio := 15;
  ELSE
    precio := 5;
END CASE;
```

## Bucles
### LOOP con EXIT
```sql
LOOP
  total := total + 1;
  EXIT WHEN total >= 5;
END LOOP;
```

### WHILE
```sql
WHILE stock > 0 LOOP
  stock := stock - 1;
END LOOP;
```

### FOR IN SELECT
```sql
FOR fila IN SELECT * FROM productos LOOP
  RAISE NOTICE 'Producto: %', fila.nombre;
END LOOP;
```

## Variable mágica FOUND
FOUND indica si la última operación de tipo SELECT INTO, FETCH, UPDATE, DELETE, etc. encontró al menos una fila.

```sql
LOOP
  FETCH mi_cursor INTO fila;
  EXIT WHEN NOT FOUND;
  -- procesar fila
END LOOP;
```

También útil para salir de bucles si ya no hay más resultados.

## Cursores en PostgreSQL
Un cursor permite recorrer los resultados de una consulta fila por fila.

### ¿Cómo se declara un cursor?
```sql
DECLARE
  cur_empleados CURSOR FOR
    SELECT * FROM empleados ORDER BY id;
```

### Ejemplo completo: recorrido ascendente
```sql
DO $$
DECLARE
  cur_empleados CURSOR FOR
    SELECT * FROM empleados ORDER BY id;
  fila RECORD;
BEGIN
  OPEN cur_empleados;
  FETCH NEXT FROM cur_empleados INTO fila;

  WHILE FOUND LOOP
    RAISE NOTICE 'Empleado: % (% años)', fila.nombre, fila.edad;
    FETCH NEXT FROM cur_empleados INTO fila;
  END LOOP;

  CLOSE cur_empleados;
END;
$$;
```

### Ejemplo completo: recorrido descendente
```sql
DO $$
DECLARE
  cur_empleados CURSOR FOR
    SELECT * FROM empleados ORDER BY id;
  fila RECORD;
BEGIN
  OPEN cur_empleados;
  FETCH LAST FROM cur_empleados INTO fila;

  WHILE FOUND LOOP
    RAISE NOTICE 'Empleado: % (% años)', fila.nombre, fila.edad;
    FETCH PRIOR FROM cur_empleados INTO fila;
  END LOOP;

  CLOSE cur_empleados;
END;
$$;
```

### Resumen: direcciones de FETCH
| Dirección | Descripción |
| --- | --- |
| NEXT | Fila siguiente (por defecto) |
| PRIOR | Fila anterior |
| FIRST | Primera fila |
| LAST | Última fila |

- Siempre cerrar el cursor con CLOSE al final.
- Usar ORDER BY si el orden es importante.
- Utilizar FOUND para saber si FETCH devolvió fila.

## Variables tipo RECORD
Las variables RECORD permiten guardar varios campos sin declarar uno por uno.

```sql
DECLARE
  empleado RECORD;

SELECT
  * INTO empleado
FROM
   empleados
WHERE id = 1;

RAISE NOTICE 'Nombre: %, Salario: %', empleado.nombre, empleado.salario;
```

Uso con FETCH:

```sql
FETCH cur_empleados INTO empleado;
RAISE NOTICE 'ID: %, Nombre: %', empleado.id, empleado.nombre;
```

## RAISE NOTICE y RAISE EXCEPTION
```sql
RAISE NOTICE 'ID: %, Nombre: %', id, nombre;
```

```sql
IF salario < 0 THEN
  RAISE EXCEPTION 'Salario negativo: %', salario;
END IF;
```

Los placeholders (%) se sustituyen por los valores en orden.

## Bloques BEGIN … EXCEPTION
```sql
BEGIN
  SELECT * INTO empleado FROM empleados WHERE id = 999;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE NOTICE 'No se encontró el empleado';
  WHEN TOO_MANY_ROWS THEN
    RAISE NOTICE 'Demasiados resultados';
  WHEN OTHERS THEN
    RAISE NOTICE 'Error inesperado';
END;
```

Captura localizada con bloques anidados:

```sql
BEGIN
  BEGIN
    SELECT * INTO emp FROM empleados WHERE activo = TRUE;
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'Error solo aquí';
  END;
END;
```

## SELECT INTO
### Ejemplo básico (una sola columna)
```sql
DECLARE
  salario NUMERIC;
BEGIN
  SELECT sueldo INTO salario
  FROM empleados
  WHERE id = 5;
END;
```

Si no se encuentra ninguna fila, la variable toma el valor NULL.  
Si se encuentran varias, se toma solo la primera.

### SELECT INTO con múltiples columnas
```sql
DECLARE
  nombre TEXT;
  edad INT;
BEGIN
  SELECT nombre, edad INTO nombre, edad
  FROM empleados
  WHERE id = 1;
END;
```

```sql
DECLARE
  emp RECORD;
BEGIN
  SELECT id, nombre, edad INTO emp
  FROM empleados
  WHERE id = 1;

  RAISE NOTICE 'Nombre: %, Edad: %', emp.nombre, emp.edad;
END;
```

### SELECT INTO STRICT
| Situación | Excepción lanzada |
| --- | --- |
| Ninguna fila encontrada | NO_DATA_FOUND |
| Más de una fila encontrada | TOO_MANY_ROWS |

```sql
DO $$
DECLARE
  emp RECORD;
BEGIN
  BEGIN
    SELECT id, nombre, salario INTO STRICT emp
    FROM empleados
    WHERE activo = TRUE;

    RAISE NOTICE 'Empleado activo: % con salario %', emp.nombre, emp.salario;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE NOTICE 'No hay empleados activos';
    WHEN TOO_MANY_ROWS THEN
      RAISE NOTICE 'Hay más de un empleado activo';
  END;
END;
$$;
```

## Rollback implícito
Cuando se produce una excepción no capturada, PostgreSQL hace un rollback automático de todo el procedimiento o función.

```sql
IF saldo < 0 THEN
  RAISE EXCEPTION 'Saldo negativo: %', saldo;
END IF;
```

Si no se captura, revierte todos los cambios realizados dentro de la función.
