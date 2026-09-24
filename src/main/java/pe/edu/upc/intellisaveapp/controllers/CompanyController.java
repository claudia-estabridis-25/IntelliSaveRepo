package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.CompanyDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.CompanyDTOList;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICompanyService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final ICompanyService cS;
    private final ModelMapper mP;

    public CompanyController(ICompanyService cS, ModelMapper mP) {
        this.cS = cS;
        this.mP = mP;
    }

    //Listar empresas
    @GetMapping
    public ResponseEntity<List<CompanyDTOList>> listar(){
        List<CompanyDTOList> lista = cS.list()
                .stream()
                .map(company -> mP.map(company, CompanyDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Registrar empresa
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTOInsert> registrar(@Valid @RequestBody CompanyDTOInsert dto) {
        Company company = mP.map(dto, Company.class);

        cS.insert(company);

        CompanyDTOInsert responseDTO = mP.map(company, CompanyDTOInsert.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(company.getIdCompany())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    //Actualizar empresa
    @PutMapping
    public ResponseEntity<CompanyDTOInsert> actualizar(
            @Valid @RequestBody CompanyDTOInsert dto) {

        Company existente = cS.listById(dto.getIdCompany())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una empresa con el id: " + dto.getIdCompany()

                        )
                );

        Company company = mP.map(dto, Company.class);

        company.setIdCompany(existente.getIdCompany());

        cS.update(company);

        CompanyDTOInsert responseDTO = mP.map(company, CompanyDTOInsert.class);

        return ResponseEntity.ok(responseDTO);
    }

    //Listar empresa por su ID
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTOList> listarPorId(@PathVariable Long id) {
        Company company = cS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una empresa con el id: " + id
                        )
                );

        //Optional<Company> company = cS.listById(id);

        CompanyDTOList dto = mP.map(company, CompanyDTOList.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar empresa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Company company = cS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una empresa con el id: " + id
                        )
                );
        cS.delete(company.getIdCompany());
        return ResponseEntity.noContent().build();
    }

}
