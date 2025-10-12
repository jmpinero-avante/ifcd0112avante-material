---
title: Capítulo 2 — Estructura básica de un proyecto Java con Maven
description: Introducción a Maven y la estructura de proyectos Java en NetBeans, incluyendo configuración, empaquetado y classpath.
---

# Capítulo 2 — Estructura básica de un proyecto Java con Maven

## 2.1. Qué es un proyecto Java

Un **proyecto Java** es una colección de archivos fuente, recursos y configuraciones que conforman una aplicación o biblioteca desarrollada en el lenguaje Java.  
Cada proyecto contiene al menos una clase con un método `main()` que actúa como punto de entrada de la aplicación.

Los proyectos pueden organizarse con herramientas como **Maven**, que facilitan la compilación, la gestión de dependencias y la distribución.

---

## 2.2. Estructura de un proyecto en NetBeans

Al crear un nuevo proyecto en **NetBeans** utilizando **Maven**, se genera una estructura estándar compatible con cualquier entorno de desarrollo:

```
mi-proyecto/
│
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ejemplo/
│   │   │       └── App.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/ejemplo/
│               └── AppTest.java
└── target/
    └── (archivos compilados y empaquetados)
```

**Descripción de carpetas:**
- `src/main/java`: contiene el código fuente principal.  
- `src/main/resources`: archivos de configuración y recursos (como `.properties`, imágenes, etc.).  
- `src/test/java`: pruebas unitarias (JUnit u otras librerías).  
- `target`: se genera al compilar y contiene los `.class` y los `.jar`.

---

## 2.3. Introducción a Maven

**Maven** es una herramienta de **gestión y construcción de proyectos** desarrollada por Apache.  
Su objetivo principal es **automatizar el ciclo de vida del proyecto**, incluyendo:
- Compilación del código fuente
- Ejecución de pruebas
- Empaquetado en `.jar` o `.war`
- Instalación en un repositorio local o remoto

Además, Maven gestiona **dependencias externas** mediante el repositorio central de Maven.

### Ventajas principales
- Estandariza la estructura de proyectos.
- Automatiza tareas repetitivas.
- Facilita la integración continua.
- Permite heredar configuraciones en proyectos grandes.

---

## 2.4. Ciclo de vida de Maven

Maven define fases de ejecución que se pueden invocar desde la terminal:

| Fase | Descripción |
|------|--------------|
| `clean` | Elimina los archivos generados anteriormente (carpeta `target`). |
| `compile` | Compila el código fuente (`src/main/java`). |
| `test` | Ejecuta las pruebas definidas. |
| `package` | Empaqueta la aplicación en un `.jar` o `.war`. |
| `install` | Instala el paquete en el repositorio local (`~/.m2`). |
| `deploy` | Publica el paquete en un repositorio remoto. |

**Ejemplo de uso:**

```bash
mvn clean package
```

Este comando limpia los archivos antiguos y empaqueta el proyecto en un `.jar` listo para ejecutar.

---

## 2.5. Estructura del archivo `pom.xml`

El archivo `pom.xml` (Project Object Model) contiene la configuración central del proyecto Maven.

Ejemplo básico:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ejemplo</groupId>
    <artifactId>mi-proyecto</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Mi Proyecto</name>
    <description>Proyecto de ejemplo en Maven</description>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Campos importantes:**
- `groupId`: identifica el grupo o empresa (ej. dominio invertido).  
- `artifactId`: nombre del proyecto o módulo.  
- `version`: versión del artefacto.  
- `packaging`: tipo de paquete (normalmente `jar`).

---

## 2.6. Configuración de la clase principal

Para indicar a Maven qué clase debe ejecutarse al ejecutar el `.jar`, se añade al `pom.xml` el siguiente plugin:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <version>3.2.2</version>
  <configuration>
    <archive>
      <manifest>
        <addClasspath>true</addClasspath>
        <mainClass>com.ejemplo.App</mainClass>
      </manifest>
    </archive>
  </configuration>
</plugin>
```

Con esto, al generar el `.jar`, se incluirá el atributo `Main-Class` en el manifiesto, permitiendo ejecutar el proyecto con:

```bash
java -jar target/mi-proyecto-1.0-SNAPSHOT.jar
```

---

## 2.7. Empaquetado en formato JAR

El formato `.jar` (**Java ARchive**) es un archivo comprimido que agrupa todos los `.class` y recursos necesarios para ejecutar la aplicación.

Un `.jar` puede ejecutarse con el comando:

```bash
java -jar nombre-del-archivo.jar
```

Maven genera el `.jar` automáticamente al ejecutar:

```bash
mvn package
```

El archivo final se guarda dentro de `target/`.

---

## 2.8. El Classpath en Java

El **classpath** define la **ruta donde la JVM busca las clases y bibliotecas** necesarias para ejecutar un programa.

Puede incluir directorios, archivos `.jar` o rutas del sistema.

### Ejemplo de uso manual

```bash
java -cp "bin;lib/mysql-connector.jar" com.ejemplo.App
```

### Con Maven
Maven gestiona automáticamente el classpath según las dependencias declaradas en el `pom.xml`,  
por lo que no es necesario configurarlo manualmente.

---

## 2.9. Ejemplo completo

1. Crear el proyecto en NetBeans o con el comando:
   ```bash
   mvn archetype:generate -DgroupId=com.ejemplo -DartifactId=hola-maven -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
   ```

2. Editar `pom.xml` y añadir el `mainClass` si se desea ejecutar el `.jar` directamente.

3. Compilar y empaquetar:
   ```bash
   mvn clean package
   ```

4. Ejecutar el resultado:
   ```bash
   java -jar target/hola-maven-1.0-SNAPSHOT.jar
   ```

---

## 2.10. Resumen

- Maven organiza y automatiza la construcción de proyectos Java.  
- El archivo `pom.xml` define la configuración y dependencias.  
- Los proyectos tienen una estructura estándar (`src/main/java`, `src/test/java`).  
- El empaquetado en `.jar` facilita la distribución y ejecución del código.  
- El classpath determina dónde busca la JVM las clases y bibliotecas necesarias.

