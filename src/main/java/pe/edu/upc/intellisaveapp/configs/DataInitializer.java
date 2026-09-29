package pe.edu.upc.intellisaveapp.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.intellisaveapp.entities.Role;
import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.repositories.IUsersRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(IUsersRepository usersRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            if (usersRepository.findByUsername("admin").isEmpty()) {
                Users admin = new Users();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEnabled(true);

                Role role = new Role();
                role.setRol("ROLE_ADMIN");
                role.setUser(admin);
                admin.getRoles().add(role);

                usersRepository.save(admin);
            }
        };
    }
}