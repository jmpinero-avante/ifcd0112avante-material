# Teoría de HTTP y REST

## 1. Estructura de HTTP
Una petición HTTP consta de:
1) **Línea de petición** (método, URL y versión), p. ej.: `GET /clientes HTTP/1.1`  
2) **Cabeceras (headers)**: metadatos (p. ej. `Content-Type`, `Accept`, `Authorization`)  
3) **Cuerpo (body)**: datos enviados en `POST/PUT/PATCH` (JSON, formularios, multipart).

### 1.1 Ejemplo de petición y respuesta (JSON)
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

## 2. Métodos HTTP (semántica)
| Método  | Uso semántico                       | Idempotencia |
|---------|-------------------------------------|--------------|
| GET     | Lectura                              | Sí (seguro)  |
| POST    | Crear recurso                        | No           |
| PUT     | Reemplazar recurso completo          | Sí           |
| PATCH   | Modificar parcialmente               | No (suele)   |
| DELETE  | Eliminar recurso                     | Sí           |

## 3. Códigos de estado frecuentes
- **200 OK**, **201 Created**, **204 No Content**  
- **400 Bad Request**, **404 Not Found**, **409 Conflict**, **422 Unprocessable Entity**  
- **500 Internal Server Error**

## 4. Tipos de contenido (Content-Type)
| Tipo                               | Descripción                                   | Uso habitual                                |
|------------------------------------|-----------------------------------------------|---------------------------------------------|
| `application/x-www-form-urlencoded`| Pares `clave=valor` URL-encoded               | Formularios HTML clásicos                    |
| `multipart/form-data`              | Campos + ficheros (con *boundaries*)          | Subida de imágenes/documentos                |
| `application/json`                 | Datos estructurados JSON                      | API REST                                     |

### 4.1 Formulario clásico (x-www-form-urlencoded)
**HTML (ruta conceptual):** `src/main/resources/templates/form_ejemplo.html`
```html
<form method="post" action="/clientes/guardar">
  <input name="nombre" />
  <input name="email" type="email"/>
  <button type="submit">Enviar</button>
</form>
```
Cuerpo enviado: `nombre=Ana&email=ana%40correo.com`  
En Spring MVC se recibe con `@ModelAttribute Cliente cliente`.

### 4.2 Envío de ficheros (multipart/form-data)
**HTML (ruta conceptual):** `src/main/resources/templates/upload.html`
```html
<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="documento"/>
  <button>Subir</button>
</form>
```
**Spring MVC (ruta conceptual):** `src/main/java/.../controller/UploadController.java`
```java
@PostMapping("/upload")
public String subir(@RequestParam("documento") org.springframework.web.multipart.MultipartFile file) {
    // procesar file.getOriginalFilename(), file.getBytes(), etc.
    return "ok";
}
```

### 4.3 JSON (application/json)
**Petición:**
```http
POST /api/clientes
Content-Type: application/json

{"nombre":"Ana","email":"ana@correo.com"}
```
**Spring REST:** `@RequestBody Cliente cliente` (Jackson).

## 5. REST: principios
- **Recursos** con URLs estables (`/api/clientes`, `/api/pedidos`).
- **Representaciones** (JSON).
- **Sin estado** (stateless): cada petición es autocontenida.
- **Uso correcto de verbos HTTP** (GET/POST/PUT/PATCH/DELETE).
