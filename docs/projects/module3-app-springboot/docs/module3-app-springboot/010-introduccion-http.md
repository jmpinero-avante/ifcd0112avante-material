# Introducción a HTTP y la arquitectura web

## Objetivos
- Comprender los fundamentos de la comunicación HTTP.
- Identificar la estructura cliente-servidor.
- Diferenciar el modelo tradicional MVC del modelo REST.

## Contenidos

### 1. Arquitectura cliente-servidor
El cliente envía solicitudes HTTP al servidor, que responde con HTML o JSON.

```
Cliente (navegador) → Servidor Web → Servidor de Aplicaciones → Base de Datos
```

### 2. El protocolo HTTP
| Método | Descripción | Ejemplo de uso |
|---------|--------------|----------------|
| **GET** | Solicita un recurso. | `/clientes` muestra listado. |
| **POST** | Envía datos al servidor. | Formulario para crear cliente. |
| **PUT** | Modifica un recurso existente. | `/clientes/5` actualiza cliente. |
| **DELETE** | Elimina un recurso. | `/clientes/5` borra cliente. |

### 3. Códigos de estado
200 OK, 201 Created, 400 Bad Request, 404 Not Found, 500 Internal Server Error.

### 4. Qué es REST
REST (Representational State Transfer) usa métodos HTTP estándar y datos en JSON o XML. Es stateless y los recursos se identifican por URL.

### 5. Introducción a Thymeleaf
Motor de plantillas HTML integrado con Spring MVC. Usa expresiones `${}` y atributos `th:` para insertar datos dinámicos.

### Actividad práctica
1. Analizar una petición HTTP en el navegador (pestaña Red).
2. Comparar solicitudes GET y POST a un mismo recurso.
