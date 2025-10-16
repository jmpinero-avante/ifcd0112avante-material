# 🧮 Iteración 3 — PreparedStatement e inserciones

Usamos **PreparedStatement** para prevenir inyecciones SQL y mejorar rendimiento.

```java
PreparedStatement ps = conn.prepareStatement(
    "INSERT INTO empleados(nombre, salario) VALUES (?, ?)"
);
ps.setString(1, "Ana");
ps.setDouble(2, 1800);
ps.executeUpdate();
```

Ventajas:
- Seguridad contra SQL injection.
- Reutilización de consultas.
- Mejor rendimiento en ejecuciones repetidas.
