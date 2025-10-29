# Teoría de HTTP y REST

## 1. Estructura de HTTP
Una petición HTTP consta de:
1) **Línea de petición**: MÉTODO, URL y versión (`GET /clientes HTTP/1.1`)
2) **Cabeceras** (Headers): metadatos como `Content-Type`, `Accept`, `Authorization`.
3) **Cuerpo** (Body): datos enviados (en POST/PUT/PATCH/multipart).

### Ejemplo de petición y respuesta (JSON)
```
POST /api/clientes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{"nombre": "Ana Pérez", "email": "ana@correo.com"}
```
```
HTTP/1.1 201 Created
Content-Type: application/json

{"id": 1, "nombre": "Ana Pérez", "email": "ana@correo.com"}
```

## 2. Métodos HTTP y semántica
| Método  | Uso semántico                       | Ejemplo en Spring |
|---------|-------------------------------------|-------------------|
| GET     | Lectura (idempotente, seguro)       | `@GetMapping`     |
| POST    | Crear recurso                        | `@PostMapping`    |
| PUT     | Reemplazar recurso completo          | `@PutMapping`     |
| PATCH   | Actualizar parcialmente              | `@PatchMapping`   |
| DELETE  | Eliminar recurso                     | `@DeleteMapping`  |

## 3. Códigos de estado frecuentes
- 200 OK, 201 Created, 204 No Content
- 400 Bad Request, 404 Not Found
- 409 Conflict, 422 Unprocessable Entity (semántico)
- 500 Internal Server Error

## 4. Tipos de contenido (Content-Type)
| Tipo                             | Descripción                                    | Ejemplo de uso                            |
|----------------------------------|------------------------------------------------|-------------------------------------------|
| `application/x-www-form-urlencoded` | Pares `clave=valor` URL-encoded                | Formularios HTML clásicos                  |
| `multipart/form-data`            | Campos + ficheros (límites/boundaries)         | Subida de imágenes/documentos              |
| `application/json`               | Datos estructurados JSON                       | API REST modernas                          |

### 4.1 Formulario clásico (x-www-form-urlencoded)
HTML:
```html
<form method="post" action="/clientes/guardar">
  <input name="nombre" />
  <input name="email" type="email"/>
  <button type="submit">Enviar</button>
</form>
```
Cuerpo enviado: `nombre=Ana&email=ana%40correo.com`

Spring MVC lo recibe con `@ModelAttribute Cliente cliente`.

### 4.2 Envío de ficheros (multipart/form-data)
HTML:
```html
<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="documento"/>
  <button>Subir</button>
</form>
```
Spring MVC lo recibe con:
```java
@PostMapping("/upload")
public String subir(@RequestParam("documento") MultipartFile file) { /* ... */ return "ok"; }
```

### 4.3 JSON (application/json)
Cliente → Servidor:
```http
POST /api/clientes
Content-Type: application/json

{"nombre":"Ana","email":"ana@correo.com"}
```
Spring REST lo mapea con `@RequestBody Cliente cliente` (Jackson).

## 5. REST: principios clave
- **Recursos** con URLs estables (`/api/clientes`, `/api/pedidos`)
- **Representaciones** (JSON)
- **Stateless** (sin estado de sesión en el servidor)
- **Verb-centric** (usar GET/POST/PUT/DELETE adecuadamente)
