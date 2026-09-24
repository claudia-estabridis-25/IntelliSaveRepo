package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.BranchDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.BranchDTOList;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICompanyService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final IBranchService bS;
    private final ICompanyService cS;
    private final ModelMapper modelMapper;

    public BranchController(IBranchService bS, ICompanyService cS, ModelMapper modelMapper) {
        this.bS = bS;
        this.cS = cS;
        this.modelMapper = modelMapper;
    }

    //Listar todos
    @GetMapping
    public ResponseEntity<List<BranchDTOList>> listar() {
        List<BranchDTOList> lista = bS.list()
                .stream()
                .map(branch -> modelMapper.map(branch, BranchDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Registrar
    @PostMapping
    public ResponseEntity<BranchDTOInsert> registrar(@Valid @RequestBody BranchDTOInsert dto) {
        //Validar que la empresa asociada a la sede sí exista
        Company company = cS.listById(dto.getIdCompany())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la empresa con el id: " + dto.getIdCompany()
                        )
                );
        Branch b = modelMapper.map(dto, Branch.class);
        b.setCompany(company);
        bS.insert(b); //registrando la sede

        BranchDTOInsert responseDTO = modelMapper.map(b, BranchDTOInsert.class);

        // El objeto location permite que, al crear una sede, el cliente pueda saber dónde se
        // guardó o dónde quedó la nueva sede. Por ejemplo, la respuesta sería:
        // HTTP/1.1 201 Created
        // Location: /sucursales/15   -> 15 es el id con que se crea la sede
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(b.getIdBranch())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    //Actualizar
    @PutMapping
    public ResponseEntity<BranchDTOInsert> actualizar(@Valid @RequestBody BranchDTOInsert dto) {
        //Primero se verifica que exista la sede a actualizar
        Optional<Branch> existente =bS.listById(dto.getIdBranch());

        if (existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una sede con el id: " + dto.getIdBranch()
            );
        }

        //Verificando que la empresa asociada también exista
        Optional<Company> company= cS.listById(dto.getIdCompany());

        if (company.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una empresa con el id: " + dto.getIdCompany()
            );
        }

        //Obteniendo la sede existente que se quiere actualizar
        Branch branch = existente.get();

        //Actualizando sus campos
        branch.setNameBranch(dto.getNameBranch());
        branch.setAddressBranch(dto.getAddressBranch());
        branch.setDescriptionBranch(dto.getDescriptionBranch());

        //Asignando a la empresa relacionada existente
        branch.setCompany(company.get());

        //Guardando la sede actualizada
        bS.update(branch);

        //Convirtiendo de nuevo a tipo DTO
        BranchDTOInsert responseDTO = modelMapper.map(branch, BranchDTOInsert.class);

        return ResponseEntity.ok(responseDTO);
    }

    //Listar por id
    @GetMapping("/{id}")
    public ResponseEntity<BranchDTOList> buscarPorId(@PathVariable Long id) {

        Branch branch = bS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + id
                        )
                );

        BranchDTOList dto = modelMapper.map(branch, BranchDTOList.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        //Encontrando el id solicitado y validando que exista
        Branch branch = bS.listById(id)
                .orElseThrow(() -> //Por si no lo encuentra
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + id
                        )
                );

        //Si sí lo encontró, recién lo elimina
        bS.delete(branch.getIdBranch());

        //No devuelve ningún cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }
}
