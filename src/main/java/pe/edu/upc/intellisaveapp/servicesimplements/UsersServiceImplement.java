package pe.edu.upc.intellisaveapp.servicesimplements;
import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Role;
import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.repositories.IRoleRepository;
import pe.edu.upc.intellisaveapp.repositories.IUsersRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IUsersService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsersServiceImplement implements IUsersService {
    // Los 3 roles fijos del sistema (Rol no tiene CRUD propio)
    public static final Map<String, String> ROLES_VALIDOS = Map.of(
            "ROLE_ADMIN", "Administrador de la empresa",
            "ROLE_SUPERVISOR", "Supervisor energético",
            "ROLE_EMPLOYEE", "Empleado de área"
    );

    private final IUsersRepository uR;
    private final IRoleRepository rR;

    public UsersServiceImplement(IUsersRepository uR, IRoleRepository rR) {
        this.uR = uR;
        this.rR = rR;
    }

    @Override
    public List<Users> list() {
        return uR.findAll();
    }

    @Override
    public Optional<Users> listById(Long id) {
        return uR.findById(id);
    }

    @Override
    public Optional<Users> findByEmail(String email) {
        return uR.findByEmailUser(email);
    }

    @Override
    public Optional<Users> findByDni(String dni) {
        return uR.findByDniUser(dni);
    }

    @Override
    public Users insert(Users user, List<String> roles) {
        // Primero se guarda el usuario, luego sus roles (la FK está en roles)
        Users guardado = uR.save(user);
        guardarRoles(guardado, roles);
        return guardado;
    }

    @Override
    public Users update(Users user) {
        return uR.save(user);
    }

    @Override
    public void replaceRoles(Users user, List<String> roles) {
        // Se eliminan las asignaciones actuales y se registran las nuevas
        rR.deleteAll(rR.findByUser_IdUser(user.getIdUser()));
        guardarRoles(user, roles);
    }

    @Override
    public List<String> rolesOf(Long idUser) {
        return rR.findByUser_IdUser(idUser)
                .stream()
                .map(Role::getNameRole)
                .toList();
    }

    private void guardarRoles(Users user, List<String> roles) {
        for (String nombreRol : roles) {
            Role role = new Role();
            role.setNameRole(nombreRol);
            role.setDescriptionRole(ROLES_VALIDOS.get(nombreRol));
            role.setUser(user);
            rR.save(role);
        }
    }
}
