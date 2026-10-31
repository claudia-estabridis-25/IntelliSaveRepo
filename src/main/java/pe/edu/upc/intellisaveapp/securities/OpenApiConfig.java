package pe.edu.upc.intellisaveapp.securities;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// OpenApiConfig es una clase de configuración que le enseña a Swagger:
// -> cómo se llama tu API
// -> qué versión tiene
// -> qué descripción mostrar
// -> y cómo autenticarse mediante JWT.
// Spring crea un Bean de tipo OpenAPI que será utilizado por SpringDoc OpenAPI (Swagger) para generar
// automáticamente la documentación de todos los endpoints.

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                // Configuración de la información general de la API, que luego se muestra en Swagger UI
                .info(new Info()
                        .title("Demo API")
                        .version("1.0")
                        .description("API REST con Spring Security y JWT"))
                .components(new Components()
                        // Configuración de los esquemas de seguridad para JWT, que luego se muestran en Swagger UI.
                        // Le inidica a Swagger que esta API utiliza autenticación Bearer Token basada en JWT.
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
    //Aplicando seguridad a todos los endpoints de la API, para que requieran un token JWT válido
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );
    }
}
