package pe.edu.upc.intellisaveapp.configs;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ModelMapper es una clase de configuración de Spring Boot
// @Configuration le indica a Spring que esta clase contiene configuraciones y beans
// que deben formar parte del contenedor de Spring.
// Cuando la aplicación inicia, Spring escanea esta clase.
// @Configuration le indica a Spring que esta clase ModelMapperConfig contiene instrucciones de configuración
// para la aplicación. Si no se añade la anotación @Configuration, Spring no escaneará esta clase como clase de
// configuración y por lo tanto, podría no reconocer el bean de ModelMapper que se define dentro de ella, osea que
// podría no registrar el Bean.
@Configuration
public class ModelMapperConfig {
    // @Bean crea un objeto de tipo ModelMapper y lo registra en el contenedor de Spring, para que pueda ser
    // inyectado en otras clases. Es decir, Spring crea una sola vez este objeto bean de tipo ModelMapper y lo guarda,
    // para que después se pueda usar este bean en cualquier parte de la aplicación, sin necesidad de crear una
    // nueva instancia cada vez que se necesita usar ModelMapper. ModelMapper es una librería que permite mapear
    // objetos de un tipo a otro, por ejemplo, de una entidad a un DTO y viceversa.
    @Bean //Bean básicamente es un objeto compartido, inyectable en diferentes clases.
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
