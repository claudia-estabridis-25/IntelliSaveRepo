package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.EquipmentDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.EquipmentDTOList;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
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

    public EquipmentController(IEquipmentService eS, IDepartmentService dS, ModelMapper mP) {
        this.eS = eS;
        this.dS = dS;
        this.mP = mP;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentDTOList>> listar() {
        List<EquipmentDTOList> lista = eS.list()
                .stream()
                .map(equipment -> mP.map(equipment, EquipmentDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<EquipmentDTOInsert> registrar(@Valid @RequestBody EquipmentDTOInsert dto) {
        Department department = dS.listById(dto.getIdDepartment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + dto.getIdDepartment()
                        )
                );

        Equipment equipment = mP.map(dto, Equipment.class);
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

    @PutMapping
    public ResponseEntity<EquipmentDTOInsert> actualizar(@Valid @RequestBody EquipmentDTOInsert dto) {
        Equipment existente = eS.listById(dto.getIdEquipment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + dto.getIdEquipment()
                        )
                );

        Department department = dS.listById(dto.getIdDepartment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + dto.getIdDepartment()
                        )
                );

        Equipment equipment = mP.map(dto, Equipment.class);
        equipment.setIdEquipment(existente.getIdEquipment());
        equipment.setDepartment(department);

        eS.update(equipment);

        EquipmentDTOInsert responseDTO = mP.map(equipment, EquipmentDTOInsert.class);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDTOList> listarPorId(@PathVariable Long id) {
        Equipment equipment = eS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + id
                        )
                );

        EquipmentDTOList dto = mP.map(equipment, EquipmentDTOList.class);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Equipment equipment = eS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + id
                        )
                );
        eS.delete(equipment.getIdEquipment());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EquipmentDTOList>> listarPorEstado(@PathVariable String status) {
        List<EquipmentDTOList> lista = eS.listByStatus(status)
                .stream()
                .map(equipment -> mP.map(equipment, EquipmentDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<EquipmentDTOList> darDeBaja(@PathVariable Long id) {
        Equipment equipment = eS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + id
                        )
                );

        equipment.setStatusEquipment("Inactivo");
        eS.update(equipment);

        EquipmentDTOList dto = mP.map(equipment, EquipmentDTOList.class);
        return ResponseEntity.ok(dto);
    }
}