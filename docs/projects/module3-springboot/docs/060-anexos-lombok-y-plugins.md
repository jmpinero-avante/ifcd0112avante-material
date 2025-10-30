# Anexos — Lombok y plugins

## 1. Lombok (resumen)
- `@Data` → getters/setters/equals/hashCode/toString
- `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Builder` (patrón builder)
- `@RequiredArgsConstructor` para inyección por constructor

## 2. maven-compiler-plugin (Java 21 + annotation processor)
**(Incluido ya en el `pom.xml` principal)**  
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.11.0</version>
  <configuration>
    <source>21</source>
    <target>21</target>
    <annotationProcessorPaths>
      <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.34</version>
      </path>
    </annotationProcessorPaths>
    <compilerArgs>
      <arg>-parameters</arg>
    </compilerArgs>
  </configuration>
</plugin>
```

## 3. NetBeans + Lombok
- Habilitar **procesamiento de anotaciones** (Opciones → Java → Compilador).
- Si el IDE no reconoce las anotaciones, ejecutar `java -jar lombok.jar` y seleccionar la instalación del IDE.

## 4. Buenas prácticas
- Inyección por constructor con `@RequiredArgsConstructor` en lugar de `@Autowired` en campo.
- `@Transactional` en servicios cuando aplique.
- Uso de DTOs cuando la API pública no deba exponer el dominio directamente.
