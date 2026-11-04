package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.*;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICompanyService;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final IBranchService bS;
    private final ICompanyService cS;
    private final ModelMapper modelMapper;
    private final CompanyAccess acceso;

    public BranchController(IBranchService bS, ICompanyService cS, ModelMapper modelMapper, CompanyAccess acceso) {
        this.bS = bS;
        this.cS = cS;
        this.modelMapper = modelMapper;
        this.acceso = acceso;
    }

    //Listar todas las sedes
    @GetMapping //Cualquier usuario autenticado
    public ResponseEntity<List<BranchDTOList>> listar() {
        // Trayendo el id de la empresa del usuario autenticado, si es que tiene uno, para validar que solo
        // pueda ver la información de su propia empresa. Si el usuario autenticado es admin, entonces idEmpresa
        // será null y podrá ver todas las sedes.
        Long idEmpresa = acceso.idEmpresaUsuario();

        List<BranchDTOList> lista = bS.list()
                .stream()
                .filter(branch -> idEmpresa == null || branch.getCompany().getIdCompany().equals(idEmpresa))
                .map(branch -> modelMapper.map(branch, BranchDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Listar las sedes de una empresa (HU032)
    @GetMapping("/company/{idCompany}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR', 'EMPLOYEE')")
    public ResponseEntity<List<BranchDTOList>> listarPorEmpresa(@PathVariable Long idCompany) {
        // Validando que el id de empresa del usuario autenticado sea igual al id de empresa que está consultando
        // (el ide de empresa se obtiene por medio de la sede).
        acceso.validarEmpresa(idCompany);

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

        // Validando que el id de empresa del usuario autenticado sea igual al id de empresa asociado a la sede
        // que está registrando, es decir, que pertenezca a la empresa.
        acceso.validarEmpresa(company.getIdCompany());

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

        // Verificando que el id de empresa del usuario autenticado sea
        // igual al id de empresa asociado a la sede que está actualizando.
        acceso.validarEmpresa(branch.getCompany().getIdCompany());

        Company company = cS.listById(dto.getIdCompany())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una empresa con el id: " + dto.getIdCompany()
                        )
                );

        // Impidiendo que el usuario mueva la sede a otra empresa, ya que solo puede actualizar
        // los datos de sedes dentro de su propia empresa.
        acceso.validarEmpresa(company.getIdCompany());

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
    @GetMapping("/{id}") //Cualquier usuario autenticado
    public ResponseEntity<BranchDTOInsert> buscarPorId(@PathVariable Long id) {
        Branch branch = bS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + id
                        )
                );

        acceso.validarEmpresa(branch.getCompany().getIdCompany());

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

        acceso.validarEmpresa(branch.getCompany().getIdCompany());

        bS.delete(branch.getIdBranch());

        return ResponseEntity.noContent().build();
    }

    //Consulta con JOIN 2: Listar cantidad de áreas y de equipos por sede
    @GetMapping("/structure")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<BranchStructureDTO>> estructuraPorSede() {
        Set<Long> sedes = sedesVisibles();

        //Convirtiendo
        //Ejemplo de arreglo devuelto: [1, "Sede Lima", 9, 86]
        List<BranchStructureDTO> lista = bS.structureByBranch()
                .stream()
                .filter(fila -> sedes == null || sedes.contains(((Number) fila[0]).longValue()))
                .map(fila -> new BranchStructureDTO(
                        ((Number) fila[0]).longValue(), //idBranch
                        (String) fila[1], //nameBranch
                        ((Number) fila[2]).longValue(), //totalDepartments
                        ((Number) fila[3]).longValue() //totalEquipments
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Consulta con JOIN 3: equipos activos e inactivos por cada área de cada sede
    @GetMapping("/equipment-status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<DepartmentEquipmentStatusDTO>> equiposPorEstadoDeAreas() {
        Set<Long> sedes = sedesVisibles();

        //Convirtiendo
        List<DepartmentEquipmentStatusDTO> lista = bS.equipmentStatusByDepartment()
                .stream()
                .filter(fila -> sedes == null || sedes.contains(((Number) fila[0]).longValue()))
                .map(fila -> new DepartmentEquipmentStatusDTO(
                        ((Number) fila[0]).longValue(), //idBranch
                        (String) fila[1], //nameBranch
                        ((Number) fila[2]).longValue(), //idDepartment
                        (String) fila[3], //nameDepartment
                        ((Number) fila[4]).longValue(), //activeEquipments
                        ((Number) fila[5]).longValue() //inactiveEquipments
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Consulta con JOIN 5: Consumo total (kWh) y costo total (S/) de cada sede de una empresa
    @GetMapping("/company/consumption/{idCompany}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<BranchConsumptionDTO>> consumoPorSedeDeEmpresa(@PathVariable Long idCompany) {
        //Validando que el id de empresa del usuario autenticado sea igual al id de empresa que está consultando.
        acceso.validarEmpresa(idCompany);

        //Validando que la empresa enviada (su id) exista
        cS.listById(idCompany)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe la empresa con el id: " + idCompany)
                );

        //Convirtiendo de tipo List<Object[]> a tipo List<BranchConsumptionDTO>
        List<BranchConsumptionDTO> lista = bS.consumptionByBranchOfCompany(idCompany)
                .stream()
                .map(fila -> new BranchConsumptionDTO(
                        ((Number) fila[0]).longValue(),    // id_branch
                        (String) fila[1],                  // name_branch
                        ((Number) fila[2]).longValue(),    // total_registros
                        redondear((Number) fila[3]),       // total_kwh
                        redondear((Number) fila[4])        // total_costo (S/)
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }


    // Consulta con JOIN 4: Potencia instalada total (en Watts) por sede.
    // Es capacidad instalada, no consumo. Suma los watts de los equipos activos, sin importar las horas de uso.
    @GetMapping("/installed-power")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<BranchPowerDTO>> potenciaInstaladaPorSede() {
        //Para que, dependiendo de qué usuario es, solo pueda ver la información de su propia empresa.
        // Si es admin, puede ver información de todas las sedes.
        Set<Long> sedes = sedesVisibles();

        // Convirtiendo de tipo List<Object[]> a tipo List<BranchPowerDTO>
        List<BranchPowerDTO> lista = bS.installedPowerByBranch()
                .stream()
                .filter(fila -> sedes == null || sedes.contains(((Number) fila[0]).longValue()))
                .map(fila -> new BranchPowerDTO(
                        ((Number) fila[0]).longValue(),    // id_branch
                        (String) fila[1],                  // name_branch
                        ((Number) fila[2]).longValue(),    // total_equipos
        // Aquí no hace falta redondear: la potencia se registra en watts enteros
        // o con pocos decimales,y no se acumulan errores como en los costos.
                        ((Number) fila[3]).doubleValue()   // total_watts
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Metodo para devolver los ids de las sedes de la empresa donde pertenece el usuario autenticado.
    // Si es admin, devuelve null (puede ver todas las sedes).
    private Set<Long> sedesVisibles() {
        Long idEmpresa = acceso.idEmpresaUsuario();
        if (idEmpresa == null) {
            return null;
        }
        return bS.listByCompany(idEmpresa)
                .stream()
                .map(Branch::getIdBranch)
                .collect(Collectors.toSet());
    }


    //Redondear a 2 decimales (evita resultados como 16.249999999999996)
    private Double redondear(Number valor) {
        return Math.round(valor.doubleValue() * 100.0) / 100.0;
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