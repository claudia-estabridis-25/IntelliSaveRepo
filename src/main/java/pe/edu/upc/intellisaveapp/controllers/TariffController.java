package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.TariffDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.TariffDTOList;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Tariff;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ITariffService;
import org.springframework.security.access.prepost.PreAuthorize;
import pe.edu.upc.intellisaveapp.dtos.TariffSupplierDTO;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tariffs")
public class TariffController {
    private final ITariffService tS;
    private final IBranchService bS;
    private final CompanyAccess acceso; //Para filtrar y validar la empresa del usuario autenticado

    public TariffController(ITariffService tS, IBranchService bS, CompanyAccess acceso) {
        this.tS = tS;
        this.bS = bS;
        this.acceso = acceso;
    }

    // HU039: listar todas las tarifas
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<TariffDTOList>> listar() {
        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        List<TariffDTOList> lista = tS.list()
                .stream()
                //Filtrando por empresa: el admin ve todas las tarifas y los demás solo las de las sedes de su empresa
                .filter(t -> idCompany == null || companyOf(t).equals(idCompany))
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU039: historial de tarifas de una sede
    @GetMapping("/branch/{idBranch}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<TariffDTOList>> listarPorSede(@PathVariable Long idBranch) {
        //Busca la sede y valida que sea de la empresa del usuario autenticado
        buscarSede(idBranch);

        List<TariffDTOList> lista = tS.listByBranch(idBranch)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU044: tarifa vigente de una sede (hoy, o en la fecha indicada)
    @GetMapping("/branch/{idBranch}/current")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<TariffDTOList> tarifaVigente(
            @PathVariable Long idBranch,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        //Busca la sede y valida que sea de la empresa del usuario autenticado
        buscarSede(idBranch);
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();

        Tariff tariff = tS.findCurrentByBranch(idBranch, dia)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "La sede con id " + idBranch + " no tiene una tarifa vigente en la fecha " + dia
                        )
                );

        return ResponseEntity.ok(toDTO(tariff));
    }

    // HU054: detalle de una tarifa
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<TariffDTOList> listarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(buscarTarifa(id)));
    }

    // HU15: registrar tarifa
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<TariffDTOList> registrar(@Valid @RequestBody TariffDTOInsert dto) {
        //Valida que la sede donde se registra la tarifa sea de la empresa del usuario autenticado
        Branch branch = buscarSede(dto.getIdBranch());
        validarTarifa(dto, null);

        Tariff tariff = new Tariff();
        copiarDatos(dto, tariff, branch);
        tS.insert(tariff);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tariff.getIdTariff())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(tariff));
    }

    // HU040: actualizar tarifa
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<TariffDTOList> actualizar(@Valid @RequestBody TariffDTOInsert dto) {
        if (dto.getIdTariff() == null) {
            throw new BusinessRuleException("El id de la tarifa es obligatorio para actualizar");
        }

        //Valida que la tarifa y la nueva sede sean de la empresa del usuario autenticado,
        //así no se puede mover una tarifa a una sede de otra empresa
        Tariff tariff = buscarTarifa(dto.getIdTariff());
        Branch branch = buscarSede(dto.getIdBranch());
        validarTarifa(dto, dto.getIdTariff());

        copiarDatos(dto, tariff, branch);
        tS.update(tariff);

        return ResponseEntity.ok(toDTO(tariff));
    }

    // Consulta nativa con JOIN: cantidad de tarifas y costo promedio por proveedor y sede
    @GetMapping("/by-supplier")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<TariffSupplierDTO>> tarifasPorProveedor() {
        Long idCompany = acceso.idEmpresaUsuario();

        //El admin ve las tarifas de todas las sedes
        if (idCompany == null) {
            return ResponseEntity.ok(tS.tariffsBySupplier());
        }

        //Ids de las sedes que pertenecen a la empresa del usuario autenticado
        Set<Long> branchIds = bS.listByCompany(idCompany)
                .stream()
                .map(Branch::getIdBranch)
                .collect(Collectors.toSet());

        //De la consulta nativa, se dejan solo las filas de las sedes de su empresa
        List<TariffSupplierDTO> lista = tS.tariffsBySupplier()
                .stream()
                .filter(fila -> branchIds.contains(fila.getIdBranch()))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // No hay DELETE: las tarifas forman parte del historial de costos (regla de negocio)

    // ===================== MÉTODOS DE APOYO =====================

    //Los 2 métodos buscar validan además que el registro sea de la empresa del usuario autenticado.
    //Si no existe devuelven 404, y si es de otra empresa devuelven 403.
    private Branch buscarSede(Long idBranch) {
        Branch branch = bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una sede con el id: " + idBranch)
                );

        acceso.validarEmpresa(branch.getCompany().getIdCompany());

        return branch;
    }

    private Tariff buscarTarifa(Long id) {
        Tariff tariff = tS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una tarifa con el id: " + id)
                );

        acceso.validarEmpresa(companyOf(tariff));

        return tariff;
    }

    //Obtiene el id de la empresa de la tarifa, siguiendo el camino Tarifa -> Sede -> Empresa
    private Long companyOf(Tariff tariff) {
        return tariff.getBranch().getCompany().getIdCompany();
    }

    // Reglas: fechas en orden y sin cruzarse con otra tarifa de la misma sede
    private void validarTarifa(TariffDTOInsert dto, Long idTariffActual) {
        if (dto.getInitialEffectiveDate().isAfter(dto.getEndEffectiveDate())) {
            throw new BusinessRuleException(
                    "La fecha de inicio de vigencia no puede ser posterior a la fecha de fin"
            );
        }

        boolean seCruza = tS.listByBranch(dto.getIdBranch())
                .stream()
                .filter(t -> idTariffActual == null || !t.getIdTariff().equals(idTariffActual))
                .anyMatch(t -> !t.getInitialEffectiveDate().isAfter(dto.getEndEffectiveDate())
                        && !t.getEndEffectiveDate().isBefore(dto.getInitialEffectiveDate()));

        if (seCruza) {
            throw new BusinessRuleException(
                    "Las fechas de vigencia se cruzan con otra tarifa de la misma sede"
            );
        }
    }

    private void copiarDatos(TariffDTOInsert dto, Tariff tariff, Branch branch) {
        tariff.setBranch(branch);
        tariff.setInitialEffectiveDate(dto.getInitialEffectiveDate());
        tariff.setEndEffectiveDate(dto.getEndEffectiveDate());
        tariff.setCostPerKwh(dto.getCostPerKwh());
        tariff.setSupplier(dto.getSupplier());
    }

    private TariffDTOList toDTO(Tariff tariff) {
        TariffDTOList dto = new TariffDTOList();
        dto.setIdTariff(tariff.getIdTariff());
        dto.setIdBranch(tariff.getBranch().getIdBranch());
        dto.setNameBranch(tariff.getBranch().getNameBranch());
        dto.setInitialEffectiveDate(tariff.getInitialEffectiveDate());
        dto.setEndEffectiveDate(tariff.getEndEffectiveDate());
        dto.setCostPerKwh(tariff.getCostPerKwh());
        dto.setSupplier(tariff.getSupplier());
        return dto;
    }

}