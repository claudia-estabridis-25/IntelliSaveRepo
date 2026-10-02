package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.CompanyDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.CompanyDTOList;
import pe.edu.upc.intellisaveapp.dtos.CompanyStructureDTO;
import pe.edu.upc.intellisaveapp.dtos.SectorCountDTO;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
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
    @GetMapping //Cualquier usuario autenticado
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
        company.setIdCompany(null); //Un POST siempre crea una empresa nueva

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTOInsert> actualizar(
            @Valid @RequestBody CompanyDTOInsert dto) {
        if (dto.getIdCompany() == null) {
            throw new pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException(
                    "El id de la empresa es obligatorio para actualizar"
            );
        }

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
    @GetMapping("/{id}") //Cualquier usuario autenticado
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
    @PreAuthorize("hasRole('ADMIN')")
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

    //Consulta simple 1: Listar cantidad de empresas por sector
    @GetMapping("/count-by-sector")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<SectorCountDTO>> listarEmpresasPorSector() {

        // Convirtiendo la lista devuelta, que es de tipo List<Object[]>, osea devuelve una lista de arreglos,
        // a tipo lista DTO (List<SectorCountDTO>)
        // Esto es lo que devuelve la lista del metodo service:
        // fila = ["Manufactura", 2]     // [0] = sector_empresa, [1] = total_empresas
        List<SectorCountDTO> lista = cS.countCompaniesBySector()
                .stream()
                .map(fila -> new SectorCountDTO(
                        (String) fila[0], //sector_empresa
                        ((Number) fila[1]).longValue() //total_empresas
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Consulta simple 2: Buscar empresas por RUC
    @GetMapping("/ruc/{ruc}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CompanyDTOList>> buscarPorRuc(@PathVariable String ruc) {

        //Validando el ruc
        if (!ruc.matches("^[0-9]{11}$")) {
            throw new BusinessRuleException("El RUC debe tener exactamente 11 dígitos numéricos");
        }

        //Convirtiendo la lista que devuelve el metodo del service (de tipo List<Company>) a tipo DTO
        List<CompanyDTOList> lista = cS.findByRucCompany(ruc)
                .stream()
                .map(company -> mP.map(company, CompanyDTOList.class))
                .toList();

        //Por si no encuentra el ruc buscado
        if (lista.isEmpty()) {
            throw new ResourceNotFoundException("No existe una empresa con el RUC: " + ruc);
        }

        return ResponseEntity.ok(lista);
    }

    //Consulta con JOIN 1: Listar cantidad de sedes y áreas por empresa
    @GetMapping("/structure")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CompanyStructureDTO>> estructuraPorEmpresa() {

        //Convirtiendo de tipo List<Object[]> a tipo List<CompanyStructureDTO>
        //En este caso, hay 4 valores en el array que devuelve Object[], los cuales deben
        //convertirse y emparejarse con los 4 campos del DTO.
        //Ejm: fila = [1, "MiEmpresa SAC", 5, 16]
        List<CompanyStructureDTO> lista = cS.structureByCompany()
                .stream()
                .map(fila -> new CompanyStructureDTO(
                        ((Number) fila[0]).longValue(), //idCompany
                        (String) fila[1], //formalNameCompany
                        ((Number) fila[2]).longValue(), //totalBranches
                        ((Number) fila[3]).longValue() //totalDepartments
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }

}
