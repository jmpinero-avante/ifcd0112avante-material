# Sistema de Manejo de Errores en *HtmlApp*

## Objetivo general

El sistema de manejo de errores de *HtmlApp* tiene tres metas principales:

1. **Mostrar mensajes claros y coherentes al usuario**, sin exponer detalles técnicos.
2. **Registrar información completa en logs** (para depuración y análisis posterior).
3. **Mantener coherencia visual** con el resto de la aplicación mediante plantillas Thymeleaf y el layout global.

---

## Estructura del sistema

```
com.example.htmlapp.controller
 └── ErrorControllerAdvice.java     ← Captura y gestiona excepciones

src/main/resources/templates/error/
 ├── 400.html                       ← Parámetros inválidos
 ├── 403.html                       ← Acceso denegado
 ├── 404.html                       ← No encontrado
 ├── operation-error.html           ← Error de integridad o BD
 └── generic-error.html             ← Error 500 general

src/main/resources/templates/layout.html  ← Layout global (header, footer, parallax)
```

---

## 1. Captura de excepciones con `@ControllerAdvice`

`ErrorControllerAdvice` es una clase global de Spring que **intercepta las excepciones** lanzadas por cualquier controlador o servicio.

### Estructura básica

```java
@ControllerAdvice
@Slf4j
public class ErrorControllerAdvice {

  @ExceptionHandler(SecurityException.class)
  public String handleSecurityException(SecurityException ex, Model model) {
    log.warn("Acceso denegado: {}", ex.getMessage());
    log.debug("StackTrace completo", ex);
    model.addAttribute("errorMessage", ex.getMessage());
    model.addAttribute("errorCode", 403);
    return "error/403";
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
    log.warn("Solicitud inválida: {}", ex.getMessage());
    log.debug("StackTrace completo", ex);
    model.addAttribute("errorMessage", ex.getMessage());
    model.addAttribute("errorCode", 400);
    return "error/400";
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public String handleIntegrity(DataIntegrityViolationException ex, Model model) {
    log.error("Violación de integridad en BD: {}", ex.getMessage());
    log.debug("StackTrace completo", ex);
    model.addAttribute("errorMessage",
      "La operación no se pudo completar: posible duplicado o dato inválido.");
    model.addAttribute("errorCode", 409);
    return "error/operation-error";
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public String handleNotFound(NoHandlerFoundException ex, Model model) {
    log.warn("Ruta no encontrada: {}", ex.getRequestURL());
    log.debug("StackTrace completo", ex);
    model.addAttribute("errorMessage", "La página solicitada no existe.");
    model.addAttribute("errorCode", 404);
    return "error/404";
  }

  @ExceptionHandler(Exception.class)
  public String handleGeneric(Exception ex, Model model) {
    log.error("Error inesperado: {}", ex.getMessage());
    log.debug("StackTrace completo", ex);
    model.addAttribute("errorMessage",
      "Se ha producido un error inesperado. Contacte con el administrador.");
    model.addAttribute("errorCode", 500);
    return "error/generic-error";
  }
}
```

---

## 2. Integración con las plantillas Thymeleaf

Todas las vistas de error utilizan el **layout global `layout.html`**,  
lo que garantiza un diseño consistente (barra superior, fondo parallax, pie, estilos).

Ejemplo — `templates/error/403.html`:

```html
<head th:replace="layout :: head('Acceso denegado (403)')"></head>
<body>
  <header th:replace="layout :: header"></header>

  <main th:replace="layout :: body">
    <section class="error-card" role="alert">
      <h1>Acceso denegado</h1>
      <p class="lead">No dispone de permisos para acceder a este recurso.</p>
      <p th:if="${errorMessage}" th:text="${errorMessage}"></p>
      <p class="code">
        Código de error: <strong th:text="${errorCode}">403</strong>
      </p>
      <div class="actions">
        <a class="btn" th:href="@{/}">Volver al inicio</a>
      </div>
    </section>
  </main>

  <footer th:replace="layout :: footer"></footer>
</body>
```

---

## 3. Tipos de errores gestionados

| Tipo de error | Clase capturada | Vista Thymeleaf | Código HTTP | Descripción |
|----------------|----------------|----------------|--------------|--------------|
| Acceso denegado | `SecurityException` | `error/403.html` | 403 | Usuario sin permisos |
| Parámetros inválidos | `IllegalArgumentException` | `error/400.html` | 400 | Argumentos GET/POST erróneos |
| Violación de integridad | `DataIntegrityViolationException` | `error/operation-error.html` | 409 | Duplicados o errores de BD |
| No encontrado | `NoHandlerFoundException` | `error/404.html` | 404 | Ruta inexistente |
| Error general | `Exception` | `error/generic-error.html` | 500 | Fallo no controlado |

---

## 4. Logging y niveles de severidad

| Nivel | Uso | Ejemplo |
|--------|-----|----------|
| **WARN** | Errores previstos o controlados (por ejemplo, intento de acceso no autorizado). | "Acceso denegado a recurso /admin" |
| **ERROR** | Fallos graves o excepciones no controladas. | "Error inesperado al guardar usuario" |
| **DEBUG** | Trazas completas (stacktrace) para depuración. | "StackTrace completo" |

Esto enseña buenas prácticas de logging profesional: **mensajes claros, niveles adecuados y separación de información visible al usuario** (modelo) frente a **información interna** (logs).

---

## 5. Flujo completo de un error

```
Controlador o servicio lanza una excepción
        ↓
ErrorControllerAdvice intercepta la excepción
        ↓
Se registra el evento en los logs (WARN/ERROR + DEBUG)
        ↓
Se añaden al modelo los atributos errorMessage y errorCode
        ↓
Spring muestra la plantilla Thymeleaf correspondiente
        ↓
El usuario ve un mensaje claro, sin información sensible
```

---

## 6. Configuración adicional en `application.yml`

Asegúrate de que estas opciones están activadas para permitir los errores 404 personalizados:

```yaml
spring:
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false
```

---

## 7. Ejemplo de uso didáctico

Puedes crear una pequeña ruta de prueba para provocar distintos errores:

```java
@GetMapping("/test-error/{type}")
public String testError(@PathVariable String type) {
  return switch (type) {
    case "400" -> throw new IllegalArgumentException("Parámetro inválido simulado");
    case "403" -> throw new SecurityException("Simulación de acceso denegado");
    case "409" -> throw new DataIntegrityViolationException("Duplicado en BD");
    case "404" -> throw new NoHandlerFoundException("GET", "/fake-url", null);
    default -> throw new RuntimeException("Error inesperado simulado");
  };
}
```

---

## Conclusión

Este sistema muestra a los alumnos cómo **Spring Boot y Thymeleaf** permiten:
- Gestionar errores globalmente con `@ControllerAdvice`.
- Distinguir entre **errores previstos** (400, 403, 409) y **no previstos** (500).
- Registrar trazas útiles para desarrollo sin exponer detalles al usuario.
- Mantener un **diseño unificado** con fragmentos y layouts reutilizables.
