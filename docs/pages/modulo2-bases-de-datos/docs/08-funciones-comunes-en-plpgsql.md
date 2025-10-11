---
title: "08 – Funciones comunes en PL/pgSQL"
---

# 8 – Funciones comunes en PL/pgSQL
## Funciones de fecha
```sql
SELECT CURRENT_DATE;               -- Solo la fecha
SELECT CURRENT_TIME;               -- Solo la hora
SELECT NOW();                      -- Fecha y hora con zona horaria
SELECT DATE_TRUNC('month', NOW()); -- Recorta al mes
SELECT EXTRACT(DAY FROM NOW());    -- Día actual
```

## Funciones de texto
```sql
SELECT UPPER('hola');              -- HOLA
SELECT LOWER('TEXTO');             -- texto
SELECT LENGTH('hola mundo');       -- 11
SELECT TRIM('  hola  ');           -- 'hola'
SELECT CONCAT('Hola', ' mundo');   -- 'Hola mundo'
```

## Funciones matemáticas
```sql
SELECT ABS(-8);             -- 8
SELECT GREATEST(10, 3, 5);  -- 10
SELECT LEAST(10, 3, 5);     -- 3
```

## Funciones condicionales
```sql
SELECT COALESCE(NULL, 'valor'); -- devuelve 'valor'
```
