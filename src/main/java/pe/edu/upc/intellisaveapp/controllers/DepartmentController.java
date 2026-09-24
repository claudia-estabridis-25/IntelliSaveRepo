package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.BranchDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.BranchDTOList;
import pe.edu.upc.intellisaveapp.dtos.DepartmentDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

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
        //Validar que la sede asociada al departamento sí exista
        Branch branch = bS.listById(dto.getIdBranch())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la sede con el id: " + dto.getIdBranch()
                        )
                );
        Department d = modelMapper.map(dto, Department.class);
        d.setBranch(branch);
        dS.insert(d); //registrando el departamento de empresa

        DepartmentDTO responseDTO = modelMapper.map(d, DepartmentDTO.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(d.getIdDepartment())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    //Listar todos
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> list() {
        List<DepartmentDTO> lista = dS.list()
                .stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Actualizar
    @PutMapping
    public ResponseEntity<DepartmentDTO> update(@Valid @RequestBody DepartmentDTO dto) {
        //Primero se verifica que exista el departamento a actualizar
        Optional<Department> existente = dS.listById(dto.getIdDepartment());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una departamento con el id: " + dto.getIdDepartment()
            );
        }

        //Verificando que la sede asociada también exista
        Optional<Branch> branch= bS.listById(dto.getIdBranch());

        if (branch.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una sede con el id: " + dto.getIdBranch()
            );
        }

        //Obteniendo el departamento existente que se quiere actualizar
        Department department = existente.get();

        //Actualizando sus campos
        department.setNameBoss(dto.getNameBoss());
        department.setNameDepartment(dto.getNameDepartment());
        department.setDescriptionDepartment(dto.getDescriptionDepartment());

        //Asignando la sede relacionada existente
        department.setBranch(branch.get());

        //Guardando el deparamento actualizado
        dS.update(department);

        //Convirtiendo de nuevo a tipo DTO
        DepartmentDTO responseDTO = modelMapper.map(department, DepartmentDTO.class);

        return ResponseEntity.ok(responseDTO);
    }

    //Listar por id
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> listById(@PathVariable Long id) {

        Department dep = dS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un departamento con el id: " + id
                        )
                );

        DepartmentDTO dto = modelMapper.map(dep, DepartmentDTO.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        //Encontrando el id solicitado y validando que exista
        Department dep = dS.listById(id)
                .orElseThrow(() -> //Por si no lo encuentra
                        new ResourceNotFoundException(
                                "No existe un departamento el id: " + id
                        )
                );

        //Si sí lo encontró, recién lo elimina
        dS.delete(dep.getIdDepartment());

        //No devuelve ningún cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }
}
