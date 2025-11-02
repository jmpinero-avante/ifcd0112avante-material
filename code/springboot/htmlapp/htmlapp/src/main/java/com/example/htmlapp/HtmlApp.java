package com.example.htmlapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación HTMLApp.
 *
 * Esta clase actúa como punto de entrada de la aplicación Spring Boot.
 * 
 * - La anotación @SpringBootApplication agrupa:
 *   @Configuration  → indica que puede contener beans de configuración.
 *   @EnableAutoConfiguration → activa la configuración automática de Spring Boot.
 *   @ComponentScan → busca automáticamente todos los componentes, servicios y controladores
 *                    dentro del paquete com.example.htmlapp y sus subpaquetes.
 */
@SpringBootApplication
public class HtmlApp {

    /**
     * Método main: punto de entrada de la aplicación.
     * 
     * SpringApplication.run(...) lanza el servidor embebido (Tomcat por defecto)
     * y carga el contexto de Spring Boot con todas las configuraciones y beans detectados.
     */
    public static void main(String[] args) {
        SpringApplication.run(HtmlApp.class, args);
    }
}