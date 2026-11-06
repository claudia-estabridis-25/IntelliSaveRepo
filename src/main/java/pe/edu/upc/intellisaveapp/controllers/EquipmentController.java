package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.EquipmentDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.EquipmentDTOList;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {
    private final IEquipmentService eS;
    private final IDepartmentService dS;
    private final ModelMapper mP;
    private final CompanyAccess acceso; //Para filtrar y validar la empresa del usuario autenticado

    public EquipmentController(IEquipmentService eS, IDepartmentService dS, ModelMapper mP, CompanyAccess acceso) {
        this.eS = eS;
        this.dS = dS;
        this.mP = mP;
        this.acceso = acceso;
    }

    //Listar equipos
    @GetMapping //Cualquier usuario autenticado
    public ResponseEntity<List<EquipmentDTOList>> listar() {
        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        List<EquipmentDTOList> lista = eS.list()
                .stream()
                //Filtrando por empresa: si el usuario es admin devuelve todos los equipos, y si no es admin
                //devuelve solo los equipos de su empresa
                .filter(equipment -> idCompany == null || companyOf(equipment).equals(idCompany))
                .map(equipment -> mP.map(equipment, EquipmentDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Registrar equipo
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<EquipmentDTOInsert> registrar(@Valid @RequestBody EquipmentDTOInsert dto) {
        //Validando que el área del equipo sí exista
        Department department = dS.listById(dto.getIdDepartment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + dto.getIdDepartment()
                        )
                );

        //Validando que el área donde se registra el equipo pertenezca a la empresa del usuario autenticado
        acceso.validarEmpresa(department.getBranch().getCompany().getIdCompany());

        Equipment equipment = mP.map(dto, Equipment.class);
        equipment.setIdEquipment(null); //Un POST siempre crea un equipo nuevo
        equipment.setDepartment(department);
        eS.insert(equipment);

        EquipmentDTOInsert responseDTO = mP.map(equipment, EquipmentDTOInsert.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(equipment.getIdEquipment())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    //Actualizar equipo
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<EquipmentDTOInsert> actualizar(@Valid @RequestBody EquipmentDTOInsert dto) {
        if (dto.getIdEquipment() == null) {
            throw new BusinessRuleException("El id del equipo es obligatorio para actualizar");
        }

        Equipment existente = eS.listById(dto.getIdEquipment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + dto.getIdEquipment()
                        )
                );

        //Validando que el equipo que se quiere actualizar pertenezca a la empresa del usuario autenticado
        acceso.validarEmpresa(companyOf(existente));

        Department department = dS.listById(dto.getIdDepartment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + dto.getIdDepartment()
                        )
                );

        //Impidiendo que el equipo se mueva a un área de otra empresa
        if (!department.getBranch().getCompany().getIdCompany().equals(companyOf(existente))) {
            throw new BusinessRuleException("No se puede mover el equipo a un área de otra empresa");
        }

        Equipment equipment = mP.map(dto, Equipment.class);
        equipment.setIdEquipment(existente.getIdEquipment());
        equipment.setDepartment(department);

        eS.update(equipment);

        EquipmentDTOInsert responseDTO = mP.map(equipment, EquipmentDTOInsert.class);

        return ResponseEntity.ok(responseDTO);
    }

    //Listar equipo por su ID
    @GetMapping("/{id}") //Cualquier usuario autenticado
    public ResponseEntity<EquipmentDTOInsert> listarPorId(@PathVariable Long id) {
        //Busca el equipo y valida que sea de la empresa del usuario autenticado
        Equipment equipment = findEquipment(id);

        EquipmentDTOInsert dto = mP.map(equipment, EquipmentDTOInsert.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar equipo
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        //Busca el equipo y valida que sea de la empresa del usuario autenticado
        Equipment equipment = findEquipment(id);
        eS.delete(equipment.getIdEquipment());
        return ResponseEntity.noContent().build();
    }

    //Listar equipos por estado (Activo o Inactivo)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<List<EquipmentDTOList>> listarPorEstado(@PathVariable String status) {
        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        List<EquipmentDTOList> lista = eS.listByStatus(status)
                .stream()
                //Filtrando por empresa: el admin ve todos y los demás solo los de su empresa
                .filter(equipment -> idCompany == null || companyOf(equipment).equals(idCompany))
                .map(equipment -> mP.map(equipment, EquipmentDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Dar de baja un equipo (cambia su estado a Inactivo)
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<EquipmentDTOList> darDeBaja(@PathVariable Long id) {
        //Busca el equipo y valida que sea de la empresa del usuario autenticado
        Equipment equipment = findEquipment(id);

        equipment.setStatusEquipment("Inactivo");
        eS.update(equipment);

        EquipmentDTOList dto = mP.map(equipment, EquipmentDTOList.class);
        return ResponseEntity.ok(dto);
    }

    //Busca un equipo por su id y valida que pertenezca a la empresa del usuario autenticado.
    //Si no existe devuelve 404, y si es de otra empresa devuelve 403.
    private Equipment findEquipment(Long id) {
        Equipment equipment = eS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + id
                        )
                );

        acceso.validarEmpresa(companyOf(equipment));

        return equipment;
    }

    //Obtiene el id de la empresa a la que pertenece el equipo, siguiendo el camino Equipo -> Área -> Sede -> Empresa
    private Long companyOf(Equipment equipment) {
        return equipment.getDepartment().getBranch().getCompany().getIdCompany();
    }
}