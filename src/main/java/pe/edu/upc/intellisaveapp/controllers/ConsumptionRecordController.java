package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionRecordDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionRecordDTOList;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;
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
import java.util.Comparator;
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
    private final IDepartmentService dS; //Para validar a qué empresa pertenece un área
    private final IBranchService bS; //Para validar a qué empresa pertenece una sede
    private final ModelMapper mP;
    private final CompanyAccess acceso; //Para filtrar y validar la empresa del usuario autenticado

    public ConsumptionRecordController(IConsumptionRecordService crS, IEquipmentService eS,
                                       ITariffService tS, IUsersService uS, IDepartmentService dS,
                                       IBranchService bS, ModelMapper mP, CompanyAccess acceso) {
        this.crS = crS;
        this.eS = eS;
        this.tS = tS;
        this.uS = uS;
        this.dS = dS;
        this.bS = bS;
        this.mP = mP;
        this.acceso = acceso;
    }

    //Listar todos los consumos
    @GetMapping //Cualquier usuario autenticado
    public ResponseEntity<List<ConsumptionRecordDTOList>> listar() {
        //El admin ve todos los consumos y los demás solo los de su empresa
        return ResponseEntity.ok(filterByCompany(crS.list()));
    }

    // HU09 (supervisor) y HU10 (empleado): registrar consumo
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<ConsumptionRecordDTOList> registrar(@Valid @RequestBody ConsumptionRecordDTOInsert dto) {
        //Busca el equipo y valida que sea de la empresa del usuario autenticado
        Equipment equipment = findEquipment(dto.getIdEquipment());

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

        //Busca el registro y valida que sea de la empresa del usuario autenticado
        ConsumptionRecord existente = findRecord(dto.getIdConsumptionRecord());

        //Busca el equipo y valida que también sea de la empresa del usuario autenticado
        Equipment equipment = findEquipment(dto.getIdEquipment());

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

    //Listar consumo por su ID
    @GetMapping("/{id}") //Cualquier usuario autenticado
    public ResponseEntity<ConsumptionRecordDTOList> listarPorId(@PathVariable Long id) {
        //Busca el registro y valida que sea de la empresa del usuario autenticado
        ConsumptionRecord cr = findRecord(id);

        ConsumptionRecordDTOList dto = mP.map(cr, ConsumptionRecordDTOList.class);

        return ResponseEntity.ok(dto);
    }

    //Eliminar consumo
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        //Busca el registro y valida que sea de la empresa del usuario autenticado
        ConsumptionRecord cr = findRecord(id);
        crS.delete(cr.getIdConsumptionRecord());
        return ResponseEntity.noContent().build();
    }

    //Listar consumo por área
    @GetMapping("/department/{idDepartment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorArea(@PathVariable Long idDepartment) {
        //Validando que el área consultada sea de la empresa del usuario autenticado
        validateDepartment(idDepartment);

        return ResponseEntity.ok(filterByCompany(crS.listByDepartment(idDepartment)));
    }

    //Buscar consumo por sede
    @GetMapping("/branch/{idBranch}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorSede(@PathVariable Long idBranch) {
        //Validando que la sede consultada sea de la empresa del usuario autenticado
        validateBranch(idBranch);

        return ResponseEntity.ok(filterByCompany(crS.listByBranch(idBranch)));
    }

    //Buscar consumo por equipo
    @GetMapping("/equipment/{idEquipment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorEquipo(@PathVariable Long idEquipment) {
        //Validando que el equipo consultado sea de la empresa del usuario autenticado
        findEquipment(idEquipment);

        return ResponseEntity.ok(filterByCompany(crS.listByEquipment(idEquipment)));
    }

    //Buscar consumo por rango de fecha
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        validarRangoFechas(desde, hasta);

        //El admin ve los consumos de todas las empresas y los demás solo los de su empresa
        return ResponseEntity.ok(filterByCompany(crS.listByDateRange(desde, hasta)));
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

        //Si se envía una sede, área o equipo, se valida que sea de la empresa del usuario autenticado
        if (idBranch != null) {
            validateBranch(idBranch);
        }
        if (idDepartment != null) {
            validateDepartment(idDepartment);
        }
        if (idEquipment != null) {
            findEquipment(idEquipment);
        }

        //Si no se envía ningún filtro, igual se devuelven solo los consumos de la empresa del usuario
        return ResponseEntity.ok(filterByCompany(crS.listHistory(idBranch, idDepartment, idEquipment, desde, hasta)));
    }

    // Consulta nativa 1: consumo total por área de una sede
    @GetMapping("/branch/{idBranch}/by-department")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<DepartmentConsumptionDTO>> consumoPorAreaDeSede(@PathVariable Long idBranch) {
        //Validando que la sede consultada sea de la empresa del usuario autenticado
        validateBranch(idBranch);

        return ResponseEntity.ok(crS.consumptionByDepartmentOfBranch(idBranch));
    }

    // Consulta nativa 2: consumo por categoría de equipo en un periodo
    @GetMapping("/by-category")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<CategoryConsumptionDTO>> consumoPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        validarRangoFechas(desde, hasta);

        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        //El admin usa la consulta nativa, que suma los consumos de todas las empresas
        if (idCompany == null) {
            return ResponseEntity.ok(crS.consumptionByEquipmentCategory(desde, hasta));
        }

        //Para los demás usuarios, se agrupan por categoría solo los consumos de su empresa
        Map<String, List<ConsumptionRecord>> recordsByCategory = crS.listByDateRange(desde, hasta)
                .stream()
                .filter(cr -> companyOf(cr).equals(idCompany))
                .collect(Collectors.groupingBy(cr -> cr.getEquipment().getCategoryEquipment()));

        //Armando el DTO con los mismos datos que devuelve la consulta nativa:
        //categoría, cantidad de equipos, cantidad de registros, total kWh y costo total
        List<CategoryConsumptionDTO> result = recordsByCategory.entrySet()
                .stream()
                .map(entry -> new CategoryConsumptionDTO(
                        entry.getKey(),
                        entry.getValue().stream().map(cr -> cr.getEquipment().getIdEquipment()).distinct().count(),
                        (long) entry.getValue().size(),
                        round(entry.getValue().stream().mapToDouble(ConsumptionRecord::getKwhConsumption).sum()),
                        round(entry.getValue().stream().mapToDouble(ConsumptionRecord::getCostTotal).sum())
                ))
                //Ordenando de mayor a menor consumo, igual que la consulta nativa
                .sorted(Comparator.comparing(CategoryConsumptionDTO::getTotalKwh).reversed())
                .toList();

        return ResponseEntity.ok(result);
    }

    //Promedio de consumo (kWh) de un área
    @GetMapping("/department/{idDepartment}/average")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Double> promedioKwhPorArea(@PathVariable Long idDepartment) {
        //Validando que el área consultada sea de la empresa del usuario autenticado
        validateDepartment(idDepartment);

        return ResponseEntity.ok(crS.averageKwhByDepartment(idDepartment));
    }

    //Equipos con consumo elevado en un área
    @GetMapping("/department/{idDepartment}/elevated-consumption")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ElevatedConsumptionDTO>> equiposConsumoElevado(@PathVariable Long idDepartment) {
        //Validando que el área consultada sea de la empresa del usuario autenticado
        validateDepartment(idDepartment);

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

    //Deja solo los consumos de la empresa del usuario autenticado y los convierte a DTO.
    //Si el usuario es admin de la plataforma (idCompany = null), devuelve todos.
    private List<ConsumptionRecordDTOList> filterByCompany(List<ConsumptionRecord> records) {
        Long idCompany = acceso.idEmpresaUsuario();

        return records
                .stream()
                .filter(cr -> idCompany == null || companyOf(cr).equals(idCompany))
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();
    }

    //Busca un registro de consumo por su id y valida que sea de la empresa del usuario autenticado.
    //Si no existe devuelve 404, y si es de otra empresa devuelve 403.
    private ConsumptionRecord findRecord(Long id) {
        ConsumptionRecord cr = crS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de consumo con el id: " + id
                        )
                );

        acceso.validarEmpresa(companyOf(cr));

        return cr;
    }

    //Busca un equipo por su id y valida que sea de la empresa del usuario autenticado.
    //Si no existe devuelve 404, y si es de otra empresa devuelve 403.
    private Equipment findEquipment(Long idEquipment) {
        Equipment equipment = eS.listById(idEquipment)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + idEquipment
                        )
                );

        acceso.validarEmpresa(equipment.getDepartment().getBranch().getCompany().getIdCompany());

        return equipment;
    }

    //Valida que el área exista y que sea de la empresa del usuario autenticado
    private void validateDepartment(Long idDepartment) {
        Department department = dS.listById(idDepartment)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un área con el id: " + idDepartment
                        )
                );

        acceso.validarEmpresa(department.getBranch().getCompany().getIdCompany());
    }

    //Valida que la sede exista y que sea de la empresa del usuario autenticado
    private void validateBranch(Long idBranch) {
        Branch branch = bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una sede con el id: " + idBranch
                        )
                );

        acceso.validarEmpresa(branch.getCompany().getIdCompany());
    }

    //Obtiene el id de la empresa del consumo, siguiendo el camino Consumo -> Equipo -> Área -> Sede -> Empresa
    private Long companyOf(ConsumptionRecord cr) {
        return cr.getEquipment().getDepartment().getBranch().getCompany().getIdCompany();
    }

    //Redondea a 2 decimales, igual que el service en la consulta nativa por categoría
    private Double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

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