package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.BranchEmissionDTO;
import pe.edu.upc.intellisaveapp.dtos.CarbonFootprintComparisonDTO;
import pe.edu.upc.intellisaveapp.dtos.CarbonFootprintDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.CarbonFootprintDTOList;
import pe.edu.upc.intellisaveapp.dtos.DepartmentEmissionDTO;
import pe.edu.upc.intellisaveapp.entities.CarbonFootprint;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICarbonFootprintService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;

import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/carbon-footprints")
public class CarbonFootprintController {
    private final ICarbonFootprintService cfS;
    private final IDepartmentService dS;
    private final IBranchService bS;

    public CarbonFootprintController(ICarbonFootprintService cfS, IDepartmentService dS, IBranchService bS) {
        this.cfS = cfS;
        this.dS = dS;
        this.bS = bS;
    }

    // HU20: listar huellas de carbono, con filtro opcional por periodo (más recientes primero)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CarbonFootprintDTOList>> listar(@RequestParam(required = false) String timePeriod) {
        List<CarbonFootprintDTOList> lista = cfS.list()
                .stream()
                .filter(cf -> timePeriod == null || timePeriod.equalsIgnoreCase(cf.getTimePeriod()))
                .sorted(Comparator.comparing(CarbonFootprint::getCalculationDate).reversed())
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU050: comparar la huella de un área o de una sede entre dos fechas de cálculo (A = base, B = a comparar)
    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<CarbonFootprintComparisonDTO> comparar(
            @RequestParam(required = false) Long idDepartment,
            @RequestParam(required = false) Long idBranch,
            @RequestParam String timePeriod,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateA,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateB) {

        if ((idDepartment == null) == (idBranch == null)) {
            throw new BusinessRuleException("Indique solo un área (idDepartment) o solo una sede (idBranch)");
        }

        if (idDepartment != null) {
            buscarArea(idDepartment);
        } else {
            buscarSede(idBranch);
        }

        return ResponseEntity.ok(cfS.compare(idDepartment, idBranch, timePeriod, dateA, dateB));
    }

    // HU057: exportar la constancia de sostenibilidad en PDF
    @GetMapping("/certificate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<byte[]> constancia(
            @RequestParam Long idDepartment,
            @RequestParam String timePeriod,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calculationDate) {

        buscarArea(idDepartment);

        // CA03: si no existe el cálculo, no se genera el documento
        CarbonFootprint footprint = cfS.findCalculation(idDepartment, timePeriod, calculationDate)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un cálculo de huella de carbono para el periodo seleccionado"
                        )
                );

        byte[] pdf = cfS.generateCertificatePdf(footprint);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"constancia-huella-carbono.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Consulta nativa con JOIN: emisiones totales de CO2 por sede
    @GetMapping("/by-branch")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<BranchEmissionDTO>> emisionesPorSede() {
        return ResponseEntity.ok(cfS.emissionsByBranch());
    }

    // Consulta con JOIN: emisiones totales de CO2 por área (opcional: solo las áreas de una sede)
    @GetMapping("/by-department")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<DepartmentEmissionDTO>> emisionesPorArea(
            @RequestParam(required = false) Long idBranch) {
        if (idBranch != null) {
            buscarSede(idBranch);
        }
        return ResponseEntity.ok(cfS.emissionsByDepartment(idBranch));
    }

    // Detalle de una huella de carbono, Listar por id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<CarbonFootprintDTOList> listarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(buscarHuella(id)));
    }

    // Historial de huellas de carbono de un área
    @GetMapping("/department/{idDepartment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CarbonFootprintDTOList>> listarPorArea(@PathVariable Long idDepartment) {
        buscarArea(idDepartment);

        List<CarbonFootprintDTOList> lista = cfS.listByDepartment(idDepartment)
                .stream()
                .sorted(Comparator.comparing(CarbonFootprint::getCalculationDate).reversed())
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Huellas de carbono de todas las áreas de una sede
    @GetMapping("/branch/{idBranch}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CarbonFootprintDTOList>> listarPorSede(@PathVariable Long idBranch) {
        buscarSede(idBranch);

        List<CarbonFootprintDTOList> lista = cfS.listByBranch(idBranch)
                .stream()
                .sorted(Comparator.comparing(CarbonFootprint::getCalculationDate).reversed())
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Calcular y registrar la huella de carbono de un área
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<CarbonFootprintDTOList> registrar(@Valid @RequestBody CarbonFootprintDTOInsert dto) {
        Department department = buscarArea(dto.getIdDepartment());
        LocalDate fecha = (dto.getCalculationDate() != null) ? dto.getCalculationDate() : LocalDate.now();

        CarbonFootprint footprint = cfS.calculate(new CarbonFootprint(), department,
                dto.getTimePeriod(), fecha, dto.getEmissionFactor());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(footprint.getIdFootprint())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(footprint));
    }

    // HU19: calcular la huella de todas las áreas a la vez (omite las que no tienen consumos en el periodo)
    @PostMapping("/calculate-all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CarbonFootprintDTOList>> calcularTodas(
            @RequestParam String timePeriod,
            @RequestParam Double emissionFactor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calculationDate) {

        if (emissionFactor <= 0) {
            throw new BusinessRuleException("El factor de emisión debe ser mayor a 0");
        }

        LocalDate fecha = (calculationDate != null) ? calculationDate : LocalDate.now();

        List<CarbonFootprintDTOList> lista = cfS.calculateAll(dS.list(), timePeriod, fecha, emissionFactor)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(lista);
    }

    // Actualizar (recalcular) una huella de carbono
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<CarbonFootprintDTOList> actualizar(@Valid @RequestBody CarbonFootprintDTOInsert dto) {
        if (dto.getIdFootprint() == null) {
            throw new BusinessRuleException("El id de la huella de carbono es obligatorio para actualizar");
        }

        CarbonFootprint existente = buscarHuella(dto.getIdFootprint());
        Department department = buscarArea(dto.getIdDepartment());

        // Si no se envía la fecha, se conserva la original
        LocalDate fecha = (dto.getCalculationDate() != null)
                ? dto.getCalculationDate()
                : existente.getCalculationDate();

        CarbonFootprint footprint = cfS.calculate(existente, department,
                dto.getTimePeriod(), fecha, dto.getEmissionFactor());

        return ResponseEntity.ok(toDTO(footprint));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        CarbonFootprint footprint = buscarHuella(id);
        cfS.delete(footprint.getIdFootprint());
        return ResponseEntity.noContent().build();
    }

    // ===================== MÉTODOS DE APOYO =====================

    private CarbonFootprint buscarHuella(Long id) {
        return cfS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una huella de carbono con el id: " + id)
                );
    }

    private Department buscarArea(Long idDepartment) {
        return dS.listById(idDepartment)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe un área con el id: " + idDepartment)
                );
    }

    private void buscarSede(Long idBranch) {
        bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una sede con el id: " + idBranch)
                );
    }

    private CarbonFootprintDTOList toDTO(CarbonFootprint cf) {
        CarbonFootprintDTOList dto = new CarbonFootprintDTOList();
        dto.setIdFootprint(cf.getIdFootprint());
        dto.setIdDepartment(cf.getDepartment().getIdDepartment());
        dto.setNameDepartment(cf.getDepartment().getNameDepartment());
        dto.setIdBranch(cf.getDepartment().getBranch().getIdBranch());
        dto.setNameBranch(cf.getDepartment().getBranch().getNameBranch());
        dto.setTimePeriod(cf.getTimePeriod());
        dto.setPeriodStartDate(cfS.periodStart(cf.getTimePeriod(), cf.getCalculationDate()));
        dto.setCalculationDate(cf.getCalculationDate());
        dto.setEmissionFactor(cf.getEmissionFactor());
        dto.setKwhTotalConsumption(cf.getKwhTotalConsumption());
        dto.setCo2Emissions(cf.getCo2Emissions());
        return dto;
    }
}