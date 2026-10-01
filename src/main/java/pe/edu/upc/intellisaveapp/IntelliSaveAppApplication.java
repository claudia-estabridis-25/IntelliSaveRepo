package pe.edu.upc.intellisaveapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Aquí también podemos encontrar Spring Boot, en la clase base

@SpringBootApplication //Anotación de Spring Boot que indica que esta clase es la clase principal de la aplicación
public class IntelliSaveAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelliSaveAppApplication.class, args);
    }

}

// Servidor embebido para depurar y ejecutar, sin tener que instalar un servidor externo.
// La línea donde aparece el primer botonsito verde de "Run" es la que ejecuta la aplicación.
// También se puede ejecutar desde la terminal con el comando: mvn spring-boot:run
// En resumen, Spring Boot nos da las funcionalidades de Spring, pero con una configuración mínima (la anotación)
// y un servidor embebido para ejecutar la aplicación.
