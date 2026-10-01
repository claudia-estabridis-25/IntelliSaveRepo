package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.BranchDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.BranchDTOList;
import pe.edu.upc.intellisaveapp.dtos.BranchStructureDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICompanyService;

import java.net.URI;
import java.util.List;

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

    //Listar todas las sedes
    @GetMapping
    public ResponseEntity<List<BranchDTOList>> listar() {
        List<BranchDTOList> lista = bS.list()
                .stream()
                .map(branch -> modelMapper.map(branch, BranchDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Listar las sedes de una empresa (HU032)
    @GetMapping("/company/{idCompany}")
    public ResponseEntity<List<BranchDTOList>> listarPorEmpresa(@PathVariable Long idCompany) {
        cS.listById(idCompany)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la empresa con el id: " + idCompany
                        )
                );

        List<BranchDTOList> lista = bS.listByCompany(idCompany)
                .stream()
                .map(branch -> modelMapper.map(branch, BranchDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Registrar
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchDTOInsert> registrar(@Valid @RequestBody BranchDTOInsert dto) {
        validarCoordenadas(dto);

        //Validar que la empresa asociada a la sede sí exista
        Company company = cS.listById(dto.getIdCompany())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la empresa con el id: " + dto.getIdCompany()
                        )
                );

        Branch b = modelMapper.map(dto, Branch.class);
        b.setIdBranch(null); //Un POST siempre crea una sede nueva
        b.setCompany(company);
        bS.insert(b);

        BranchDTOInsert responseDTO = modelMapper.map(b, BranchDTOInsert.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(b.getIdBranch())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    //Actualizar (HU033)
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchDTOInsert> actualizar(@Valid @RequestBody BranchDTOInsert dto) {
        if (dto.getIdBranch() == null) {
            throw new BusinessRuleException("El id de la sede es obligatorio para actualizar");
        }
        validarCoordenadas(dto);

        Branch branch = bS.listById(dto.getIdBranch())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + dto.getIdBranch()
                        )
                );

        Company company = cS.listById(dto.getIdCompany())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una empresa con el id: " + dto.getIdCompany()
                        )
                );

        branch.setNameBranch(dto.getNameBranch());
        branch.setAddressBranch(dto.getAddressBranch());
        branch.setDescriptionBranch(dto.getDescriptionBranch());
        branch.setLatitudeBranch(dto.getLatitudeBranch());
        branch.setLongitudeBranch(dto.getLongitudeBranch());
        branch.setCompany(company);

        bS.update(branch);

        BranchDTOInsert responseDTO = modelMapper.map(branch, BranchDTOInsert.class);

        return ResponseEntity.ok(responseDTO);
    }

    //Listar sede por id (HU058)
    @GetMapping("/{id}")
    public ResponseEntity<BranchDTOInsert> buscarPorId(@PathVariable Long id) {
        Branch branch = bS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + id
                        )
                );

        BranchDTOInsert dto = modelMapper.map(branch, BranchDTOInsert.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar por id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Branch branch = bS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + id
                        )
                );

        bS.delete(branch.getIdBranch());

        return ResponseEntity.noContent().build();
    }

    //Consulta con JOIN 2: Listar cantidad de áreas y de equipos por sede
    @GetMapping("/structure")
    public ResponseEntity<List<BranchStructureDTO>> estructuraPorSede() {
        //Convirtiendo
        //Ejemplo de arreglo devuelto: [1, "Sede Lima", 9, 86]
        List<BranchStructureDTO> lista = bS.structureByBranch()
                .stream()
                .map(fila -> new BranchStructureDTO(
                        ((Number) fila[0]).longValue(), //idBranch
                        (String) fila[1], //nameBranch
                        ((Number) fila[2]).longValue(), //totalDepartments
                        ((Number) fila[3]).longValue() //totalEquipments
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }


    //Regla de negocio: (0, 0) no es una ubicación válida para una sede
    private void validarCoordenadas(BranchDTOInsert dto) {
        if (dto.getLatitudeBranch() == 0 && dto.getLongitudeBranch() == 0) {
            throw new BusinessRuleException(
                    "Las coordenadas (0, 0) no son válidas: indique la latitud y la longitud reales de la sede"
            );
        }
    }
}