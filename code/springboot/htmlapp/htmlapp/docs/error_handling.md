# Arquitectura de manejo de errores en HtmlApp

Este documento explica cómo se gestionan los errores en la aplicación **HtmlApp**,  
siguiendo las buenas prácticas de Spring Boot, Thymeleaf y SLF4J.  

Incluye:  
- Excepciones personalizadas  
- Controlador global de errores  
- Estructura de vistas de error  
- Sistema de logging  
- Buenas prácticas y propósito didáctico  

---

## 1. Estructura general

```
src/
 ├─ main/java/com/example/htmlapp/
 │   ├─ controller/
 │   │   └─ ErrorControllerAdvice.java
 │   ├─ model/logic/exceptions/
 │   │   └─ OperationFailedException.java
 │   └─ ...
 │
 └─ main/resources/templates/error/
     ├─ 400.html
     ├─ 403.html
     ├─ 404.html
     ├─ 500.html
     ├─ generic-error.html
     ├─ operation-error.html
     └─ layout-error.html
```

---

## 2. Filosofía de diseño

El sistema de errores tiene tres objetivos principales:

1. **Seguridad:**  
   No exponer información sensible al usuario final.  
   (Los detalles técnicos se registran solo en los logs).

2. **Coherencia visual:**  
   Todas las páginas de error usan el mismo diseño mediante `layout-error.html`  
   y los estilos de `error.css`.

3. **Claridad didáctica:**  
   Cada excepción representa un tipo de error diferente y enseña buenas prácticas de desacoplamiento.

---

## 3. Componentes principales

### a) `OperationFailedException`
Excepción personalizada para errores de negocio.  
Permite incluir un `statusCode` numérico opcional (por ejemplo 409 o 422).

```java
throw new OperationFailedException("No se pudo enviar el correo.", 422);
```

Vista mostrada: `error/operation-error.html`  
Código mostrado en pantalla: `422`  
Código HTTP real: `500` (controlado por @ResponseStatus)

---

### b) `ErrorControllerAdvice`
Clase central del sistema de errores.  
Usa `@ControllerAdvice` y `@ExceptionHandler` para interceptar excepciones.

| Tipo de excepción | Código HTTP | Vista Thymeleaf |
|--------------------|--------------|------------------|
| `IllegalArgumentException` | 400 | `error/400.html` |
| `SecurityException` | 403 | `error/403.html` |
| `NoHandlerFoundException` | 404 | `error/404.html` |
| `OperationFailedException` | 500 (u otro) | `error/operation-error.html` |
| `Exception` (genérica) | 500 | `error/generic-error.html` |

Cada método del controlador:
- Asigna el código y título adecuados.
- Añade `errorMessage` y `errorCode` al modelo.
- Registra el error en los logs con diferentes niveles.

---

## 4. Logging y niveles de severidad

| Nivel | Cuándo se usa | Ejemplo |
|-------|----------------|----------|
| **WARN** | Errores esperables (403, 404) | `log.warn("Acceso denegado: {}", ex.getMessage());` |
| **ERROR** | Fallos graves del servidor o lógica | `log.error("Error interno del servidor: {}", ex.getMessage());` |
| **DEBUG** | Información extendida y stackTrace | `log.debug("StackTrace:", ex);` |

Esto permite mantener los logs limpios en producción,  
pero obtener trazas detalladas en desarrollo.

---

## 5. Vistas de error (HTML + Thymeleaf)

Todas las vistas de error heredan el mismo layout:

### `layout-error.html`
Define la estructura fija de las páginas:
- Título y cabecera.
- Bloque central con `error-code`, `error-message` y botones de acción.
- Pie de página común.

Las demás vistas (400, 403, 404, 500, etc.) solo definen su fragmento `msg` con el mensaje de error.

Ejemplo (404):
```html
<div th:replace="~{layout-error :: errorLayout('404', ~{::msg})}">
  <p th:fragment="msg">La página que estás buscando no existe.</p>
</div>
```

---

## 6. Flujo de un error

1. Un controlador o servicio lanza una excepción.
2. Spring delega el control a `ErrorControllerAdvice`.
3. Se selecciona el método correspondiente según el tipo de excepción.
4. Se registran los detalles en los logs.
5. Se renderiza una plantilla `error/*.html` con el mensaje amigable.

---

## 7. Ejemplo completo

```java
@PostMapping("/register")
public String register(@RequestParam String email) {
    if (emailService.exists(email)) {
        throw new OperationFailedException(
            "Ya existe un usuario con ese correo electrónico.", 409);
    }
    // Registro exitoso...
}
```

Resultado visual:
```
Error 409
Ya existe un usuario con ese correo electrónico.
[Volver al inicio] [Volver atrás]
```

En logs:
```
ERROR  [ErrorControllerAdvice] - Error en operación de negocio (código 409): Ya existe un usuario con ese correo electrónico.
DEBUG  StackTrace: com.example.htmlapp.model.logic.exceptions.OperationFailedException: ...
```

---

## 8. Buenas prácticas

- No exponer nunca excepciones de Java directamente al usuario.
- Centralizar el tratamiento de errores en un solo `@ControllerAdvice`.
- Separar las excepciones técnicas (500) de las de negocio (OperationFailedException).
- Usar logs con niveles de severidad adecuados.
- Mostrar mensajes claros y breves en el front-end.
