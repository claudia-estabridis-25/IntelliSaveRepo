package pe.edu.upc.trabajoavance_kevin.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.trabajoavance_kevin.dtos.UserDTO;
import pe.edu.upc.trabajoavance_kevin.entities.Role;
import pe.edu.upc.trabajoavance_kevin.entities.User;
import pe.edu.upc.trabajoavance_kevin.exceptions.ResourceNotFoundException;
import pe.edu.upc.trabajoavance_kevin.servicesinterfaces.IRoleService;
import pe.edu.upc.trabajoavance_kevin.servicesinterfaces.IUserService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final IUserService uS;
    private final IRoleService rS;
    private final ModelMapper modelMapper;

    public UserController(IUserService uS, IRoleService rS, ModelMapper modelMapper) {
        this.uS = uS;
        this.rS = rS;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> listar() {

        List<UserDTO> lista = uS.list()
                .stream()
                .map(use -> modelMapper.map(use, UserDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<UserDTO> registrar(
            @Valid @RequestBody UserDTO dto) {
        Role role = rS.listId(dto.getIdUser())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el usuario con el id: " + dto.getIdUser()
                        )
                );
        User us = modelMapper.map(dto, User.class);
        us.setRole(role);
        uS.insert(us);

        UserDTO responseDTO =
                modelMapper.map(us, UserDTO.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(us.getIdUser())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> buscarPorId(
            @PathVariable Long id) {

        User us = uS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un usuario con el id: " + id
                        )
                );

        UserDTO dto = modelMapper.map(us, UserDTO.class);
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<UserDTO> actualizar(
            @Valid @RequestBody UserDTO dto) {

        // 1. Verificar que la actividad exista
        Optional<User> existente = uS.listId(dto.getIdUser());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una actividad con el id: " + dto.getIdUser()
            );
        }

        // 2. Verificar que el cultivo exista
        Optional<Role> role = rS.listId(dto.getIdRole());

        if (role.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un role con el id: " + dto.getIdRole()
            );
        }

        // 3. Obtener la actividad existente
        User user = existente.get();

        // 4. Actualizar sus campos
        user.setPosition(dto.getPosition());
        user.setDocumentNumber(dto.getDocumentNumber());
        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        user.setPaternalLastName(dto.getPaternalLastName());
        user.setMaternalLastName(dto.getMaternalLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        // 5. Asignar el cultivo existente
        user.setRole(role.get());

        // 6. Guardar
        uS.update(user);

        // 7. Convertir a DTO
        UserDTO responseDTO =
                modelMapper.map(user, UserDTO.class);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        User ac = uS.listId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una actividad con el id: " + id
                        )
                );
        uS.delete(ac.getIdUser());
        return ResponseEntity.noContent().build();
    }

}
