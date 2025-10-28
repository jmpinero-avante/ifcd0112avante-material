# Introducción y configuración de Hibernate

## Qué es Hibernate

Hibernate es un framework de mapeo objeto–relacional (ORM) que permite trabajar con bases de datos desde Java sin necesidad de escribir SQL de forma directa.  
Su objetivo principal es traducir los objetos Java en registros de una base de datos y viceversa.

Esto evita tener que manejar conexiones, sentencias y `ResultSet` de manera manual, como ocurre con JDBC.  
En lugar de eso, trabajamos con objetos Java, y Hibernate se encarga de ejecutar las consultas SQL necesarias de forma transparente.

Por ejemplo:

```java
Libro libro = new Libro("Cien años de soledad", "Gabriel García Márquez", 19.95);
session.save(libro);
```

Hibernate convertirá automáticamente esa instrucción en un `INSERT` en la tabla `libros`.

---

## Cómo funciona internamente

Hibernate se apoya en tres componentes fundamentales:

1. **SessionFactory**: crea y gestiona las sesiones. Es un objeto costoso, por lo que normalmente se crea una sola instancia durante toda la aplicación.
2. **Session**: representa una conexión con la base de datos. Permite realizar operaciones CRUD (crear, leer, actualizar y eliminar).
3. **Transaction**: agrupa un conjunto de operaciones que deben ejecutarse como una unidad de trabajo.  
   Si algo falla, se hace un `rollback` para revertir los cambios.

---

## Dependencias necesarias

### Archivo `pom.xml`

Usaremos Maven como gestor de dependencias. Añadimos lo siguiente dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.5.2.Final</version>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.32</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.22.0</version>
</dependency>

<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.22.0</version>
</dependency>
```

Estas librerías proporcionan:
- Hibernate (el motor ORM),
- el driver JDBC de PostgreSQL,
- Lombok (para generar constructores, getters y setters automáticamente),
- y Log4j2 (para visualizar los mensajes de log).

---

## Configuración básica

### Archivo `hibernate.cfg.xml`

Este archivo define cómo se conecta Hibernate a la base de datos y qué clases están mapeadas como entidades.

Debe colocarse en `src/main/resources/`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>

        <!-- Conexión a PostgreSQL -->
        <property name="hibernate.connection.driver_class">org.postgresql.Driver</property>
        <property name="hibernate.connection.url">jdbc:postgresql://localhost:5432/biblioteca</property>
        <property name="hibernate.connection.username">postgres</property>
        <property name="hibernate.connection.password">postgres</property>

        <!-- Dialecto SQL -->
        <property name="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</property>

        <!-- Mostrar y formatear las consultas SQL -->
        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>

        <!-- Generación automática del esquema -->
        <property name="hibernate.hbm2ddl.auto">update</property>

        <!-- Registro de las clases mapeadas -->
        <mapping class="com.ejemplo.hibernate.modelo.Libro"/>
        <mapping class="com.ejemplo.hibernate.modelo.Editorial"/>

    </session-factory>
</hibernate-configuration>
```

---

## Explicación de los parámetros principales

| Propiedad | Descripción |
|------------|-------------|
| `hibernate.connection.driver_class` | Especifica el driver JDBC que se usará. |
| `hibernate.connection.url` | Define la URL de conexión a la base de datos. |
| `hibernate.connection.username` y `hibernate.connection.password` | Usuario y contraseña de acceso. |
| `hibernate.dialect` | Indica a Hibernate qué tipo de SQL debe generar (PostgreSQL, MySQL, etc.). |
| `hibernate.show_sql` | Muestra las consultas SQL generadas en la consola. |
| `hibernate.format_sql` | Da formato legible a las consultas SQL. |
| `hibernate.hbm2ddl.auto` | Controla la creación y actualización automática de las tablas. |

### Valores posibles para `hibernate.hbm2ddl.auto`

| Valor | Descripción | Uso recomendado |
|--------|--------------|-----------------|
| `create` | Borra y crea de nuevo las tablas cada vez que se ejecuta la aplicación. | En desarrollo inicial. |
| `create-drop` | Crea las tablas al iniciar y las borra al cerrar. | En pruebas o demos temporales. |
| `update` | Actualiza las tablas si detecta cambios en las entidades. | En desarrollo habitual. |
| `validate` | Comprueba que las tablas existen y son correctas. | En producción. |
| `none` | No realiza ninguna acción sobre la base de datos. | En producción. |

En entornos reales, se recomienda **`validate` o `none`** para evitar modificaciones accidentales en la base de datos.

---

## Configuración de Log4j2

Hibernate genera mensajes de log útiles, como las consultas SQL ejecutadas y los valores de los parámetros.

Creamos el archivo `src/main/resources/log4j2.xml` con este contenido:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d{HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
    </Console>
  </Appenders>

  <Loggers>
    <Root level="info">
      <AppenderRef ref="Console"/>
    </Root>

    <Logger name="org.hibernate.SQL" level="debug" additivity="false">
      <AppenderRef ref="Console"/>
    </Logger>

    <Logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="trace" additivity="false">
      <AppenderRef ref="Console"/>
    </Logger>
  </Loggers>
</Configuration>
```

Con esta configuración podremos ver las sentencias SQL reales que Hibernate ejecuta y los valores que asigna a los parámetros.

---

## Configuraciones alternativas

Además del archivo `hibernate.cfg.xml`, existen dos alternativas:

1. **Archivo `hibernate.properties`**: se usa igual, pero en formato de propiedades.
2. **Configuración mediante código Java**:  
   Puede hacerse con la clase `org.hibernate.cfg.Configuration`.

Ejemplo:

```java
Configuration cfg = new Configuration();
cfg.configure("hibernate.cfg.xml");
SessionFactory sessionFactory = cfg.buildSessionFactory();
```

Estas formas alternativas son útiles cuando se necesita un control más dinámico de la configuración, aunque en la mayoría de los proyectos se utiliza el archivo XML estándar.

---

Con esta configuración lista, ya podemos crear nuestras primeras entidades y probar las operaciones básicas con Hibernate.
