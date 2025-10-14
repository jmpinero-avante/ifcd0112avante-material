---
title: "04 – Índices y análisis de consultas"
---

# 4 – Índices y análisis de consultas
## Concepto de índice
Un índice es una estructura auxiliar que acelera la búsqueda y el acceso a los registros de una tabla.
Su función es reducir el número de filas que el motor de la base de datos necesita leer para localizar los datos que cumplen una condición.
Sin índice, PostgreSQL tiene que realizar un sequential scan (escaneo secuencial), revisando una por una todas las filas de la tabla.
Con un índice, puede saltar directamente a la ubicación donde se encuentran los datos relevantes, de forma análoga a cómo usamos el índice de un libro para encontrar rápidamente una palabra o tema.

## Estructura interna: el índice B-tree
El B-tree (Balanced Tree) es el tipo de índice más común en PostgreSQL (y en la mayoría de los SGBD).

### Cómo funciona un B-tree
Imagina un árbol ordenado:
Los nodos intermedios contienen claves que dividen el espacio de búsqueda.
Los nodos hoja contienen punteros a las filas reales en la tabla.
El árbol se mantiene equilibrado: todas las hojas están a la misma profundidad.
Esto garantiza que el tiempo de búsqueda, inserción y borrado sea logarítmico: O(log n).

```plain
-
            [K=50]
           /     \
	[10,20,30] [60,70,80]
-
```

Cuando buscamos el valor 70, el árbol compara:
70 > 50 → va a la rama derecha.
Busca en [60,70,80] → encuentra la posición exacta → devuelve la fila correspondiente.
El acceso es mucho más rápido que recorrer todos los registros secuencialmente.

## Índices implícitos
PostgreSQL crea automáticamente algunos índices sin que el usuario los pida:

Clave primaria (PRIMARY KEY):

```sql
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nombre TEXT
);
```

Crea automáticamente un índice B-tree sobre id.

Restricciones UNIQUE:

```sql
CREATE TABLE productos (
    codigo TEXT UNIQUE
);
```

Crea un índice implícito que garantiza la unicidad de codigo.
Estos índices automáticos son esenciales para mantener la integridad referencial y acelerar las búsquedas por claves.

## Creación de índices manuales
Índice básico:

```sql
CREATE INDEX idx_usuarios_nombre ON usuarios (nombre);
```

Índice en orden descendente:

```sql
CREATE INDEX idx_pedidos_fecha_desc ON pedidos (fecha DESC);
```

Índice múltiple (o compuesto):

```sql
CREATE INDEX idx_clientes_apellido_ciudad ON clientes (apellido, ciudad);
```

Importante:
Los índices múltiples se aprovechan por su primera columna y las subsecuentes, en ese orden.

Por ejemplo:

```sql
WHERE apellido = 'García'
	-- ✅ usa el índice.

WHERE apellido = 'García' AND ciudad = 'Sevilla'
	-- ✅ usa el índice.

WHERE ciudad = 'Sevilla'
	-- ❌ no usa el índice
	-- (la primera columna apellido no se filtra).
```

Este comportamiento se conoce como regla del prefijo del índice.

Índice parcial:

```sql
CREATE INDEX idx_activos ON empleados (dni) WHERE activo = true;
```

Ideal para grandes tablas con muchas filas inactivas.

Índice único:

```sql
CREATE UNIQUE INDEX idx_email_unico ON usuarios (email);
```

Garantiza que no se repitan valores en esa columna.

## Buenas prácticas en el uso de índices
- Usar índices en columnas que aparecen con frecuencia en cláusulas WHERE, condiciones de JOIN, ORDER BY o GROUP BY.
- No indexar todo: cada índice ocupa espacio y ralentiza INSERT, UPDATE y DELETE.
- Evitar índices en columnas con baja selectividad (por ejemplo, un campo BOOLEAN con casi todos los valores iguales).
- Analizar el tamaño de los índices.

```sql
SELECT relname, pg_size_pretty(pg_total_relation_size(indexrelid))
FROM pg_stat_user_indexes
WHERE schemaname = 'public';
```

Actualizar estadísticas regularmente:

```sql
VACUUM ANALYZE;
```

Esto ayuda al optimizador a tomar decisiones más acertadas.

## Análisis de rendimiento con EXPLAIN y EXPLAIN ANALYZE
### EXPLAIN
Muestra el plan estimado de ejecución:

```sql
EXPLAIN SELECT * FROM usuarios WHERE nombre = 'Juan';
```

Salida:

```plain
Index Scan using idx_usuarios_nombre on usuarios  (cost=0.15..8.17 rows=1 width=48)
```

| Componente | Significado |
| --- | --- |
| Index Scan | Está usando el índice. |
| cost | Estimación del optimizador. |

### EXPLAIN ANALYZE
Ejecuta realmente la consulta y muestra los tiempos reales:

```sql
EXPLAIN ANALYZE SELECT * FROM usuarios WHERE nombre = 'Juan';
```

Ejemplo de salida:

```plain
Index Scan using idx_usuarios_nombre on usuarios  (cost=0.15..8.17 rows=1 width=48)
(actual time=0.030..0.035 rows=1 loops=1)
```

| Componente | Significado |
| --- | --- |
| cost | Coste estimado (cuanto menor, mejor). |
| actual time | Tiempo real de ejecución. |
| rows | Número de filas encontradas. |
| loops | Veces que se repitió el plan (en subconsultas o bucles). |

## Comparativa de rendimiento: sin índice y con índice
Supón que tenemos 1 millón de filas en usuarios:

```sql
SELECT * FROM usuarios WHERE nombre = 'Juan';
```

Sin índice:

```plain
Seq Scan on usuarios (cost=0.00..25000.00 rows=1 width=48)
(actual time=50.000..50.001 rows=1 loops=1)
```

Con índice:

```plain
Index Scan using idx_usuarios_nombre on usuarios (cost=0.15..8.17 rows=1 width=48)
(actual time=0.035..0.036 rows=1 loops=1)
```

El acceso pasa de 50 ms a 0.03 ms, un ahorro enorme.

## Mantenimiento de índices
Comprobar el uso de índices:

```sql
SELECT
   relname AS tabla,
   indexrelname AS indice,
   idx_scan AS veces_usado,
   idx_tup_read AS tuplas_leidas,
   idx_tup_fetch AS tuplas_devueltas
FROM
   pg_stat_user_indexes
WHERE
   schemaname = 'public';
```

Esta vista muestra cuántas veces se ha utilizado cada índice desde el último reinicio de estadísticas.

Reindexar un índice específico:

```sql
REINDEX INDEX idx_usuarios_nombre;
```

Reindexar una tabla completa:

```sql
REINDEX TABLE usuarios;
```

Ver todos los índices existentes:

```sql
\di
```

## Ejemplo completo
Supongamos una tabla de pedidos:

```sql
CREATE TABLE pedidos (
    id SERIAL PRIMARY KEY,
    cliente_id INT,
    fecha DATE,
    total DECIMAL
);
```

Creamos un índice para acelerar búsquedas por fecha:

```sql
CREATE INDEX idx_pedidos_fecha ON pedidos (fecha);
```

Consultamos:

```sql
EXPLAIN ANALYZE
SELECT * FROM pedidos
WHERE fecha BETWEEN '2025-01-01' AND '2025-03-31';
```

Si el rango abarca pocas filas: “Index Scan”.  
Si abarca la mayoría: “Seq Scan” (el optimizador decide que leer toda la tabla es más rápido).

## Resumen final
- Los índices mejoran la velocidad de lectura pero empeoran ligeramente la escritura.
- Los B-trees son los índices por defecto y más versátiles.
- Los índices compuestos funcionan en orden de las columnas, empezando por la primera.
- Usa EXPLAIN ANALYZE para comprobar si se usan correctamente.
- Revisa estadísticas periódicamente y evita sobreindexar.
