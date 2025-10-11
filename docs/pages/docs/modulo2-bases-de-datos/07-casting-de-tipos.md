---
title: "07 – Casting de tipos"
---

# 7 – Casting de tipos
En PostgreSQL puedes convertir un dato de un tipo a otro usando el operador de conversión (::) o la función CAST().

## Sintaxis general
```sql
valor::tipo
```

```sql
CAST(valor AS tipo)
```

## Ejemplos de conversiones comunes
### Texto a número
```sql
SELECT '123'::INTEGER;
```

### Número a texto
```sql
SELECT 99::TEXT;
```

### Texto a fecha
```sql
SELECT '2025-09-27'::DATE;
```

### TIMESTAMP a DATE (descarta la hora)
```sql
SELECT CURRENT_TIMESTAMP::DATE;
```

### Alternativa con CAST
```sql
SELECT CAST('123' AS INTEGER);
```
