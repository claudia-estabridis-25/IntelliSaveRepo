package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.DepartmentDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
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

    public DepartmentController(IDepartmentService dS, IBranchService bS, ModelMapper modelMapper) {
        this.dS = dS;
        this.bS = bS;
        this.modelMapper = modelMapper;
    }

    //Registrar
    @PostMapping
    public ResponseEntity<DepartmentDTO> register(@Valid @RequestBody DepartmentDTO dto) {
        Branch branch = bS.listById(dto.getIdBranch())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la sede con el id: " + dto.getIdBranch()
                        )
                );

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
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> list() {
        List<DepartmentDTO> lista = dS.list()
                .stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Listar las áreas de una sede (HU022)
    @GetMapping("/branch/{idBranch}")
    public ResponseEntity<List<DepartmentDTO>> listByBranch(@PathVariable Long idBranch) {
        bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la sede con el id: " + idBranch
                        )
                );

        List<DepartmentDTO> lista = dS.listByBranch(idBranch)
                .stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Actualizar
    @PutMapping
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

        Branch branch = bS.listById(dto.getIdBranch())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + dto.getIdBranch()
                        )
                );

        department.setNameBoss(dto.getNameBoss());
        department.setNameDepartment(dto.getNameDepartment());
        department.setDescriptionDepartment(dto.getDescriptionDepartment());
        department.setBranch(branch);

        dS.update(department);

        DepartmentDTO responseDTO = modelMapper.map(department, DepartmentDTO.class);

        return ResponseEntity.ok(responseDTO);
    }

    //Listar por id
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> listById(@PathVariable Long id) {
        Department dep = dS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + id
                        )
                );

        DepartmentDTO dto = modelMapper.map(dep, DepartmentDTO.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Department dep = dS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + id
                        )
                );

        dS.delete(dep.getIdDepartment());

        return ResponseEntity.noContent().build();
    }
}