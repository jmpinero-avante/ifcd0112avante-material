---
title: "10 – Instalación y Configuración de PostgreSQL + pgAdmin + JDBC"
---

# 10 – Instalación y Configuración de PostgreSQL + pgAdmin + JDBC
## Instalación de PostgreSQL y pgAdmin en Windows
Descarga el instalador oficial desde:

```plain
https://www.postgresql.org/download/windows/
```

1. Ejecuta el instalador de EnterpriseDB (EDB) y selecciona los componentes:
    - PostgreSQL Server
    - pgAdmin 4
    - Command Line Tools
2. Define una contraseña para el usuario postgres.
3. Deja el puerto por defecto 5432.
4. Anota la carpeta de datos (por ejemplo C:\Program Files\PostgreSQL\16\data).

Al finalizar, PostgreSQL se inicia automáticamente como servicio de Windows.
Puedes comprobarlo en el Administrador de tareas (pestaña Servicios) o con PowerShell:

```bash
net start postgresql-x64-16
```

## Activar acceso por contraseña en Windows
Por defecto, el usuario postgres puede acceder localmente sin contraseña.
Para requerir contraseña (necesario para pgAdmin, DBeaver o JDBC):

1.- Abre el archivo:

```plain
C:\Program Files\PostgreSQL\16\data\pg_hba.conf
```

2.- Busca la línea:

```plain
local   all             postgres                                trust
```

3.- Cámbiala por:

```plain
local   all             postgres                                md5
```

Opcional: permitir acceso local TCP/IP:

```plain
host    all             all             127.0.0.1/32            md5
```

Guarda los cambios y reinicia el servicio:

```bash
net stop postgresql-x64-16
net start postgresql-x64-16
```

Verifica el acceso:

```bash
psql -U postgres -h localhost
```

Debería pedir la contraseña configurada durante la instalación.

## Instalación de PostgreSQL y pgAdmin en Linux (Ubuntu, Mint, Pop!_OS, etc.)
### Opción 1 – Repositorios estándar (versión estable de Ubuntu)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib pgadmin4 -y
```

### Opción 2 (recomendada) – Repositorios oficiales de PostgreSQL.org
Permite instalar versiones más recientes (por ejemplo PostgreSQL 16 o 17).

```bash
sudo apt install wget ca-certificates -y
wget -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | \
   sudo apt-key add -
. /etc/os-release
echo "deb http://apt.postgresql.org/pub/repos/apt ${VERSION_CODENAME}-pgdg main" | \
   sudo tee /etc/apt/sources.list.d/pgdg.list
sudo apt update
sudo apt install postgresql postgresql-contrib -y
```

Verifica que el servicio esté activo:

```bash
sudo systemctl status postgresql
```

(Opcional) Instalar una versión específica:

```bash
sudo apt install postgresql-16 postgresql-contrib -y
```

## Cambiar la contraseña del usuario postgres
```bash
sudo -u postgres psql
```

```sql
ALTER USER postgres PASSWORD 'nueva_contraseña';
```

Ejemplo:

```sql
ALTER USER postgres PASSWORD 'admin123';
```

```bash
\q
```

## Permitir acceso por contraseña (modo md5)
Edita el archivo de configuración de autenticación:

```bash
sudo nano /etc/postgresql/16/main/pg_hba.conf
```

Reemplaza:

```plain
local   all             postgres                                peer
```

Por:

```plain
local   all             postgres                                md5
```

Y añade:

```plain
host    all             all             127.0.0.1/32            md5
```

```bash
sudo systemctl restart postgresql
```

| Método | Significado |
| --- | --- |
| peer | Usa el usuario del sistema operativo (sin contraseña) |
| md5 | Requiere contraseña cifrada (recomendado) |
| trust | Permite acceso sin contraseña (solo para pruebas locales) |

## Crear usuarios y bases de datos
```bash
sudo -u postgres psql
```

```sql
CREATE USER juanma WITH PASSWORD '12345';

CREATE DATABASE cursos;

GRANT ALL PRIVILEGES ON DATABASE cursos TO juanma;

\c cursos

GRANT ALL PRIVILEGES ON SCHEMA public TO juanma;

ALTER ROLE juanma CREATEDB;
```

## Conectarse con psql y pgAdmin
### Desde la terminal
```bash
psql -U juanma -d cursos -h localhost -W
```

### Desde pgAdmin
1. Abre pgAdmin y crea una Master Password local.
2. En Servers → Create → Server:
    - Name: Localhost
    - Host: 127.0.0.1
    - Port: 5432
    - Username: postgres
    - Password: la configurada
3. Guarda la conexión y accede a la base de datos.

## Instalar el driver JDBC
### Instalación manual
Descarga desde:

```plain
https://jdbc.postgresql.org/download.html
```

Archivo típico:

```plain
postgresql-42.7.2.jar
```

Estructura de proyecto:

```text
ProyectoJava/
├─ src/
└─ lib/postgresql-42.7.2.jar
```

Compila y ejecuta con:

```bash
javac -cp ".:lib/postgresql-42.7.2.jar" Main.java
java  -cp ".:lib/postgresql-42.7.2.jar" Main
```

(En Windows usa ; en lugar de :)

### Instalación con Maven
```xml
<dependencies>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.2</version>
    </dependency>
</dependencies>
```

```bash
mvn dependency:tree
```

Deberías ver:

```plain
org.postgresql:postgresql:jar:42.7.2:compile
```
