
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

---

## Representación de cardinalidades

Las cardinalidades indican cuántas veces una entidad puede intervenir en una relación.
Hay dos formas de representarlas gráficamente:

### Forma 1 – Dos grupos de números (más precisa)

<!-- svg-diagram start -->
<div class="diagram-block" align="center">
<svg width="750" height="300" xmlns="http://www.w3.org/2000/svg" style="background-color:#fff">
  <style>
    /* === Estilos generales === */
    text {
      font-family: "DejaVu Sans", Arial, sans-serif;
      fill: #222;
    }
    rect, polygon, line {
      stroke: #333;
      stroke-width: 1.3;
      fill: #fff;
    }
    .entity rect {
      fill: #f8fbff;
      stroke: #375a7f;
    }
    .relationship polygon {
      fill: #eef2f5;
      stroke: #222;
    }
    .card {
      font-size: 12px;
      fill: #000;
    }
  </style>

  <!-- === Definición de marcador de flecha === -->
  <defs>
    <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5"
            markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#333" />
    </marker>
  </defs>

  <!-- === Entidad izquierda === -->
  <g class="entity">
    <rect x="60" y="120" width="130" height="60" rx="4" ry="4"/>
    <text x="125" y="155" text-anchor="middle">Alumno</text>
  </g>

  <!-- === Entidad derecha (acercada) === -->
  <g class="entity">
    <rect x="530" y="120" width="130" height="60" rx="4" ry="4"/>
    <text x="595" y="155" text-anchor="middle">Curso</text>
  </g>

  <!-- === Relación (rombo) === -->
  <g class="relationship">
    <polygon points="320,100 410,150 320,200 230,150"/>
    <text x="320" y="157" text-anchor="middle">se matricula</text>
  </g>

  <!-- === Conexiones con flechas === -->
  <line x1="190" y1="150" x2="230" y2="150" stroke="#333" marker-end="url(#arrow)"/>
  <line x1="530" y1="150" x2="410" y2="150" stroke="#333" marker-end="url(#arrow)"/>

  <!-- === Cardinalidades === -->
  <text class="card" x="210" y="135" text-anchor="middle">0..N</text>
  <text class="card" x="465" y="135" text-anchor="middle">1..M</text>
</svg>
</div>
<!-- svg-diagram end -->

- Cada alumno puede estar matriculado de 1 o mas cursos.
- Cada curso puede tener matriculados 0 o más alumnos.

Esta forma es muy explícita, ya que indica claramente el mínimo y máximo de participación de cada entidad en la relación.

### Forma 2 – Un solo grupo de números en el rombo

<!-- svg-diagram start -->
<div class="diagram-block" align="center">
<svg width="700" height="260" xmlns="http://www.w3.org/2000/svg" style="background-color:#fff">
  <style>
    /* === Estilos generales === */
    text {
      font-family: "DejaVu Sans", Arial, sans-serif;
      fill: #222;
    }
    rect, polygon, line {
      stroke: #333;
      stroke-width: 1.3;
      fill: #fff;
    }
    .entity rect {
      fill: #f8fbff;
      stroke: #375a7f;
    }
    .relationship polygon {
      fill: #eef2f5;
      stroke: #222;
    }
    .card {
      font-size: 13px;
      fill: #000;
    }
  </style>

  <!-- === Flechas === -->
  <defs>
    <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5"
            markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#333" />
    </marker>
  </defs>

  <!-- === Entidad izquierda: Alumno === -->
  <g class="entity">
    <rect x="60" y="105" width="140" height="70" rx="4" ry="4"/>
    <text x="130" y="145" text-anchor="middle">Alumno</text>
  </g>

  <!-- === Entidad derecha: Curso === -->
  <g class="entity">
    <rect x="430" y="105" width="140" height="70" rx="4" ry="4"/>
    <text x="500" y="145" text-anchor="middle">Curso</text>
  </g>

  <!-- === Relación (rombo): se matricula (más separado del alumno) === -->
  <g class="relationship">
    <polygon points="290,90 340,140 290,190 240,140"/>
    <text x="290" y="146" text-anchor="middle">se matricula</text>
  </g>

  <!-- === Conexiones con flecha hacia el rombo === -->
  <line x1="200" y1="140" x2="240" y2="140" stroke="#333" marker-end="url(#arrow)"/>
  <line x1="430" y1="140" x2="340" y2="140" stroke="#333" marker-end="url(#arrow)"/>

  <!-- === Cardinalidad sobre el rombo === -->
  <text class="card" x="290" y="72" text-anchor="middle">N:M</text>
</svg>
</div>
<!-- svg-diagram end -->

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

---

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

---

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


---

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

---

## El tipo SERIAL

```sql
INTEGER NOT NULL DEFAULT nextval('nombre_secuencia')
```


Al usar SERIAL, PostgreSQL crea una secuencia automática asociada.

---

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

---

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

---

## Restricciones adicionales

| Restricción | ¿Dónde se aplica? | Significado |
| --- | --- | --- |
| NOT NULL | Columna | El valor no puede ser nulo |
| UNIQUE | Columna o grupo | No puede haber valores repetidos |
| CHECK | Columna o tabla | Condición que debe cumplirse (CHECK (edad > 0)) |

---

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

---

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

