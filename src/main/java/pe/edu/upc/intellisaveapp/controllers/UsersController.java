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
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesimplements.UsersServiceImplement;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IUsersService;
import pe.edu.upc.intellisaveapp.dtos.DepartmentUserCountDTO;
import pe.edu.upc.intellisaveapp.dtos.RoleUserCountDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private final IUsersService uS;
    private final IDepartmentService dS;
    private final PasswordEncoder passwordEncoder;
    //Para filtrar y validar la empresa del usuario autenticado
    private final CompanyAccess acceso;

    public UsersController(IUsersService uS, IDepartmentService dS, PasswordEncoder passwordEncoder, CompanyAccess acceso) {
        this.uS = uS;
        this.dS = dS;
        this.passwordEncoder = passwordEncoder;
        this.acceso = acceso;
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

    // HU nueva: Registrar el administrador de una empresa
    @PostMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> registerCompanyAdmin(@Valid @RequestBody UsersDTOInsert dto) {
        //Solo el admin de la plataforma (sin área ni empresa) puede crear administradores de empresa
        acceso.validarAdminDePlataforma();

        //El área es obligatoria (@NotNull en el DTO), porque de ella sale la empresa que el nuevo admin va a gestionar
        return registrarConRol(dto, "ROLE_ADMIN");
    }

    // HU25: listar usuarios (filtros opcionales por área y rol)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsersDTOList>> listar(
            @RequestParam(required = false) Long idDepartment,
            @RequestParam(required = false) String role) {
        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        List<UsersDTOList> lista = uS.list()
                .stream()
                .map(this::toDTO)
                //Filtrando por empresa: El admin de la plataforma ve a todos y el admin de empresa solo a los de su empresa
                .filter(u -> idCompany == null || idCompany.equals(u.getIdCompany()))
                .filter(u -> idDepartment == null || idDepartment.equals(u.getIdDepartment()))
                .filter(u -> role == null || u.getRoles().contains(role.toUpperCase()))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU052: detalle de un usuario
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> listarPorId(@PathVariable Long id) {
        //Busca el usuario y valida que sea de la empresa del admin que hace la consulta
        return ResponseEntity.ok(toDTO(buscarUsuario(id)));
    }

    // Consulta con JOIN 1: cantidad de usuarios por rol
    @GetMapping("/count-by-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleUserCountDTO>> usuariosPorRol() {

        Long idCompany = acceso.idEmpresaUsuario();

        //El admin de la plataforma usa la consulta nativa, que cuenta los usuarios de todas las empresas
        if (idCompany == null) {
            return ResponseEntity.ok(uS.countByRole());
        }

        //El admin de empresa solo cuenta los usuarios de su empresa
        return ResponseEntity.ok(countRolesByCompany(idCompany));
    }

    // Consulta con JOIN 2: cantidad de usuarios por cada área de cada sede
    @GetMapping("/by-department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentUserCountDTO>> usuariosPorArea() {
        Long idCompany = acceso.idEmpresaUsuario();

        //El admin de la plataforma ve el conteo de las áreas de todas las empresas
        if (idCompany == null) {
            return ResponseEntity.ok(uS.countByDepartment());
        }

        //Ids de las áreas que pertenecen a la empresa del admin
        Set<Long> departmentIds = dS.list()
                .stream()
                .filter(d -> d.getBranch().getCompany().getIdCompany().equals(idCompany))
                .map(Department::getIdDepartment)
                .collect(Collectors.toSet());

        //De la consulta nativa, se dejan solo las filas de las áreas de su empresa
        List<DepartmentUserCountDTO> lista = uS.countByDepartment()
                .stream()
                .filter(fila -> departmentIds.contains(fila.getIdDepartment()))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU026: actualizar usuario (datos, área, contraseña y roles)
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> actualizar(@Valid @RequestBody UsersDTOInsert dto) {
        if (dto.getIdUser() == null) {
            throw new BusinessRuleException("El id del usuario es obligatorio para actualizar");
        }

        //Busca el usuario y valida que sea de la empresa del admin
        Users user = buscarUsuario(dto.getIdUser());
        validarDatosUnicos(dto, user.getIdUser());
        //Busca la nueva área y valida que también sea de la empresa del admin
        Department department = buscarArea(dto.getIdDepartment());

        //Los roles se validan ANTES de guardar cualquier dato, para no dejar el usuario a medio actualizar
        if (dto.getRoles() != null) {
            validarRoles(dto.getRoles());
            validateAdminRoleChange(user, dto.getRoles());
        }

        copiarDatos(dto, user, department);

        // La contraseña solo se cambia si se envía una nueva
        if (dto.getPasswordUser() != null && !dto.getPasswordUser().isBlank()) {
            user.setPasswordUser(passwordEncoder.encode(dto.getPasswordUser()));
        }

        uS.update(user);

        // Los roles solo se cambian si se envían (ya se validaron arriba)
        if (dto.getRoles() != null) {
            uS.replaceRoles(user, dto.getRoles().stream().map(String::toUpperCase).distinct().toList());
        }

        return ResponseEntity.ok(toDTO(user));
    }

    // HU027: desactivar usuario (no se elimina, se cambia su estado)
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTOList> desactivar(@PathVariable Long id) {
        //Busca el usuario y valida que sea de la empresa del admin
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
        //Busca el área y valida que sea de la empresa del admin (no se pueden crear usuarios en otra empresa)
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

    //Los 2 métodos de buscar validan además que el registro sea de la empresa del admin.
    //Si no existe devuelven 404, y si es de otra empresa devuelven 403.

    private Users buscarUsuario(Long id) {
        Users user = uS.listById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con el id: " + id));

        //Un usuario sin área (admin de la plataforma) no tiene empresa: solo el admin de la plataforma puede verlo o editarlo
        acceso.validarEmpresa(companyOf(user));

        return user;
    }

    private Department buscarArea(Long idDepartment) {
        Department department = dS.listById(idDepartment)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un área con el id: " + idDepartment));

        acceso.validarEmpresa(department.getBranch().getCompany().getIdCompany());

        return department;
    }

    //Obtiene el id de la empresa del usuario siguiendo el camino Usuario -> Área -> Sede -> Empresa.
    //Devuelve null si el usuario no tiene área (admin de la plataforma).
    private Long companyOf(Users user) {
        if (user.getDepartment() == null) {
            return null;
        }
        return user.getDepartment().getBranch().getCompany().getIdCompany();
    }

    //Solo el admin de la plataforma puede dar o quitar el rol ADMIN. Se revisa si los roles nuevos
    //incluyen ADMIN o si el usuario ya era ADMIN (en ese caso se le estarían cambiando sus roles).
    private void validateAdminRoleChange(Users user, List<String> newRoles) {
        boolean givesAdmin = newRoles.stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_ADMIN"));
        boolean wasAdmin = uS.rolesOf(user.getIdUser()).contains("ROLE_ADMIN");

        if (givesAdmin || wasAdmin) {
            acceso.validarAdminDePlataforma();
        }
    }

    //Cuenta los usuarios de una empresa por rol (total, activos e inactivos),
    //ordenados de mayor a menor cantidad, igual que la consulta nativa
    private List<RoleUserCountDTO> countRolesByCompany(Long idCompany) {
        Map<String, List<Users>> usersByRole = new HashMap<>();

        for (Users user : uS.list()) {
            if (!idCompany.equals(companyOf(user))) {
                continue;
            }
            for (String role : uS.rolesOf(user.getIdUser())) {
                usersByRole.computeIfAbsent(role, k -> new ArrayList<>()).add(user);
            }
        }

        return usersByRole.entrySet()
                .stream()
                .map(entry -> {
                    long active = entry.getValue().stream().filter(u -> Boolean.TRUE.equals(u.getStatusUser())).count();
                    long total = entry.getValue().size();
                    return new RoleUserCountDTO(entry.getKey(), total, active, total - active);
                })
                .sorted(Comparator.comparing(RoleUserCountDTO::getTotalUsers).reversed()
                        .thenComparing(RoleUserCountDTO::getNameRole))
                .toList();
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
            Branch sede = user.getDepartment().getBranch();
            dto.setIdBranch(sede.getIdBranch());
            dto.setNameBranch(sede.getNameBranch());
            dto.setIdCompany(sede.getCompany().getIdCompany());
            dto.setNameCompany(sede.getCompany().getComercialNamecompany());
        }
        dto.setRoles(uS.rolesOf(user.getIdUser()));
        return dto;
    }

}