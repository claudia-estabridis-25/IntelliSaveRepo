package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.BranchPowerDTO;
import pe.edu.upc.intellisaveapp.dtos.DepartmentDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final IDepartmentService dS;
    private final IBranchService bS;
    private final ModelMapper modelMapper;
    private final CompanyAccess acceso;

    public DepartmentController(IDepartmentService dS, IBranchService bS, ModelMapper modelMapper, CompanyAccess acceso) {
        this.dS = dS;
        this.bS = bS;
        this.modelMapper = modelMapper;
        this.acceso = acceso;
    }

    //Registrar
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> register(@Valid @RequestBody DepartmentDTO dto) {
        Branch branch = bS.listById(dto.getIdBranch())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la sede con el id: " + dto.getIdBranch()
                        )
                );

        //Validando que el id de empresa asociado al usuario que hace la consulta, sea igual al id de la empresa
        //asociado al área que se registra
        acceso.validarEmpresa(branch.getCompany().getIdCompany());

        Department d = modelMapper.map(dto, Department.class);
        d.setIdDepartment(null); //Un POST siempre crea un área nueva
        d.setBranch(branch);
        dS.insert(d);

        DepartmentDTO responseDTO = modelMapper.map(d, DepartmentDTO.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(d.getIdDepartment())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    //Listar todas las áreas
    @GetMapping //Cualquier usuario autenticado
    public ResponseEntity<List<DepartmentDTO>> list() {
        Long idEmpresa = acceso.idEmpresaUsuario();

        List<DepartmentDTO> lista = dS.list()
                .stream()
                .filter(department -> idEmpresa == null ||
                        department.getBranch().getCompany().getIdCompany().equals(idEmpresa))
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Listar las áreas de una sede (HU022)
    @GetMapping("/branch/{idBranch}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR', 'EMPLOYEE')")
    public ResponseEntity<List<DepartmentDTO>> listByBranch(@PathVariable Long idBranch) {
        Branch sede = bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la sede con el id: " + idBranch
                        )
                );

        acceso.validarEmpresa(sede.getCompany().getIdCompany());

        List<DepartmentDTO> lista = dS.listByBranch(idBranch)
                .stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .toList();

        // HU022 CA03: la sede no tiene áreas registradas
        if (lista.isEmpty()) {
            throw new ResourceNotFoundException("Esta sede no tiene áreas registradas");
        }

        return ResponseEntity.ok(lista);
    }

    //Actualizar
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> update(@Valid @RequestBody DepartmentDTO dto) {

        if (dto.getIdDepartment() == null) {
            throw new BusinessRuleException("El id del área es obligatorio para actualizar");
        }

        Department department = dS.listById(dto.getIdDepartment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + dto.getIdDepartment()
                        )
                );

        acceso.validarEmpresa(department.getBranch().getCompany().getIdCompany());

        Branch branch = bS.listById(dto.getIdBranch())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + dto.getIdBranch()
                        )
                );

        acceso.validarEmpresa(branch.getCompany().getIdCompany());

        department.setNameBoss(dto.getNameBoss());
        department.setNameDepartment(dto.getNameDepartment());
        department.setDescriptionDepartment(dto.getDescriptionDepartment());
        department.setBranch(branch);

        dS.update(department);

        DepartmentDTO responseDTO = modelMapper.map(department, DepartmentDTO.class);

        return ResponseEntity.ok(responseDTO);
    }

    //Listar por id
    @GetMapping("/{id}") //Cualquier usuario autenticado
    public ResponseEntity<DepartmentDTO> listById(@PathVariable Long id) {
        Department dep = dS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + id
                        )
                );

        acceso.validarEmpresa(dep.getBranch().getCompany().getIdCompany());

        DepartmentDTO dto = modelMapper.map(dep, DepartmentDTO.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar por id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Department dep = dS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + id
                        )
                );

        acceso.validarEmpresa(dep.getBranch().getCompany().getIdCompany());

        dS.delete(dep.getIdDepartment());

        return ResponseEntity.noContent().build();
    }

    // Consulta simple 4: Buscar áreas por su nombre
    @GetMapping("/search-by-name")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<List<DepartmentDTO>> buscarPorNombre(@RequestParam String name) {

        if (name.isBlank()) {
            throw new BusinessRuleException("Indique el nombre del área a buscar");
        }

        Long idEmpresa = acceso.idEmpresaUsuario();

        //Convirtiendo de tipo entidad (Department) a tipo DTO (DepartmentDTO)
        List<DepartmentDTO> lista = dS.searchByName(name.trim())
                .stream()
                .filter(department -> idEmpresa == null ||
                        department.getBranch().getCompany().getIdCompany().equals(idEmpresa))
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

}