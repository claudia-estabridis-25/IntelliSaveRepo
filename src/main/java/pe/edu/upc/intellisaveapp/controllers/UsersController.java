package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.ProfileUpdateDTO;
import pe.edu.upc.intellisaveapp.dtos.UsersDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.UsersDTOList;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesimplements.UsersServiceImplement;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IUsersService;
import pe.edu.upc.intellisaveapp.dtos.DepartmentUserCountDTO;
import pe.edu.upc.intellisaveapp.dtos.RoleUserCountDTO;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private final IUsersService uS;
    private final IDepartmentService dS;
    private final PasswordEncoder passwordEncoder;

    public UsersController(IUsersService uS, IDepartmentService dS, PasswordEncoder passwordEncoder) {
        this.uS = uS;
        this.dS = dS;
        this.passwordEncoder = passwordEncoder;
    }

    // HU04: registrar usuario supervisor energético
    @PostMapping("/supervisors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> registrarSupervisor(@Valid @RequestBody UsersDTOInsert dto) {
        return registrarConRol(dto, "ROLE_SUPERVISOR");
    }

    // HU05: registrar usuario empleado
    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> registrarEmpleado(@Valid @RequestBody UsersDTOInsert dto) {
        return registrarConRol(dto, "ROLE_EMPLOYEE");
    }

    // HU25: listar usuarios (filtros opcionales por área y rol)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsersDTOList>> listar(
            @RequestParam(required = false) Long idDepartment,
            @RequestParam(required = false) String role) {
        List<UsersDTOList> lista = uS.list()
                .stream()
                .map(this::toDTO)
                .filter(u -> idDepartment == null || idDepartment.equals(u.getIdDepartment()))
                .filter(u -> role == null || u.getRoles().contains(role.toUpperCase()))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU052: detalle de un usuario
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> listarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(buscarUsuario(id)));
    }

    // Consulta con JOIN 1: cantidad de usuarios por rol
    @GetMapping("/count-by-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleUserCountDTO>> usuariosPorRol() {
        return ResponseEntity.ok(uS.countByRole());
    }

    // Consulta con JOIN 2: cantidad de usuarios por cada área de cada sede
    @GetMapping("/by-department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentUserCountDTO>> usuariosPorArea() {
        return ResponseEntity.ok(uS.countByDepartment());
    }

    // HU026: actualizar usuario (datos, área, contraseña y roles)
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> actualizar(@Valid @RequestBody UsersDTOInsert dto) {
        if (dto.getIdUser() == null) {
            throw new BusinessRuleException("El id del usuario es obligatorio para actualizar");
        }

        Users user = buscarUsuario(dto.getIdUser());
        validarDatosUnicos(dto, user.getIdUser());
        Department department = buscarArea(dto.getIdDepartment());

        copiarDatos(dto, user, department);

        // La contraseña solo se cambia si se envía una nueva
        if (dto.getPasswordUser() != null && !dto.getPasswordUser().isBlank()) {
            user.setPasswordUser(passwordEncoder.encode(dto.getPasswordUser()));
        }

        uS.update(user);

        // Los roles solo se cambian si se envían
        if (dto.getRoles() != null) {
            validarRoles(dto.getRoles());
            uS.replaceRoles(user, dto.getRoles().stream().map(String::toUpperCase).distinct().toList());
        }

        return ResponseEntity.ok(toDTO(user));
    }

    // HU027: desactivar usuario (no se elimina, se cambia su estado)
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> desactivar(@PathVariable Long id) {
        Users user = buscarUsuario(id);

        if (user.getEmailUser().equals(correoUsuarioActual())) {
            throw new BusinessRuleException("No puede desactivar su propio usuario");
        }

        user.setStatusUser(false);
        uS.update(user);

        return ResponseEntity.ok(toDTO(user));
    }

    // HU041: ver mi perfil
    @GetMapping("/me")
    public ResponseEntity<UsersDTOList> miPerfil() {
        return ResponseEntity.ok(toDTO(usuarioActual()));
    }

    // HU041: editar mi perfil
    @PutMapping("/me")
    public ResponseEntity<UsersDTOList> editarMiPerfil(@Valid @RequestBody ProfileUpdateDTO dto) {
        Users user = usuarioActual();

        if (dto.getTelephoneUser() != null) {
            user.setTelephoneUser(dto.getTelephoneUser());
        }
        if (dto.getSecondName() != null) {
            user.setSecondName(dto.getSecondName());
        }
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPasswordUser(passwordEncoder.encode(dto.getNewPassword()));
        }

        uS.update(user);

        return ResponseEntity.ok(toDTO(user));
    }

    // ===================== MÉTODOS DE APOYO =====================

    private ResponseEntity<UsersDTOList> registrarConRol(UsersDTOInsert dto, String rol) {
        if (dto.getPasswordUser() == null || dto.getPasswordUser().isBlank()) {
            throw new BusinessRuleException("La contraseña es obligatoria para registrar un usuario");
        }

        validarDatosUnicos(dto, null);
        Department department = buscarArea(dto.getIdDepartment());

        Users user = new Users();
        copiarDatos(dto, user, department);
        user.setPasswordUser(passwordEncoder.encode(dto.getPasswordUser()));
        user.setStatusUser(true);

        Users guardado = uS.insert(user, List.of(rol));

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/users/{id}")
                .buildAndExpand(guardado.getIdUser())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(guardado));
    }

    private void copiarDatos(UsersDTOInsert dto, Users user, Department department) {
        user.setEmailUser(dto.getEmailUser().toLowerCase());
        user.setDniUser(dto.getDniUser());
        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        user.setPaternalSurname(dto.getPaternalSurname());
        user.setMaternalSurname(dto.getMaternalSurname());
        user.setPositionUser(dto.getPositionUser());
        user.setTelephoneUser(dto.getTelephoneUser());
        user.setDepartment(department);
    }

    // El correo y el DNI no se pueden repetir entre usuarios
    private void validarDatosUnicos(UsersDTOInsert dto, Long idUsuarioActual) {
        uS.findByEmail(dto.getEmailUser().toLowerCase()).ifPresent(u -> {
            if (!u.getIdUser().equals(idUsuarioActual)) {
                throw new BusinessRuleException("Ya existe un usuario con el correo: " + dto.getEmailUser());
            }
        });

        uS.findByDni(dto.getDniUser()).ifPresent(u -> {
            if (!u.getIdUser().equals(idUsuarioActual)) {
                throw new BusinessRuleException("Ya existe un usuario con el DNI: " + dto.getDniUser());
            }
        });
    }

    private void validarRoles(List<String> roles) {
        if (roles.isEmpty()) {
            throw new BusinessRuleException("El usuario debe tener al menos un rol");
        }
        for (String rol : roles) {
            if (!UsersServiceImplement.ROLES_VALIDOS.containsKey(rol.toUpperCase())) {
                throw new BusinessRuleException(
                        "Rol no válido: " + rol + ". Los roles permitidos son ROLE_ADMIN, ROLE_SUPERVISOR y ROLE_EMPLOYEE"
                );
            }
        }
    }

    private Users buscarUsuario(Long id) {
        return uS.listById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con el id: " + id));
    }

    private Department buscarArea(Long idDepartment) {
        return dS.listById(idDepartment)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un área con el id: " + idDepartment));
    }

    // El correo del usuario logueado viene dentro del token JWT
    private String correoUsuarioActual() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Users usuarioActual() {
        return uS.findByEmail(correoUsuarioActual())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario de la sesión actual"));
    }

    private UsersDTOList toDTO(Users user) {
        UsersDTOList dto = new UsersDTOList();
        dto.setIdUser(user.getIdUser());
        dto.setEmailUser(user.getEmailUser());
        dto.setDniUser(user.getDniUser());
        dto.setFirstName(user.getFirstName());
        dto.setSecondName(user.getSecondName());
        dto.setPaternalSurname(user.getPaternalSurname());
        dto.setMaternalSurname(user.getMaternalSurname());
        dto.setPositionUser(user.getPositionUser());
        dto.setTelephoneUser(user.getTelephoneUser());
        dto.setStatusUser(user.getStatusUser());
        if (user.getDepartment() != null) {
            dto.setIdDepartment(user.getDepartment().getIdDepartment());
            dto.setNameDepartment(user.getDepartment().getNameDepartment());
        }
        dto.setRoles(uS.rolesOf(user.getIdUser()));
        return dto;
    }
}
