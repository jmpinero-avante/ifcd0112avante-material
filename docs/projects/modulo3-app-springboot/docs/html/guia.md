
# 💻 GUÍA PRÁCTICA COMPLETA: DE JDBC A SPRING BOOT MVC CON HIBERNATE, LOMBOK Y THYMELEAF

## 🎯 Objetivo general

Construir paso a paso una aplicación CRUD en Java que acceda a una base de datos PostgreSQL, aplicando los principios del **patrón MVC**.  
A lo largo del proceso aprenderás a usar:
- **JDBC** (bajo nivel)
- **POJOs y JavaBeans**
- **Lombok** (para reducir código repetitivo)
- **Spring Boot**
- **JdbcTemplate**
- **Hibernate (JPA)**
- **Thymeleaf** (plantillas HTML integradas en MVC)

---

# 🧭 Antes de empezar

### 1️⃣ Instalar Maven en Windows
1. Descarga Maven desde [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)  
   (elige “Binary zip archive”, por ejemplo `apache-maven-3.9.9-bin.zip`)
2. Descomprime en una carpeta, por ejemplo `C:\apache-maven-3.9.9`
3. Configura variables de entorno:  
   - `MAVEN_HOME = C:\apache-maven-3.9.9`  
   - Añade a `Path`: `%MAVEN_HOME%\bin`  
   - Asegura `JAVA_HOME` (por ejemplo `C:\Program Files\Java\jdk-17`)
4. Verifica instalación con `mvn -v`

### 2️⃣ Crear la tabla en PostgreSQL
```sql
CREATE TABLE IF NOT EXISTS empleados (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  salario NUMERIC(10,2) NOT NULL
);

INSERT INTO empleados (nombre, salario) VALUES
('Ana', 1800),
('Luis', 2200),
('María', 2100);
```

---

# 🧩 Qué es el patrón MVC

**MVC (Modelo - Vista - Controlador)** separa responsabilidades:

| Componente | Rol | En nuestra app |
|-------------|-----|----------------|
| **Modelo** | Representa los datos y la lógica de negocio | Clase `Empleado` y su repositorio |
| **Vista** | Presenta los datos al usuario | Consola, JSON o HTML (Thymeleaf) |
| **Controlador** | Coordina la comunicación entre Vista y Modelo | Clases que manejan peticiones y devuelven resultados |

Esta separación facilita la **mantenibilidad**, la **escalabilidad** y las **pruebas** del código.

---

# 🧱 POJO vs JavaBean

Antes de crear el modelo de datos (`Empleado`), conviene entender dos conceptos fundamentales.

## 🔹 ¿Qué es un POJO?
**POJO (Plain Old Java Object)** es una clase Java simple que no depende de ninguna librería ni framework.  
Solo contiene atributos, constructores y métodos.

```java
public class Empleado {
    private int id;
    private String nombre;
    private double salario;
}
```

**Características:**
- No extiende ni implementa clases especiales (solo `Object`).
- No usa anotaciones ni herencias de frameworks.
- Es fácil de probar y reutilizar.

## 🔹 ¿Qué es un JavaBean?
Un **JavaBean** es un **POJO con un conjunto de convenciones** adicionales.

**Requisitos:**
1. Tener un **constructor público sin argumentos**.  
2. Usar **atributos privados** y **métodos públicos get/set**.  
3. Ser **serializable** (opcional, pero común).

```java
public class Empleado implements java.io.Serializable {
    private int id;
    private String nombre;
    private double salario;

    public Empleado() {}
    public Empleado(int id, String nombre, double salario) {
        this.id = id; this.nombre = nombre; this.salario = salario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
}
```

| Característica | POJO | JavaBean |
|-----------------|------|----------|
| Constructor vacío obligatorio | No | Sí |
| Métodos getter/setter | Opcional | Obligatorio |
| Atributos privados | Recomendado | Requerido |
| Serializable | No necesariamente | Recomendado |
| Dependencia de frameworks | Ninguna | Tampoco |

💡 **Conclusión:**  
Todo **JavaBean es un POJO**, pero no todo POJO es un JavaBean.

---

# ⚙️ Iteración 1 — Conexión JDBC y SELECT simple

*(Código y explicaciones de la primera versión de la guía)*

---

# 🧱 Iteración 2 — Separar en Modelo, Vista y Controlador (MVC básico)
*(Código y explicación del patrón MVC con consola)*

---

# 🧮 Iteración 3 — PreparedStatement e inserciones
*(Uso de parámetros, consultas seguras y métodos de escritura)*

---

# 🧩 Iteración 4 — Uso de Lombok
*(Uso de anotaciones @Data, @NoArgsConstructor, @AllArgsConstructor)*

---

# 🌱 Iteración 5 — Spring Boot + JdbcTemplate (REST básico)
*(Explicación de aplicación REST, JSON, y JdbcTemplate con ejemplo de controlador)*

---

# 🏗️ Iteración 6 — Hibernate (JPA) con Spring Data
*(Uso de entidades JPA, repositorio y CRUD completo con JPA)*

---

# 🧱 Iteración 7 — Validaciones y transacciones
*(Uso de @Valid, @Transactional, logs con @Slf4j y buenas prácticas)*

---

# 🧩 Iteración 8 — Plantillas HTML con Thymeleaf
*(Controlador MVC con vistas Thymeleaf: empleados.html, empleado-form.html e index.html)*

---

# 🧪 Ejercicios prácticos

1. Añade botón “Editar” que cargue los datos actuales.  
2. Crea una vista `detalle.html` para mostrar un empleado.  
3. Usa `@Valid` y muestra errores con `th:errors`.  
4. Crea un `layout.html` base.  
5. Añade Bootstrap en `/static/css/`.

---

# 📚 Resumen final

| Iteración | Vista | Controlador | Modelo | Tecnología |
|------------|--------|-------------|----------|-------------|
| 1–3 | Consola | Java | POJO | JDBC |
| 4 | Consola | Java | JavaBean + Lombok | Lombok |
| 5 | JSON (REST) | `@RestController` | POJO | Spring Boot + JdbcTemplate |
| 6–7 | JSON (REST) | `@RestController` | Entidad JPA | Hibernate / Spring Data |
| 8 | HTML (servidor) | `@Controller` | Entidad JPA | Thymeleaf (MVC completo) |

---

# 🧭 Conclusión

Con esta guía has aprendido a **evolucionar una aplicación Java paso a paso**, desde JDBC hasta Spring Boot con vistas HTML completas, aplicando el patrón **MVC** en todas sus etapas.
