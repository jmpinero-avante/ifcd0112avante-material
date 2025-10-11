---
title: "03 – Inserción, uso y eliminación de datos"
---

# 3 – Inserción, uso y eliminación de datos
## INSERT: Añadir datos a una tabla
### Forma básica con VALUES
```sql
INSERT INTO empleados (nombre, edad, salario)
VALUES ('Ana', 30, 1500);
```

Se insertan los valores en el orden de los campos indicados.
Puedes omitir los campos si insertas en todos y en orden:

```sql
INSERT INTO empleados
VALUES (1, 'Luis', 40, 1800);
```

### Insertar múltiples filas con VALUES
También es posible insertar varias filas a la vez:

```sql
INSERT INTO empleados (nombre, edad, salario)
VALUES 
  ('Carlos', 25, 1400),
  ('Lucía', 28, 1550),
  ('Mario', 35, 1750);
```

Esto es más eficiente que hacer varios INSERT individuales.
Todas las filas deben tener el mismo número de valores.

### INSERT con SELECT
Permite copiar datos desde otra tabla o subconsulta:

```sql
INSERT INTO
   empleados_archivados (nombre, edad, salario)
SELECT
   nombre, edad, salario
FROM
   empleados
WHERE
   edad > 60;
```

Los campos del SELECT deben coincidir en número y tipo con los del INSERT.

## UPDATE: Modificar registros existentes
```sql
UPDATE
   empleados
SET
   salario = salario * 1.05
WHERE
   departamento = 'Ventas';
```

Aplica una modificación filtrada con WHERE.
Si hay triggers definidos para UPDATE, se ejecutan automáticamente.

## DELETE: Eliminar registros
```sql
DELETE FROM empleados
WHERE edad > 65;
```

Elimina filas que cumplan la condición WHERE.
También puede disparar triggers de tipo AFTER DELETE o BEFORE DELETE.

## Uso de WHERE en UPDATE y DELETE
Tanto UPDATE como DELETE pueden filtrar filas usando WHERE.

### Ejemplo en UPDATE (modificación)
```sql
UPDATE
   productos
SET
   stock = stock - 1
WHERE
   id_producto = 5;
```

### Ejemplo en DELETE (borrado)
```sql
DELETE FROM usuarios
WHERE fecha_registro < '2023-01-01';
```

## TRUNCATE: Vaciar una tabla completamente
```sql
TRUNCATE TABLE empleados;
```

## Diferencias entre DELETE y TRUNCATE
| Característica | DELETE | TRUNCATE |
| --- | --- | --- |
| Usa WHERE | ✅ Sí | ❌ No |
| Dispara triggers | ✅ Sí | ⚠️ No (en muchos casos) |
| Puede deshacerse | ✅ Sí (si hay transacción activa) | ✅ Sí (si hay transacción activa) |
| Velocidad | Más lento (registro por registro) | Muy rápido |
| Recuperación | Posible con transacciones | Posible si se usa en transacción |
