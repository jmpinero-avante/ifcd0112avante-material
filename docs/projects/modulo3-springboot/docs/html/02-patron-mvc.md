# 🧩 Patrón MVC

**Modelo-Vista-Controlador (MVC)** es un patrón de diseño que separa responsabilidades dentro de una aplicación.

| Capa | Rol | En nuestra app |
|------|-----|----------------|
| **Modelo** | Representa los datos y la lógica de negocio | Clase `Empleado` y su repositorio |
| **Vista** | Presenta los datos al usuario | Consola, JSON o HTML (Thymeleaf) |
| **Controlador** | Coordina la comunicación entre Vista y Modelo | Clases que manejan peticiones y devuelven resultados |

Esta separación facilita:
- La **mantenibilidad**
- La **escalabilidad**
- Las **pruebas unitarias**

Ejemplo visual:

```
Usuario → Vista → Controlador → Modelo → Base de Datos
```
