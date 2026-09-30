package pe.edu.upc.intellisaveapp.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.intellisaveapp.entities.Role;
import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.repositories.IUsersRepository;
import pe.edu.upc.intellisaveapp.repositories.IRoleRepository;

/* Esta clase DataInitializer sirve para crear un usuario inicial o seed con el rol Admin
en la tabla Users, para que así podamos iniciar sesión (login) la primera vez que entremos
a la aplicación.
permitAll() en /login (SecurityConfig) permite usar el endpoint login, osea sin eso, para
logearnos necesitaríamos un token, pero como al inicio no tenemos ningún token (primera
vez que accedemos a la aplicación), entonces no podríamos logearnos (el endpoint login
no dejaría usar el "try out", sino que pediría un token antes, lo cual no tendría sentido).
Analogía:
- permitAll() decide quién puede llamar al endpoint: cualquiera, sin token.
- DataInitializer hace que exista un usuario válido para que ese login funcione.
Además, DataInitializer permite que el login nos funcione a todos los miembros del equipo
al arrancar la aplicación. Como cada uno de nosotros tenemos nuestra propia base de datos
local, sin eso cada uno tendríamos que crear el usuario con rol Admin directamente en el
Postgre por separado y luego en Render. Sería más tedioso y largo.
*/

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(IUsersRepository usersRepository,
                                           IRoleRepository roleRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            if (usersRepository.findByEmailUser("admin@intellisave.pe").isEmpty()) {
                // 1. Crear y guardar el usuario
                Users admin = new Users();
                admin.setEmailUser("admin@intellisave.pe");
                admin.setPasswordUser(passwordEncoder.encode("admin123"));
                admin.setPositionUser("Administrador");
                admin.setDniUser("00000000");
                admin.setFirstName("Admin");
                admin.setPaternalSurname("IntelliSave");
                admin.setMaternalSurname("Admin");
                admin.setTelephoneUser("900000000");
                admin.setStatusUser(true);

                Users adminGuardado = usersRepository.save(admin);

                // 2. Crear y guardar su rol, apuntando al usuario ya guardado
                Role role = new Role();
                role.setNameRole("ROLE_ADMIN");
                role.setDescriptionRole("Permisos de administrador.");
                role.setUser(adminGuardado);

                roleRepository.save(role);
            }
        };
    }
}