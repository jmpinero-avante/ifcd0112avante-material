# Thymeleaf avanzado (fragmentos, layouts, validación, utilidades)

## 1. Fragmentos reutilizables
**Ruta:** `/src/main/resources/templates/fragments/layout.html`
```html
<!DOCTYPE html>
<html xmlns="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <title th:fragment="title">Título</title>
</head>
<body>
  <header th:fragment="header"><h1>Sistema de Clientes</h1></header>
  <main th:fragment="content">Contenido</main>
  <footer th:fragment="footer"><small>© Centro</small></footer>
</body>
</html>
```

**Uso de fragmentos en otra plantilla (ruta conceptual):**
```html
<div th:replace="fragments/layout :: header"></div>
```

## 2. Validación y mensajes
**Ruta:** `/src/main/resources/templates/form_cliente.html` (extensión del ejemplo)
```html
<form th:object="${cliente}" th:action="@{/clientes/guardar}" method="post">
  <input th:field="*{nombre}"/>
  <p th:if="${#fields.hasErrors('nombre')}" th:errors="*{nombre}"></p>
</form>
```

## 3. Expresiones útiles
- `${#dates.format(fecha, 'dd/MM/yyyy')}`
- `${#strings.toUpperCase(texto)}`
- `${#lists.size(lista)}`

## 4. Enlaces y rutas
```html
<a th:href="@{/clientes/editar/{id}(id=${c.id})}">Editar</a>
```
