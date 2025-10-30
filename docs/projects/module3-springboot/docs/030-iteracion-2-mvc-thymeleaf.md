# Iteración 2 — MVC con Thymeleaf (CRUD de clientes)

En esta iteración construimos controladores y vistas para un CRUD básico de **Cliente**. Todo el código aparece con su ruta destino.

---

## 1. Controlador MVC — **ruta:** `/src/main/java/com/avante/demo/controller/ClienteController.java`
```java
package com.avante.demo.controller;

import com.avante.demo.model.Cliente;
import com.avante.demo.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository repo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", repo.findAll());
        return "clientes";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "form_cliente";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cliente cliente) {
        repo.save(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", repo.findById(id).orElse(new Cliente()));
        return "form_cliente";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/clientes";
    }
}
```

---

## 2. Vistas Thymeleaf

### 2.1 Listado — **ruta:** `/src/main/resources/templates/clientes.html`
```html
<!DOCTYPE html>
<html xmlns="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <title>Clientes</title>
</head>
<body>
<h1>Listado de Clientes</h1>
<p><a th:href="@{/clientes/nuevo}">Nuevo cliente</a></p>

<table border="1" cellpadding="6">
  <tr><th>ID</th><th>Nombre</th><th>Email</th><th>Acciones</th></tr>
  <tr th:each="c : ${clientes}">
      <td th:text="${c.id}"></td>
      <td th:text="${c.nombre}"></td>
      <td th:text="${c.email}"></td>
      <td>
          <a th:href="@{/clientes/editar/{id}(id=${c.id})}">Editar</a> |
          <a th:href="@{/clientes/eliminar/{id}(id=${c.id})}">Eliminar</a>
      </td>
  </tr>
</table>
</body>
</html>
```

### 2.2 Formulario — **ruta:** `/src/main/resources/templates/form_cliente.html`
```html
<!DOCTYPE html>
<html xmlns="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
  <title>Formulario de Cliente</title>
</head>
<body>
<h1>Cliente</h1>
<form th:action="@{/clientes/guardar}" th:object="${cliente}" method="post">
  <label>Nombre:</label>
  <input type="text" th:field="*{nombre}" required/><br/><br/>
  <label>Email:</label>
  <input type="email" th:field="*{email}" required/><br/><br/>
  <button type="submit">Guardar</button>
</form>
<p><a th:href="@{/clientes}">Volver</a></p>
</body>
</html>
```
