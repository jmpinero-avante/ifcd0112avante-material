# Iteración 3 — API REST (CRUD JSON)

Construimos una API REST para **Cliente**. Se exponen endpoints JSON para listar, obtener, crear, actualizar y eliminar.

---

## 1. Controlador REST — **ruta:** `/src/main/java/com/avante/demo/api/ClienteRestController.java`
```java
package com.avante.demo.api;

import com.avante.demo.model.Cliente;
import com.avante.demo.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteRestController {

    private final ClienteRepository repo;

    @GetMapping
    public List<Cliente> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        Cliente nuevo = repo.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @RequestBody Cliente datos) {
        return repo.findById(id)
                .map(c -> {
                    c.setNombre(datos.getNombre());
                    c.setEmail(datos.getEmail());
                    return ResponseEntity.ok(repo.save(c));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 2. Ejemplos de peticiones (curl/Postman)

**Crear**
```http
POST /api/clientes
Content-Type: application/json

{"nombre":"María","email":"maria@correo.com"}
```

**Actualizar**
```http
PUT /api/clientes/1
Content-Type: application/json

{"nombre":"María G.","email":"maria.g@correo.com"}
```

**Eliminar**
```http
DELETE /api/clientes/1
```
