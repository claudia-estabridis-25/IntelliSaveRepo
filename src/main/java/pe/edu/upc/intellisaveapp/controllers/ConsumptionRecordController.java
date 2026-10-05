package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionRecordDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionRecordDTOList;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;
import pe.edu.upc.intellisaveapp.dtos.ElevatedConsumptionDTO;
import org.springframework.format.annotation.DateTimeFormat;
import pe.edu.upc.intellisaveapp.entities.Tariff;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ITariffService;
import pe.edu.upc.intellisaveapp.dtos.CategoryConsumptionDTO;
import pe.edu.upc.intellisaveapp.dtos.DepartmentConsumptionDTO;
import pe.edu.upc.intellisaveapp.entities.Users;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IUsersService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/consumption-records")
public class ConsumptionRecordController {
    // HU012 CA04: mensaje cuando no hay equipos con consumo elevado
    private static final String SIN_CONSUMO_ELEVADO = "No se han detectado equipos con consumo elevado";

    private final IConsumptionRecordService crS;
    private final IEquipmentService eS;
    private final ITariffService tS;
    private final IUsersService uS;
    private final ModelMapper mP;

    public ConsumptionRecordController(IConsumptionRecordService crS, IEquipmentService eS,
                                       ITariffService tS, IUsersService uS, ModelMapper mP) {
        this.crS = crS;
        this.eS = eS;
        this.tS = tS;
        this.uS = uS;
        this.mP = mP;
    }

    @GetMapping //Cualquier usuario autenticado
    public ResponseEntity<List<ConsumptionRecordDTOList>> listar() {
        List<ConsumptionRecordDTOList> lista = crS.list()
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU09 (supervisor) y HU10 (empleado): registrar consumo
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<ConsumptionRecordDTOList> registrar(@Valid @RequestBody ConsumptionRecordDTOInsert dto) {
        Equipment equipment = eS.listById(dto.getIdEquipment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + dto.getIdEquipment()
                        )
                );

        validarAreaDelEmpleado(equipment);
        validarEquipoActivo(equipment);

        ConsumptionRecord cr = mP.map(dto, ConsumptionRecord.class);
        cr.setIdConsumptionRecord(null); // Un POST siempre crea un registro nuevo
        cr.setEquipment(equipment);

        Tariff tariff = obtenerTarifa(dto, equipment);
        cr.setTariff(tariff);

        Double kwhConsumption = (equipment.getWattPowerEquipment() * dto.getHoursOfUse()) / 1000;
        cr.setKwhConsumption(kwhConsumption);
        cr.setCostTotal(kwhConsumption * tariff.getCostPerKwh());

        crS.insert(cr);

        ConsumptionRecordDTOList responseDTO = mP.map(cr, ConsumptionRecordDTOList.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cr.getIdConsumptionRecord())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    // HU060: actualizar un registro de consumo
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ConsumptionRecordDTOList> actualizar(@Valid @RequestBody ConsumptionRecordDTOInsert dto) {
        if (dto.getIdConsumptionRecord() == null) {
            throw new BusinessRuleException("El id del registro de consumo es obligatorio para actualizar");
        }

        ConsumptionRecord existente = crS.listById(dto.getIdConsumptionRecord())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de consumo con el id: " + dto.getIdConsumptionRecord()
                        )
                );

        Equipment equipment = eS.listById(dto.getIdEquipment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + dto.getIdEquipment()
                        )
                );

        // Si se cambia el equipo del registro, el nuevo equipo debe estar activo
        if (!equipment.getIdEquipment().equals(existente.getEquipment().getIdEquipment())) {
            validarEquipoActivo(equipment);
        }

        ConsumptionRecord cr = mP.map(dto, ConsumptionRecord.class);
        cr.setIdConsumptionRecord(existente.getIdConsumptionRecord());
        cr.setEquipment(equipment);

        Tariff tariff = obtenerTarifa(dto, equipment);
        cr.setTariff(tariff);

        Double kwhConsumption = (equipment.getWattPowerEquipment() * dto.getHoursOfUse()) / 1000;
        cr.setKwhConsumption(kwhConsumption);
        cr.setCostTotal(kwhConsumption * tariff.getCostPerKwh());

        crS.update(cr);

        ConsumptionRecordDTOList responseDTO = mP.map(cr, ConsumptionRecordDTOList.class);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}") //Cualquier usuario autenticado
    public ResponseEntity<ConsumptionRecordDTOList> listarPorId(@PathVariable Long id) {
        ConsumptionRecord cr = crS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de consumo con el id: " + id
                        )
                );

        ConsumptionRecordDTOList dto = mP.map(cr, ConsumptionRecordDTOList.class);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ConsumptionRecord cr = crS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de consumo con el id: " + id
                        )
                );
        crS.delete(cr.getIdConsumptionRecord());
        return ResponseEntity.noContent().build();
    }

    //Listar consumo por área
    @GetMapping("/department/{idDepartment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorArea(@PathVariable Long idDepartment) {
        List<ConsumptionRecordDTOList> lista = crS.listByDepartment(idDepartment)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Buscar consumo por sede
    @GetMapping("/branch/{idBranch}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorSede(@PathVariable Long idBranch) {
        List<ConsumptionRecordDTOList> lista = crS.listByBranch(idBranch)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Buscar consumo por equipo
    @GetMapping("/equipment/{idEquipment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorEquipo(@PathVariable Long idEquipment) {
        List<ConsumptionRecordDTOList> lista = crS.listByEquipment(idEquipment)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Buscar consumo por rango de fecha
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        validarRangoFechas(desde, hasta);

        List<ConsumptionRecordDTOList> lista = crS.listByDateRange(desde, hasta)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Historial de consumos
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> historial(
            @RequestParam(required = false) Long idBranch,
            @RequestParam(required = false) Long idDepartment,
            @RequestParam(required = false) Long idEquipment,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        validarRangoFechas(desde, hasta);

        List<ConsumptionRecordDTOList> lista = crS.listHistory(idBranch, idDepartment, idEquipment, desde, hasta)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Consulta nativa 1: consumo total por área de una sede
    @GetMapping("/branch/{idBranch}/by-department")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<DepartmentConsumptionDTO>> consumoPorAreaDeSede(@PathVariable Long idBranch) {
        return ResponseEntity.ok(crS.consumptionByDepartmentOfBranch(idBranch));
    }

    // Consulta nativa 2: consumo por categoría de equipo en un periodo
    @GetMapping("/by-category")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CategoryConsumptionDTO>> consumoPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        validarRangoFechas(desde, hasta);

        return ResponseEntity.ok(crS.consumptionByEquipmentCategory(desde, hasta));
    }

    @GetMapping("/department/{idDepartment}/average")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Double> promedioKwhPorArea(@PathVariable Long idDepartment) {
        return ResponseEntity.ok(crS.averageKwhByDepartment(idDepartment));
    }

    @GetMapping("/department/{idDepartment}/elevated-consumption")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ElevatedConsumptionDTO>> equiposConsumoElevado(@PathVariable Long idDepartment) {
        // 1. Consumo total (kWh) de cada equipo del área
        Map<Equipment, Double> totalPorEquipo = crS.listByDepartment(idDepartment)
                .stream()
                .collect(Collectors.groupingBy(
                        ConsumptionRecord::getEquipment,
                        Collectors.summingDouble(ConsumptionRecord::getKwhConsumption)
                ));

        if (totalPorEquipo.isEmpty()) {
            throw new ResourceNotFoundException(SIN_CONSUMO_ELEVADO);
        }

        // 2. Promedio del área = promedio de los totales de sus equipos
        Double promedio = totalPorEquipo.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // 3. Consumo elevado = 30% o más sobre el promedio del área
        Double umbral = promedio * 1.3;

        List<ElevatedConsumptionDTO> resultado = totalPorEquipo.entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= umbral)
                .map(entry -> {
                    ElevatedConsumptionDTO dto = new ElevatedConsumptionDTO();
                    dto.setIdEquipment(entry.getKey().getIdEquipment());
                    dto.setNameEquipment(entry.getKey().getNameEquipment());
                    dto.setTotalKwhConsumption(entry.getValue());
                    dto.setDepartmentAverageKwh(promedio);
                    dto.setElevatedConsumption(true);
                    return dto;
                })
                .toList();

        // HU012 CA04: ningún equipo supera el umbral de consumo elevado
        if (resultado.isEmpty()) {
            throw new ResourceNotFoundException(SIN_CONSUMO_ELEVADO);
        }

        return ResponseEntity.ok(resultado);
    }

    //Métodos complementarios

    // HU10: un empleado solo puede registrar consumo de los equipos de su propia área.
    // El administrador y el supervisor pueden registrar en cualquier área.
    private void validarAreaDelEmpleado(Equipment equipment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean esAdminOSupervisor = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPERVISOR"));

        if (esAdminOSupervisor) {
            return;
        }

        Users usuario = uS.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("No se pudo identificar al usuario autenticado"));

        if (usuario.getDepartment() == null) {
            throw new AccessDeniedException("El empleado no tiene un área asignada y no puede registrar consumo");
        }

        Long areaEmpleado = usuario.getDepartment().getIdDepartment();
        Long areaEquipo = equipment.getDepartment().getIdDepartment();

        if (!areaEmpleado.equals(areaEquipo)) {
            throw new AccessDeniedException("Solo puede registrar consumo de los equipos de su área");
        }
    }

    private void validarEquipoActivo(Equipment equipment) {
        if ("Inactivo".equalsIgnoreCase(equipment.getStatusEquipment())) {
            throw new BusinessRuleException(
                    "El equipo con id " + equipment.getIdEquipment()
                            + " está dado de baja y no puede registrar consumo"
            );
        }
    }

    private void validarRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessRuleException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
    }

    // HU09 CA02: el costo se calcula con la tarifa vigente de la sede del equipo
    private Tariff obtenerTarifa(ConsumptionRecordDTOInsert dto, Equipment equipment) {
        Long idBranch = equipment.getDepartment().getBranch().getIdBranch();
        java.time.LocalDate fecha = dto.getDateTimeRecord().toLocalDate();

        // Si no se envía idTariff, se busca automáticamente la tarifa vigente
        if (dto.getIdTariff() == null) {
            return tS.findCurrentByBranch(idBranch, fecha)
                    .orElseThrow(() ->
                            new BusinessRuleException(
                                    "La sede del equipo no tiene una tarifa vigente para la fecha " + fecha
                                            + ". Registre una tarifa antes de registrar el consumo."
                            )
                    );
        }

        // Si se envía idTariff, se valida que exista, sea de la sede del equipo y esté vigente
        Tariff tariff = tS.listById(dto.getIdTariff())
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una tarifa con el id: " + dto.getIdTariff())
                );

        if (!tariff.getBranch().getIdBranch().equals(idBranch)) {
            throw new BusinessRuleException("La tarifa indicada no pertenece a la sede del equipo");
        }

        if (fecha.isBefore(tariff.getInitialEffectiveDate()) || fecha.isAfter(tariff.getEndEffectiveDate())) {
            throw new BusinessRuleException("La tarifa indicada no está vigente en la fecha del registro");
        }

        return tariff;
    }
}