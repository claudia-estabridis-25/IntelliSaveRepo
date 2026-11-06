package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.AlertCountDTO;
import pe.edu.upc.intellisaveapp.dtos.AlertDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.AlertDTOList;
import pe.edu.upc.intellisaveapp.dtos.AlertDepartmentCountDTO;
import pe.edu.upc.intellisaveapp.entities.Alert;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IAlertService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    // Estados según las HU16 y HU056 de Trello
    private static final List<String> ESTADOS = List.of("Pendiente", "En revisión", "Atendida", "Descartada");
    private static final List<String> ESTADOS_CERRADOS = List.of("Atendida", "Descartada");
    private static final List<String> PRIORIDADES = List.of("Alta", "Media", "Baja");

    private final IAlertService aS;
    private final IDepartmentService dS;
    private final IEquipmentService eS;
    private final CompanyAccess acceso; //Para filtrar y validar la empresa del usuario autenticado

    public AlertController(IAlertService aS, IDepartmentService dS, IEquipmentService eS, CompanyAccess acceso) {
        this.aS = aS;
        this.dS = dS;
        this.eS = eS;
        this.acceso = acceso;
    }

    // HU16: listar alertas, con filtros opcionales, ordenadas por fecha (más recientes primero) y prioridad
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<List<AlertDTOList>> listar(
            @RequestParam(required = false) Long idDepartment,
            @RequestParam(required = false) Long idEquipment,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {

        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        List<AlertDTOList> lista = aS.list()
                .stream()
                //Filtrando por empresa: el admin ve todas las alertas y los demás solo las de su empresa
                .filter(a -> idCompany == null || companyOf(a).equals(idCompany))
                .filter(a -> idDepartment == null || a.getDepartment().getIdDepartment().equals(idDepartment))
                .filter(a -> idEquipment == null || a.getEquipment().getIdEquipment().equals(idEquipment))
                .filter(a -> status == null || a.getStatusAlert().equalsIgnoreCase(status))
                .filter(a -> priority == null || a.getPriorityLevelAlert().equalsIgnoreCase(priority))
                .sorted(Comparator.comparing(Alert::getDateTimeAlert).reversed()
                        .thenComparing(a -> PRIORIDADES.indexOf(a.getPriorityLevelAlert())))
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU056: historial de alertas atendidas o descartadas, separado del listado general
    // Filtros opcionales: tipo de alerta y rango de fechas de detección
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertDTOList>> historial(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessRuleException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }

        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        List<AlertDTOList> lista = aS.history(type, desde, hasta)
                .stream()
                //Filtrando por empresa: el admin ve todo el historial y los demás solo el de su empresa
                .filter(a -> idCompany == null || companyOf(a).equals(idCompany))
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Detalle de una alerta
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<AlertDTOList> listarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(buscarAlerta(id)));
    }

    // Alertas de un área
    @GetMapping("/department/{idDepartment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<List<AlertDTOList>> listarPorArea(@PathVariable Long idDepartment) {
        //Busca el área y valida que sea de la empresa del usuario autenticado
        buscarArea(idDepartment);

        List<AlertDTOList> lista = aS.listByDepartment(idDepartment)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Alertas de un equipo
    @GetMapping("/equipment/{idEquipment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<List<AlertDTOList>> listarPorEquipo(@PathVariable Long idEquipment) {
        //Busca el equipo y valida que sea de la empresa del usuario autenticado
        buscarEquipo(idEquipment);

        List<AlertDTOList> lista = aS.listByEquipment(idEquipment)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Alertas por estado
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<List<AlertDTOList>> listarPorEstado(@PathVariable String status) {
        //Obteniendo el id de la empresa del usuario autenticado. Si es admin de la plataforma, devuelve null
        Long idCompany = acceso.idEmpresaUsuario();

        List<AlertDTOList> lista = aS.listByStatus(status)
                .stream()
                //Filtrando por empresa: el admin ve todas y los demás solo las de su empresa
                .filter(a -> idCompany == null || companyOf(a).equals(idCompany))
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Consulta simple: cantidad de alertas por estado
    @GetMapping("/count-by-status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertCountDTO>> contarPorEstado() {
        Long idCompany = acceso.idEmpresaUsuario();

        //El admin usa la consulta del repositorio, que cuenta las alertas de todas las empresas
        if (idCompany == null) {
            return ResponseEntity.ok(aS.countByStatus());
        }

        //Para los demás usuarios, se cuentan solo las alertas de su empresa
        return ResponseEntity.ok(countByField(idCompany, Alert::getStatusAlert));
    }

    // Consulta simple: cantidad de alertas por nivel de prioridad
    @GetMapping("/count-by-priority")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertCountDTO>> contarPorPrioridad() {
        Long idCompany = acceso.idEmpresaUsuario();

        //El admin usa la consulta del repositorio, que cuenta las alertas de todas las empresas
        if (idCompany == null) {
            return ResponseEntity.ok(aS.countByPriority());
        }

        //Para los demás usuarios, se cuentan solo las alertas de su empresa
        return ResponseEntity.ok(countByField(idCompany, Alert::getPriorityLevelAlert));
    }

    // Consulta nativa con JOIN: cantidad de alertas por área, tipo y estado
    @GetMapping("/count-by-department")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertDepartmentCountDTO>> contarPorArea() {
        Long idCompany = acceso.idEmpresaUsuario();

        //El admin ve el conteo de todas las áreas
        if (idCompany == null) {
            return ResponseEntity.ok(aS.countByDepartment());
        }

        //Ids de las áreas que pertenecen a la empresa del usuario autenticado
        Set<Long> departmentIds = dS.list()
                .stream()
                .filter(d -> d.getBranch().getCompany().getIdCompany().equals(idCompany))
                .map(Department::getIdDepartment)
                .collect(Collectors.toSet());

        //De la consulta nativa, se dejan solo las filas de las áreas de su empresa
        List<AlertDepartmentCountDTO> lista = aS.countByDepartment()
                .stream()
                .filter(fila -> departmentIds.contains(fila.getIdDepartment()))
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Registrar alerta manualmente
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<AlertDTOList> registrar(@Valid @RequestBody AlertDTOInsert dto) {
        //Busca el equipo y valida que sea de la empresa del usuario autenticado
        Equipment equipment = buscarEquipo(dto.getIdEquipment());

        Alert alert = new Alert();
        copiarDatos(dto, alert, equipment);
        aS.insert(alert);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(alert.getIdAlert())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(alert));
    }

    // HU21: generar automáticamente las alertas de un área (periodo opcional)
    @PostMapping("/department/{idDepartment}/generate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertDTOList>> generar(
            @PathVariable Long idDepartment,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessRuleException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }

        //Busca el área y valida que sea de la empresa del usuario autenticado
        Department department = buscarArea(idDepartment);

        List<AlertDTOList> lista = aS.generateElevatedConsumptionAlerts(department, desde, hasta)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(lista);
    }

    // Actualizar alerta
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<AlertDTOList> actualizar(@Valid @RequestBody AlertDTOInsert dto) {
        if (dto.getIdAlert() == null) {
            throw new BusinessRuleException("El id de la alerta es obligatorio para actualizar");
        }

        //Valida que la alerta y el nuevo equipo sean de la empresa del usuario autenticado,
        //así no se puede mover una alerta a un equipo de otra empresa
        Alert alert = buscarAlerta(dto.getIdAlert());
        Equipment equipment = buscarEquipo(dto.getIdEquipment());

        // Si no se envía la fecha, se conserva la original
        if (dto.getDateTimeAlert() == null) {
            dto.setDateTimeAlert(alert.getDateTimeAlert());
        }

        copiarDatos(dto, alert, equipment);
        aS.update(alert);

        return ResponseEntity.ok(toDTO(alert));
    }

    // HU16: cambiar solo el estado (Pendiente, En revisión, Atendida o Descartada)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<AlertDTOList> cambiarEstado(@PathVariable Long id, @RequestParam String status) {
        Alert alert = buscarAlerta(id);
        aplicarEstado(alert, normalizar(status, ESTADOS, "estado"));
        aS.update(alert);

        return ResponseEntity.ok(toDTO(alert));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Alert alert = buscarAlerta(id);
        aS.delete(alert.getIdAlert());
        return ResponseEntity.noContent().build();
    }

    // ===================== MÉTODOS DE APOYO =====================

    //Los 3 métodos buscar validan además que el registro sea de la empresa del usuario autenticado.
    //Si no existe devuelven 404, y si es de otra empresa devuelven 403.
    private Alert buscarAlerta(Long id) {
        Alert alert = aS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una alerta con el id: " + id)
                );

        acceso.validarEmpresa(companyOf(alert));

        return alert;
    }

    private Department buscarArea(Long idDepartment) {
        Department department = dS.listById(idDepartment)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe un área con el id: " + idDepartment)
                );

        acceso.validarEmpresa(department.getBranch().getCompany().getIdCompany());

        return department;
    }

    private Equipment buscarEquipo(Long idEquipment) {
        Equipment equipment = eS.listById(idEquipment)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe un equipo con el id: " + idEquipment)
                );

        acceso.validarEmpresa(equipment.getDepartment().getBranch().getCompany().getIdCompany());

        return equipment;
    }

    //Obtiene el id de la empresa de la alerta, siguiendo el camino Alerta -> Área -> Sede -> Empresa
    private Long companyOf(Alert alert) {
        return alert.getDepartment().getBranch().getCompany().getIdCompany();
    }

    //Cuenta las alertas de una empresa agrupadas por un campo (estado o prioridad),
    //ordenadas de mayor a menor cantidad, igual que las consultas del repositorio
    private List<AlertCountDTO> countByField(Long idCompany, Function<Alert, String> field) {
        return aS.list()
                .stream()
                .filter(a -> companyOf(a).equals(idCompany))
                .collect(Collectors.groupingBy(field, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new AlertCountDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(AlertCountDTO::getTotalAlerts).reversed())
                .toList();
    }

    // Devuelve el valor permitido con su escritura oficial (sin importar mayúsculas)
    private String normalizar(String valor, List<String> permitidos, String campo) {
        return permitidos.stream()
                .filter(p -> p.equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "El " + campo + " '" + valor + "' no es válido. Valores permitidos: " + permitidos
                        )
                );
    }

    // HU056 CA02: al pasar a "Atendida" o "Descartada" se guarda la fecha de resolución
    private void aplicarEstado(Alert alert, String estado) {
        alert.setStatusAlert(estado);

        if (ESTADOS_CERRADOS.contains(estado)) {
            if (alert.getDateTimeResolution() == null) {
                alert.setDateTimeResolution(LocalDateTime.now());
            }
        } else {
            alert.setDateTimeResolution(null);
        }
    }

    private void copiarDatos(AlertDTOInsert dto, Alert alert, Equipment equipment) {
        alert.setEquipment(equipment);
        alert.setDepartment(equipment.getDepartment()); // el área siempre es la del equipo
        alert.setDateTimeAlert(dto.getDateTimeAlert() != null ? dto.getDateTimeAlert() : LocalDateTime.now());
        alert.setTypeAlert(dto.getTypeAlert().trim());
        alert.setDescriptionAlert(dto.getDescriptionAlert().trim());
        alert.setPriorityLevelAlert(normalizar(dto.getPriorityLevelAlert(), PRIORIDADES, "nivel de prioridad"));
        alert.setKwhDetected(dto.getKwhDetected());

        // Si no se envía el estado: una alerta nueva queda "Pendiente" y una existente conserva el suyo
        String estado = dto.getStatusAlert() != null
                ? normalizar(dto.getStatusAlert(), ESTADOS, "estado")
                : (alert.getStatusAlert() != null ? alert.getStatusAlert() : "Pendiente");
        aplicarEstado(alert, estado);
    }

    private AlertDTOList toDTO(Alert alert) {
        AlertDTOList dto = new AlertDTOList();
        dto.setIdAlert(alert.getIdAlert());
        dto.setIdDepartment(alert.getDepartment().getIdDepartment());
        dto.setNameDepartment(alert.getDepartment().getNameDepartment());
        dto.setIdEquipment(alert.getEquipment().getIdEquipment());
        dto.setNameEquipment(alert.getEquipment().getNameEquipment());
        dto.setDateTimeAlert(alert.getDateTimeAlert());
        dto.setTypeAlert(alert.getTypeAlert());
        dto.setDescriptionAlert(alert.getDescriptionAlert());
        dto.setPriorityLevelAlert(alert.getPriorityLevelAlert());
        dto.setKwhDetected(alert.getKwhDetected());
        dto.setStatusAlert(alert.getStatusAlert());
        dto.setDateTimeResolution(alert.getDateTimeResolution());
        return dto;
    }
}