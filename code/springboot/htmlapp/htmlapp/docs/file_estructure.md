```
htmlapp/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/htmlapp/
│   │   │       ├── HtmlApp.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java                # (login/registro)
│   │   │       │   ├── UserController.java                # (datos personales)
│   │   │       │   ├── UserListController.java            # (listado de usuarios)
│   │   │       │   └── ErrorControllerAdvice.java         # (manejador global de errores)
│   │   │       │
│   │   │       └── model/
│   │   │           ├── db/
│   │   │           │   ├── User.java                      # Entidad JPA
│   │   │           │   ├── UserRepository.java            # Repositorio principal
│   │   │           │   ├── UserRepositoryCustom.java      # Interface personalizada
│   │   │           │   └── UserRepositoryCustomImpl.java  # Implementación personalizada
│   │   │           │
│   │   │           ├── enums/
│   │   │           │   ├── UserOrderField.java            # Campo de ordenación (EMAIL, FULL_NAME...)
│   │   │           │   └── SortDirection.java             # Dirección (ASC, DESC)
│   │   │           │
│   │   │           └── logic/
│   │   │               ├── AuthService.java               # Manejo de sesión y login/logout
│   │   │               ├── PasswordService.java           # Hash, salt y cifrado
│   │   │               ├── PermissionsService.java        # Verificación de permisos
│   │   │               ├── UserService.java               # CRUD individual del usuario
│   │   │               └── UserListService.java           # Operaciones en bloque (admin)
│   │   │
│   │   ├── resources/
│   │   │   ├── application.yml                            # Configuración general
│   │   │   │
│   │   │   ├── templates/
│   │   │   │   ├── index.html                             # Página principal (login/registro)
│   │   │   │   ├── main.html                              # Página con lorem ipsum y parallax
│   │   │   │   ├── user/
│   │   │   │   │   ├── profile.html                       # Ver datos personales
│   │   │   │   │   ├── edit.html                          # Editar datos personales
│   │   │   │   │   └── change-password.html               # Cambiar contraseña
│   │   │   │   │
│   │   │   │   ├── admin/
│   │   │   │   │   ├── user-list.html                     # Listado de usuarios (admin)
│   │   │   │   │   ├── user-bulk-success.html             # Confirmación de operación en bloque
│   │   │   │   │   └── user-delete-confirm.html           # Confirmar eliminación
│   │   │   │   │
│   │   │   │   └── error/
│   │   │   │       ├── 400.html                           # Parámetros inválidos
│   │   │   │       ├── 403.html                           # Acceso denegado
│   │   │   │       ├── 404.html                           # No encontrado
│   │   │   │       ├── operation-error.html               # Error de integridad de BD
│   │   │   │       └── generic-error.html                 # Error 500 general
│   │   │   │
│   │   │   └── static/
│   │   │       ├── css/
│   │   │       │   ├── base.css                           # Estilos base de toda la app
│   │   │       │   └── error.css                          # Estilos específicos de páginas de error
│   │   │       │
│   │   │       ├── js/
│   │   │       │   ├── rellax.min.js                      # Librería externa Parallax
│   │   │       │   └── parallax-init.js                   # Inicialización simple del efecto
│   │   │       │
│   │   │       ├── img/
│   │   │       │   └── background.jpg                     # Imagen de fondo para el parallax
│   │   │       │
│   │   │       └── sql/
│   │   │           └── users.sql                          # Esquema SQL descargable
│   │   │
│   │   └── scripts/
│   │       └── linux/
│   │           ├── GeneratePasswordHash.sh                # Wrapper CLI para Java GeneratePasswordHash
│   │           ├── CheckPasswordHash.sh                   # Verificar contraseña
│   │           └── CreateUserInsert.sh                    # Generar INSERT SQL de usuario
│   │
│   └── test/                                              # (No usamos tests por decisión tuya)
│
└── README.md                                              # Instrucciones para alumnos/profesorado
```