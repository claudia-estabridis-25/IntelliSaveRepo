package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.dtos.DepartmentUserCountDTO;
import pe.edu.upc.intellisaveapp.dtos.RoleUserCountDTO;

import java.util.List;
import java.util.Optional;

public interface IUsersService {
    public List<Users> list();
    public Optional<Users> listById(Long id);
    public Optional<Users> findByEmail(String email);
    public Optional<Users> findByDni(String dni);
    public Users insert(Users user, List<String> roles);
    public Users update(Users user);
    public void replaceRoles(Users user, List<String> roles);
    public List<String> rolesOf(Long idUser);
    public List<RoleUserCountDTO> countByRole();
    public List<DepartmentUserCountDTO> countByDepartment();
}
