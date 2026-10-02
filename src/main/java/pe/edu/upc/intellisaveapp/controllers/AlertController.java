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
import pe.edu.upc.intellisaveapp.servicesinterfaces.IAlertService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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

    public AlertController(IAlertService aS, IDepartmentService dS, IEquipmentService eS) {
        this.aS = aS;
        this.dS = dS;
        this.eS = eS;
    }

    // HU16: listar alertas, con filtros opcionales, ordenadas por fecha (más recientes primero) y prioridad
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLOYEE')")
    public ResponseEntity<List<AlertDTOList>> listar(
            @RequestParam(required = false) Long idDepartment,
            @RequestParam(required = false) Long idEquipment,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {

        List<AlertDTOList> lista = aS.list()
                .stream()
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

        List<AlertDTOList> lista = aS.history(type, desde, hasta)
                .stream()
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
        List<AlertDTOList> lista = aS.listByStatus(status)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // Consulta simple: cantidad de alertas por estado
    @GetMapping("/count-by-status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertCountDTO>> contarPorEstado() {
        return ResponseEntity.ok(aS.countByStatus());
    }

    // Consulta simple: cantidad de alertas por nivel de prioridad
    @GetMapping("/count-by-priority")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertCountDTO>> contarPorPrioridad() {
        return ResponseEntity.ok(aS.countByPriority());
    }

    // Consulta nativa con JOIN: cantidad de alertas por área, tipo y estado
    @GetMapping("/count-by-department")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<AlertDepartmentCountDTO>> contarPorArea() {
        return ResponseEntity.ok(aS.countByDepartment());
    }

    // Registrar alerta manualmente
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<AlertDTOList> registrar(@Valid @RequestBody AlertDTOInsert dto) {
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

    private Alert buscarAlerta(Long id) {
        return aS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una alerta con el id: " + id)
                );
    }

    private Department buscarArea(Long idDepartment) {
        return dS.listById(idDepartment)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe un área con el id: " + idDepartment)
                );
    }

    private Equipment buscarEquipo(Long idEquipment) {
        return eS.listById(idEquipment)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe un equipo con el id: " + idEquipment)
                );
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