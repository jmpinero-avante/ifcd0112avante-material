---
title: "01 – Modelado y definición de tablas en PostgreSQL"
---

# 1 – Modelado y definición de tablas en PostgreSQL
## Elementos gráficos del Diagrama Entidad-Relación (ER)
| Elemento | Forma en el diagrama | Representa |
| --- | --- | --- |
| Entidad | Rectángulo | Una tabla |
| Relación | Rombo | Asociación entre dos o más entidades |
| Atributo | Óvalo | Propiedad de una entidad o una relación |
| Flecha | Flecha → | Une entidad (rectángulo) a relación (rombo) |

Las flechas siempre van desde una entidad hacia una relación, nunca al revés.
Un atributo puede estar conectado a:

- Una entidad: se convertirá en una columna en la tabla correspondiente.
- Una relación: si la relación tiene atributos, se transforma en una tabla propia.

## Representación de cardinalidades
Las cardinalidades indican cuántas veces una entidad puede intervenir en una relación.
Hay dos formas de representarlas gráficamente:

### Forma 1 – Dos grupos de números (más precisa)
Alumno      Matrícula              Curso
(1..1)  ← (0..N) → (1..N) → (1..1)

- Cada alumno puede tener 0 o más matrículas.
- Cada matrícula pertenece a un único alumno.
- Cada matrícula es sobre un único curso.
- Un curso puede tener muchas matrículas.

Esta forma es muy explícita, ya que indica claramente el mínimo y máximo de participación de cada entidad en la relación.

### Forma 2 – Un solo grupo de números en el rombo
```plain
Alumno <──────(1:N)──────> Curso
```

- Se entiende que un alumno puede estar inscrito en varios cursos.
- Un curso tiene varios alumnos inscritos.
- No se distingue entre 0..N y 1..N, ni entre 1..1 y 0..1. Es más compacta pero menos precisa.

| Aspecto | Forma 1: dos lados | Forma 2: en el rombo |
| --- | --- | --- |
| Precisión | Muy alta | Media (general) |
| Muestra mínimos | Sí (0..N, 1..1, etc.) | No |
| Claridad visual | Detallada | Más simple visualmente |
| Ambigüedad | Ninguna | Puede generar confusión |
| Uso común | Formal, académico | Bocetos, esquemas rápidos |

## Traducción del modelo ER al modelo relacional (SQL)
| Elemento ER | Traducción SQL |
| --- | --- |
| Entidad | Tabla con CREATE TABLE |
| Atributo | Columna |
| Relación 1:N | Clave foránea en el lado N |
| Relación N:M | Nueva tabla intermedia con 2 claves foráneas |
| Relación con atributos propios | Tabla adicional con claves foráneas y columnas extra |
| Atributo clave primaria | PRIMARY KEY |
| Atributo clave foránea | FOREIGN KEY |
| Atributo obligatorio | NOT NULL |
| Atributo único | UNIQUE |
| Atributo con condición | CHECK (condición) |

## Sintaxis general de CREATE TABLE
```sql
CREATE TABLE IF NOT EXISTS nombre_tabla (
    columna1 tipo [restricciones],
    columna2 tipo [restricciones],
    ...
    CONSTRAINT nombre_restriccion CHECK (...),
    PRIMARY KEY (...),
    FOREIGN KEY (...) REFERENCES otra_tabla(columna)
        ON DELETE ... ON UPDATE ...
);
```

## Tipos de datos más usados en PostgreSQL
| Tipo | Descripción |
| --- | --- |
| INTEGER | Números enteros |
| TEXT | Texto de longitud variable |
| BOOLEAN | TRUE o FALSE |
| DATE | Fecha (YYYY-MM-DD) |
| TIMESTAMP | Fecha y hora combinadas |
| NUMERIC | Números decimales de precisión fija |
| SERIAL | Entero autoincremental |

## El tipo SERIAL
```sql
INTEGER NOT NULL DEFAULT nextval('nombre_secuencia')
```

Al usar SERIAL, PostgreSQL crea una secuencia automática asociada.

## Claves primarias
```sql
id_empleado SERIAL PRIMARY KEY
```

Garantiza unicidad y no permite valores nulos.
No necesita NOT NULL ni UNIQUE porque ya están implícitos.

```sql
PRIMARY KEY (id_alumno, id_curso)
```

Se utiliza cuando la combinación de varios campos debe ser única.
Muy común en relaciones N:M.

## Claves foráneas
```sql
id_departamento INTEGER REFERENCES departamentos(id_departamento)
```

```sql
FOREIGN KEY (id_departamento)
    REFERENCES departamentos(id_departamento)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
```

| Acción | Efecto |
| --- | --- |
| CASCADE | Borra/modifica también las filas relacionadas |
| SET NULL | Asigna NULL al campo en la tabla dependiente |
| SET DEFAULT | Asigna el valor por defecto |
| RESTRICT | Impide borrar/modificar si hay filas relacionadas |
| NO ACTION | Igual que RESTRICT pero verificado al final de la transacción |

## Restricciones adicionales
| Restricción | ¿Dónde se aplica? | Significado |
| --- | --- | --- |
| NOT NULL | Columna | El valor no puede ser nulo |
| UNIQUE | Columna o grupo | No puede haber valores repetidos |
| CHECK | Columna o tabla | Condición que debe cumplirse (CHECK (edad > 0)) |

## Identificadores con comillas dobles
```sql
CREATE TABLE "Empleados Activos" (
    "ID Usuario" SERIAL,
    "Nombre Completo" TEXT
);
```

```sql
SELECT "Nombre Completo" FROM "Empleados Activos";
```

Una vez creado así, debes usar comillas dobles siempre para referenciar ese campo o tabla.

## Ejemplo final con todos los elementos aplicados
```sql
CREATE TABLE IF NOT EXISTS empleados (
    id_empleado SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    dni TEXT UNIQUE,
    edad INTEGER CHECK (edad > 17),
    id_departamento INTEGER REFERENCES departamentos(id_departamento)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    fecha_ingreso DATE DEFAULT CURRENT_DATE
);
```

- id_empleado es autoincremental y clave primaria.
- dni debe ser único.
- edad debe ser mayor de 17.
- id_departamento es una clave foránea con ON DELETE SET NULL.
- fecha_ingreso tiene un valor por defecto (la fecha actual).
