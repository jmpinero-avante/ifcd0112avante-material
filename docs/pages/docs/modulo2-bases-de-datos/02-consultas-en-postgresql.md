---
title: "02 – Consultas en PostgreSQL"
---

# 2 – Consultas en PostgreSQL
## Consultas básicas: SELECT
Devuelve todas las filas y columnas:

```sql
SELECT * FROM empleados;
```

Puedes seleccionar columnas concretas:

```sql
SELECT nombre, edad FROM empleados;
```

## Alias de columnas
Los alias permiten renombrar columnas en la salida:

```sql
SELECT
    nombre AS "Nombre completo",
    salario * 1.1 AS "Salario con subida"
FROM empleados;
```

Se puede usar AS, aunque es opcional.
Si el alias tiene espacios o mayúsculas, hay que usar comillas dobles.

## Alias de tablas
Útiles en consultas con múltiples tablas o JOINs:

```sql
SELECT
    e.nombre, 
    d.nombre_departamento
FROM
    empleados AS e
JOIN departamentos AS d
    ON e.id_departamento = d.id_departamento;
```

## Cláusula WHERE
Filtra los resultados según condiciones.

```sql
SELECT nombre FROM empleados WHERE edad > 30;
```

## Operadores habituales
| Operador | Uso |
| --- | --- |
| = | Igual a |
| <> o != | Distinto de |
| <, >, <=, >= | Comparaciones numéricas |
| BETWEEN | Rango de valores |
| IN | Coincidencia en una lista |
| LIKE | Patrón textual (%, _) |
| IS NULL | Es nulo |
| IS NOT NULL | No es nulo |

## Ordenación y límites: ORDER BY y LIMIT
Ordena el resultado:

```sql
SELECT * FROM empleados ORDER BY edad;
```

Por defecto es ascendente (ASC).  
Descendente:

```sql
ORDER BY edad DESC
```

También puedes ordenar por el número de columna:

```sql
SELECT
    nombre,
    salario
FROM
    empleados
ORDER BY
    2 DESC;
```

Restringe el número de filas devueltas:

```sql
SELECT
    *
FROM
    empleados
ORDER BY
    salario DESC
LIMIT 3;
```

Empleado con salario más alto:

```sql
SELECT
    nombre,
    salario
FROM
    empleados
ORDER BY
    salario DESC
LIMIT 1;
```

Empleado más joven:

```sql
SELECT
    nombre,
    edad
FROM
    empleados
ORDER BY
    edad ASC
LIMIT 1;
```

## Tipos de JOIN
### Tablas de ejemplo
| id_producto | nombre_producto |
| --- | --- |
| 1 | Camiseta |
| 2 | Pantalón |
| 3 | Zapatos |

| id_color | nombre_color |
| --- | --- |
| 1 | Rojo |
| 2 | Azul |

### INNER JOIN
Solo coincidencias.

```sql
SELECT
    *
FROM
    productos
    INNER JOIN colores
        ON productos.id_producto = colores.id_color;
```

### LEFT JOIN
Muestra todos los productos, aunque no tengan color asociado.

```sql
SELECT
    *
FROM
    productos
    LEFT JOIN colores
        ON productos.id_producto = colores.id_color;
```

Filtrar productos sin color asignado:

```sql
SELECT
    *
FROM
    productos
    LEFT JOIN colores
        ON productos.id_producto = colores.id_color
WHERE
    colores.id_color IS NULL;
```

### RIGHT JOIN
Muestra todos los colores, aunque no tengan producto asociado.

```sql
SELECT
    *
FROM
    productos
    RIGHT JOIN colores
        ON productos.id_producto = colores.id_color;
```

### FULL JOIN
Devuelve todas las filas de ambas tablas, coincidan o no.

```sql
SELECT
    *
FROM
    productos
    FULL JOIN colores
        ON productos.id_producto = colores.id_color;
```

### CROSS JOIN
Producto cartesiano: todas las combinaciones posibles (3 × 2 = 6 filas).

```sql
SELECT
    *
FROM
    productos
    CROSS JOIN colores;
```

| JOIN | ¿Qué hace? |
| --- | --- |
| FULL JOIN | Une filas coincidentes y muestra también las no relacionadas |
| CROSS JOIN | Todas las combinaciones posibles (producto cartesiano) |

## Funciones de agregación
| id | nombre | edad | salario | departamento |
| --- | --- | --- | --- | --- |
| 1 | Ana | 25 | 1200 | Ventas |
| 2 | Luis | 45 | 1800 | Marketing |
| 3 | Marta | 30 | 1500 | Ventas |
| 4 | Pedro | 50 | 2200 | Marketing |
| 5 | Clara | 29 | 1600 | Ventas |

```sql
SELECT COUNT(*) FROM empleados;             -- 5
SELECT SUM(salario) FROM empleados;         -- 8300
SELECT AVG(salario) FROM empleados;         -- 1660
SELECT MIN(edad), MAX(edad) FROM empleados; -- 25 y 50
```

## Agrupación: GROUP BY
```sql
SELECT
    departamento,
    COUNT(*) 
FROM
    empleados 
GROUP BY
    departamento;
```

Regla: todos los campos que están en GROUP BY deben aparecer en el SELECT, salvo funciones de agregación.

```sql
SELECT
    departamento,
    AVG(salario) 
FROM
    empleados 
GROUP BY
    departamento;
```

| departamento | AVG(salario) |
| --- | --- |
| Ventas | 1433.33 |
| Marketing | 2000.00 |

## HAVING vs WHERE
| Cláusula | Filtra… | Cuándo se aplica |
| --- | --- | --- |
| WHERE | Filas individuales | Antes del GROUP BY |
| HAVING | Grupos agregados | Después del GROUP BY |

```sql
SELECT
    nombre,
    salario
FROM
    empleados
WHERE
    salario > 1500;
```

Filtra empleados individuales cuyo salario es mayor a 1500.

```sql
SELECT
    departamento,
    COUNT(*) AS empleados
FROM
    empleados
GROUP BY
    departamento
HAVING COUNT(*) > 2;
```

Filtra grupos (departamentos) que tienen más de 2 empleados.

## Subconsultas
### En WHERE (subconsulta NO correlacionada)
```sql
SELECT
    nombre
FROM
    empleados
WHERE
    salario > (
        SELECT AVG(salario) FROM empleados
);
```

Compara con el promedio general.

### En SELECT
```sql
SELECT
    nombre, 
    (
        SELECT
            AVG(edad)
        FROM
            empleados
    ) AS edad_media
FROM
    empleados;
```

Agrega la media a cada fila.

### En FROM
```sql
SELECT
    departamento,
    total
FROM
  (
      SELECT
          departamento,
          COUNT(*) AS total
      FROM
          empleados
      GROUP BY
          departamento
  ) AS resumen
WHERE
    total > 2;
```

### Subconsulta correlacionada vs no correlacionada
No correlacionada:

```sql
SELECT
    nombre
FROM
    empleados
WHERE
    salario > (SELECT AVG(salario) FROM empleados);
```

No depende de la fila externa.

Correlacionada:

```sql
SELECT
    e.nombre
FROM
    empleados e
WHERE
    salario > (
        SELECT
            AVG(salario)
        FROM
            empleados
        WHERE
            departamento = e.departamento
);
```

Compara con el promedio del mismo departamento.
