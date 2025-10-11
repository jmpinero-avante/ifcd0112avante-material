---
title: "06 – Triggers"
---

# 6 – Triggers
Un trigger (disparador) es un mecanismo automático que se ejecuta como respuesta a un evento sobre una tabla o vista.
Puede activarse con operaciones INSERT, UPDATE, DELETE o incluso DDL (como CREATE TABLE o ALTER).
En resumen:

Un trigger actúa como un "reaccionador" dentro de la base de datos:
“Cuando ocurra X en la tabla Y, ejecuta automáticamente el código Z.”

## ¿Para qué sirven los triggers?
- Mantener integridad lógica entre tablas (por ejemplo, borrar líneas de pedido al borrar un pedido).
- Auditar cambios: registrar en una tabla quién modificó un registro y cuándo.
- Validar o corregir datos antes de guardarlos (convertir texto, comprobar rangos, etc.).
- Sincronizar información: actualizar totales o estadísticas al cambiar datos base.

## Tipos de triggers en PostgreSQL
### Según el momento
| Tipo | Se ejecuta... | Uso típico |
| --- | --- | --- |
| BEFORE | Antes de la operación | Validar, modificar o impedir cambios |
| AFTER | Después de la operación | Registrar cambios, propagar datos |
| INSTEAD OF | En lugar de la operación (solo vistas) | Personalizar vistas actualizables |

### Según el alcance
| Tipo | Actúa sobre... | Ejemplo |
| --- | --- | --- |
| FOR EACH ROW | Cada fila afectada | Registrar log por cada registro insertado |
| FOR EACH STATEMENT | Una vez por sentencia | Calcular totales tras un UPDATE masivo |

## Estructura general de un trigger
Un trigger tiene dos componentes:

1- Una función PL/pgSQL que define la lógica.

2- Una declaración CREATE TRIGGER que la asocia a una tabla y evento.

### Paso 1. Crear tabla principal
```sql
CREATE TABLE empleados (
    id SERIAL PRIMARY KEY,
    nombre TEXT,
    salario NUMERIC(10,2)
);
```

### Paso 2. Crear tabla de auditoría
```sql
CREATE TABLE log_empleados (
    id SERIAL PRIMARY KEY,
    empleado_id INT,
    accion TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario TEXT
);
```

### Paso 3. Crear la función trigger
```sql
CREATE OR REPLACE FUNCTION registrar_cambio_empleado()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO log_empleados (empleado_id, accion, usuario)
        VALUES (NEW.id, 'INSERT', current_user);

    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO log_empleados (empleado_id, accion, usuario)
        VALUES (NEW.id, 'UPDATE', current_user);

    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO log_empleados (empleado_id, accion, usuario)
        VALUES (OLD.id, 'DELETE', current_user);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

| Elemento | Explicación |
| --- | --- |
| TG_OP | Operación (INSERT, UPDATE, DELETE) |
| NEW | Fila nueva (INSERT/UPDATE) |
| OLD | Fila anterior (UPDATE/DELETE) |
| current_user | Usuario que ejecutó la acción |

### Paso 4. Asociar el trigger a la tabla
```sql
CREATE TRIGGER tr_log_empleados
AFTER INSERT OR UPDATE OR DELETE
ON empleados
FOR EACH ROW
EXECUTE FUNCTION registrar_cambio_empleado();
```

| Elemento | Significado |
| --- | --- |
| AFTER | Se ejecuta después de la acción. |
| FOR EACH ROW | Actúa por cada fila. |
| EXECUTE FUNCTION | Llama a la función PL/pgSQL definida. |

### Ejemplo de funcionamiento
```sql
INSERT INTO empleados (nombre, salario)
VALUES ('Lucía', 2500.00);
```

PostgreSQL ejecuta automáticamente:

```sql
INSERT INTO log_empleados (empleado_id, accion, usuario)
VALUES (1, 'INSERT', 'juanma');
```

El log se rellena sin intervención manual del programador.

## Ejemplo de trigger BEFORE (validación de datos)
```sql
CREATE OR REPLACE FUNCTION validar_salario()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.salario < 1000 THEN
        RAISE EXCEPTION 'El salario mínimo debe ser 1000 euros';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_validar_salario
BEFORE INSERT OR UPDATE ON empleados
FOR EACH ROW
EXECUTE FUNCTION validar_salario();
```

Si se intenta insertar un salario menor a 1000, la operación se cancela.
El trigger BEFORE actúa antes de escribir los datos.

## Ejemplo con INSTEAD OF (vistas actualizables)
```sql
CREATE VIEW vista_empleados AS
SELECT id, nombre, salario FROM empleados WHERE salario > 2000;
```

```sql
CREATE OR REPLACE FUNCTION insertar_en_vista()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO empleados (nombre, salario) VALUES (NEW.nombre, NEW.salario);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_insertar_vista
INSTEAD OF INSERT ON vista_empleados
FOR EACH ROW
EXECUTE FUNCTION insertar_en_vista();
```

Permite ejecutar INSERT sobre una vista que, internamente, inserta en la tabla real.

## Variables útiles dentro de un trigger
| Variable | Descripción |
| --- | --- |
| TG_OP | Tipo de operación (INSERT, UPDATE, DELETE) |
| TG_TABLE_NAME | Nombre de la tabla afectada |
| TG_WHEN | Momento del trigger (BEFORE, AFTER, INSTEAD OF) |
| NEW | Fila nueva (solo INSERT y UPDATE) |
| OLD | Fila antigua (solo UPDATE y DELETE) |
| TG_ARGV[] | Argumentos pasados al trigger |

## Buenas prácticas con triggers
- Usar solo cuando sea necesario, ya que pueden complicar la depuración.
- Documentar claramente cada trigger y su función.
- Evitar operaciones pesadas dentro del trigger.
- Usar AFTER para auditorías y BEFORE para validaciones.
- Probar y depurar con cuidado, usando RAISE NOTICE para depuración.

```sql
RAISE NOTICE 'Se ha insertado el empleado %', NEW.nombre;
```

## Cómo ver y eliminar triggers
Listar triggers de una tabla:

```sql
\d nombre_tabla
```

```sql
SELECT tgname, tgtype::regtype, tgfoid::regproc
FROM pg_trigger
WHERE tgrelid = 'empleados'::regclass;
```

Eliminar un trigger:

```sql
DROP TRIGGER tr_log_empleados ON empleados;
```

Eliminar la función asociada:

```sql
DROP FUNCTION registrar_cambio_empleado();
```
