# 🧰 Utilidades de administración — HTMLApp

Este directorio contiene herramientas auxiliares en forma de scripts que permiten
interactuar con las utilidades Java del paquete:

```
com.example.htmlapp.tools
```

Las utilidades permiten generar contraseñas, verificar hashes y crear usuarios
directamente en la base de datos PostgreSQL del proyecto.

---

## 📁 Estructura

```
scripts/
 ├── linux/
 │    ├── generate-password-hash.sh
 │    ├── verify-password.sh
 │    └── generate-user-insert.sh
 │
 └── windows/
      ├── generate-password-hash.bat
      ├── verify-password.bat
      └── generate-user-insert.bat
```

Todos los scripts invocan las clases Java mediante **Maven** usando el plugin `exec:java`.

---

## 🧩 Scripts disponibles

### 1️⃣ `GeneratePasswordHash`
> Genera el `salt` y el `hash` de una contraseña.

#### 🐧 Linux / macOS
```bash
./scripts/linux/generate-password-hash.sh miContraseñaSegura
```

#### 🪟 Windows
```bat
scripts\windows\generate-password-hash.bat miContraseñaSegura
```

🔹 Si no se pasa ninguna contraseña como argumento, el programa la pedirá por teclado.  
🔹 Devuelve el `salt`, el `hash` y un ejemplo de `INSERT` SQL.

---

### 2️⃣ `VerifyPassword`
> Verifica si una contraseña coincide con un `salt` y `hash` almacenados.

#### 🐧 Linux / macOS
```bash
./scripts/linux/verify-password.sh \  --password miContraseñaSegura \  --salt a9BzQkR2x1tP7fW3 \  --hash 5f4dcc3b5aa765d61d8327deb882cf99
```

#### 🪟 Windows
```bat
scripts\windows\verify-password.bat ^
  --password miContraseñaSegura ^
  --salt a9BzQkR2x1tP7fW3 ^
  --hash 5f4dcc3b5aa765d61d8327deb882cf99
```

🔹 Si no se pasan los parámetros, el programa los pedirá de forma interactiva.  
🔹 Devuelve `Password matches: true` o `false` según el resultado.

---

### 3️⃣ `GenerateUserInsert`
> Genera y (opcionalmente) ejecuta un `INSERT INTO users`.

#### 🐧 Linux / macOS
```bash
./scripts/linux/generate-user-insert.sh \  --email user@example.com \  --password 1234 \  --full-name "Juan Pérez" \  --is-admin false
```

#### 🪟 Windows
```bat
scripts\windows\generate-user-insert.bat ^
  --email user@example.com ^
  --password 1234 ^
  --full-name "Juan Pérez" ^
  --is-admin false
```

🔹 Si se añade `--execute`, el script conecta a la base de datos PostgreSQL y realiza la inserción.  
🔹 Requiere `--db-url`, `--db-user` y `--db-password` para ejecutar.

Ejemplo completo (modo ejecución):
```bash
./scripts/linux/generate-user-insert.sh \  --email admin@example.com \  --password admin1234 \  --is-admin true \  --execute \  --db-url jdbc:postgresql://localhost:5432/mibd \  --db-user postgres \  --db-password 1234
```

---

## 📚 Notas didácticas

- Todos los scripts son multiplataforma y pueden adaptarse fácilmente.
- Se recomienda ejecutarlos desde la raíz del proyecto para evitar errores de ruta.
- El código Java se encuentra en `src/main/java/com/example/htmlapp/tools`.
- Las utilidades se basan en el servicio `PasswordService`, garantizando coherencia
  con el sistema de autenticación de la aplicación.
