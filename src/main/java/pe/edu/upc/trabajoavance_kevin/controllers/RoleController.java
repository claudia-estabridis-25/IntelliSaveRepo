package pe.edu.upc.trabajoavance_kevin.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.trabajoavance_kevin.dtos.RoleDTO;
import pe.edu.upc.trabajoavance_kevin.entities.Role;
import pe.edu.upc.trabajoavance_kevin.exceptions.ResourceNotFoundException;
import pe.edu.upc.trabajoavance_kevin.servicesinterfaces.IRoleService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final IRoleService rS;
    private final ModelMapper modelMapper;

    public RoleController(IRoleService rS, ModelMapper modelMapper) {
        this.rS = rS;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<RoleDTO>> listar() {

        List<RoleDTO> lista = rS.list()
                .stream()
                .map(role -> modelMapper.map(role, RoleDTO.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleDTO> registrar(
            @Valid @RequestBody RoleDTO dto) {

        Role role = modelMapper.map(dto, Role.class);

        rS.insert(role);

        RoleDTO responseDTO =
                modelMapper.map(role, RoleDTO.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(role.getIdRole())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @PutMapping
    public ResponseEntity<RoleDTO> actualizar(
            @Valid @RequestBody RoleDTO dto) {
        Role existente = rS.listId(dto.getIdRole())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un role con el id: " + dto.getIdRole()
                        )
                );
        Role crop = modelMapper.map(dto, Role.class);
        crop.setIdRole(existente.getIdRole());
        rS.update(crop);
        RoleDTO responseDTO =
                modelMapper.map(crop, RoleDTO.class);
        return ResponseEntity.ok(responseDTO);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Role crop = rS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un role con el id: " + id
                        )
                );
        rS.delete(crop.getIdRole());
        return ResponseEntity.noContent().build();
    }
}
