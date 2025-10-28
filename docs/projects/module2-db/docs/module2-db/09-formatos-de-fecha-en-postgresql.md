---
title: "09 – Formatos de fecha en PostgreSQL"
---

# 9 – Formatos de fecha en PostgreSQL
## Formato natural (locale española)
Cuando el sistema usa la configuración regional española, el formato habitual de fecha es:
'27/09/2025 15:30:00' (día/mes/año + hora).
Este formato es legible, pero no estándar y puede causar errores al intercambiar datos entre sistemas.

Es recomendable usar el formato ISO 8601 (internacional) para garantizar interoperabilidad y evitar ambigüedades.
También se acepta la versión simplificada YYYY-MM-DD.

| Tipo | Ejemplo |
| --- | --- |
| Solo fecha | 2023-09-27 |
| Fecha y hora | 2023-09-27 15:30:00 |
| Fecha, hora y zona | 2023-09-27 15:30:00+02 |

## Formato ISO 8601 (internacional)
ISO 8601 es un estándar para representar fechas y horas con formato claro y compatible.
Estructura general:
AAAA-MM-DDTHH:MM:SS.sss+ZZ:ZZ

| Elemento | Ejemplo / Significado |
| --- | --- |
| Solo fecha | 2023-09-27 |
| Fecha y hora | 2023-09-27T15:30:00 |
| Fecha y hora con milisegundos | 2023-09-27T15:30:00.000 |
| Fecha y hora con zona horaria | 2023-09-27T15:30:00+02:00 |
| Separador entre fecha y hora | T |
| Zona horaria | +02:00 / +02 |

PostgreSQL acepta este formato directamente en consultas e inserciones.

```sql
SELECT '2025-09-27T18:45:00.000+02:00'::TIMESTAMPTZ;
```

## Fechas en formato ISO 8601 sin separadores
PostgreSQL admite también la variante compacta del formato ISO, sin guiones ni dos puntos:

```plain
YYYYMMDDTHHMMSS
```

```sql
SELECT '20250927T183000'::TIMESTAMP;
-- Resultado: 2025-09-27 18:30:00
```

```sql
SELECT '20250927T183000+0200'::TIMESTAMPTZ;
-- Resultado: 2025-09-27 18:30:00+02
```

## Comparación de formatos
| Formato | Cumple ISO 8601 | Recomendado |
| --- | --- | --- |
| 2025-09-27T18:30:00 | ✅ ISO extendido | ✅ Sí |
| 20250927T183000 | ✅ ISO básico | ⚠️ Aceptable, menos legible |
| 2025-09-27 18:30:00 | ❌ No ISO estricto | ⚠️ Legible, pero informal |

### Cuándo usar formato compacto
- Cuando se cargan datos masivos.
- Cuando se conoce el formato exacto de destino.
- Cuando se desea optimizar espacio o parsing.

Usa el formato extendido (YYYY-MM-DDTHH:MM:SS) en informes, APIs o documentación formal.

## UTC: Tiempo Universal Coordinado
UTC es el estándar global de tiempo sin ajustes de horario de verano, sustituyendo al antiguo GMT.
Un TIMESTAMP WITH TIME ZONE se guarda internamente en UTC.
PostgreSQL convierte automáticamente entre UTC y la zona local al mostrarlo.

```sql
-- Mostrado en hora local:
SELECT NOW();  -- 2025-09-27 17:15:00+02

-- Mostrado como UTC:
SELECT NOW() AT TIME ZONE 'UTC';  -- 2025-09-27 15:15:00+00
```

De esta forma, las comparaciones y almacenamiento de fechas son consistentes globalmente.
Siempre que trabajes con múltiples zonas horarias, usa TIMESTAMPTZ.

## Tipo de dato TIMESTAMPTZ
| Tipo de dato | Significado |
| --- | --- |
| TIMESTAMP | Fecha y hora sin zona horaria |
| TIMESTAMPTZ | Fecha y hora con zona horaria |

### Cómo funciona realmente TIMESTAMPTZ
PostgreSQL almacena internamente el valor en UTC y lo muestra ajustado a la zona horaria del cliente.

```sql
CREATE TABLE eventos (
  id SERIAL PRIMARY KEY,
  nombre TEXT,
  fecha TIMESTAMPTZ
);

INSERT INTO eventos (nombre, fecha)
VALUES ('Concierto', '2025-09-27 20:00:00');
```

Internamente se guarda como: 2025-09-27 18:00:00+00 (UTC)  
Pero al consultar desde Europe/Madrid (UTC+2) se muestra: 2025-09-27 20:00:00+02

### Ventajas de TIMESTAMPTZ
- Ideal para aplicaciones internacionales.
- Permite almacenar un valor estandarizado y mostrarlo localmente.
- Facilita comparaciones precisas entre zonas horarias.

### Comparación práctica
```sql
-- Timestamp sin zona horaria
SELECT TIMESTAMP '2025-09-27 20:00:00';

-- Timestamp con zona horaria
SELECT TIMESTAMPTZ '2025-09-27 20:00:00+02';
```

El primero es una hora absoluta sin contexto.  
El segundo permite conversión automática entre zonas.

## Conclusión
Siempre que trabajes con usuarios en distintas regiones o necesites conservar la hora real de un evento global, usa TIMESTAMPTZ.
