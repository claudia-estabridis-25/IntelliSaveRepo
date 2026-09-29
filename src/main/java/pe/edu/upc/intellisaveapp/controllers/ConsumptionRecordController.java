package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/consumption-records")
public class ConsumptionRecordController {
    private final IConsumptionRecordService crS;
    private final IEquipmentService eS;
    private final ModelMapper mP;

    private static final Double PRECIO_KWH_TEMPORAL = 0.75;

    public ConsumptionRecordController(IConsumptionRecordService crS, IEquipmentService eS, ModelMapper mP) {
        this.crS = crS;
        this.eS = eS;
        this.mP = mP;
    }

    @GetMapping
    public ResponseEntity<List<ConsumptionRecordDTOList>> listar() {
        List<ConsumptionRecordDTOList> lista = crS.list()
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<ConsumptionRecordDTOList> registrar(@Valid @RequestBody ConsumptionRecordDTOInsert dto) {
        Equipment equipment = eS.listById(dto.getIdEquipment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + dto.getIdEquipment()
                        )
                );

        validarEquipoActivo(equipment);

        ConsumptionRecord cr = mP.map(dto, ConsumptionRecord.class);
        cr.setIdConsumptionRecord(null); // Un POST siempre crea un registro nuevo
        cr.setEquipment(equipment);

        Double kwhConsumption = (equipment.getWattPowerEquipment() * dto.getHoursOfUse()) / 1000;
        cr.setKwhConsumption(kwhConsumption);
        cr.setCostTotal(kwhConsumption * PRECIO_KWH_TEMPORAL);

        crS.insert(cr);

        ConsumptionRecordDTOList responseDTO = mP.map(cr, ConsumptionRecordDTOList.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cr.getIdConsumptionRecord())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @PutMapping
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

        Double kwhConsumption = (equipment.getWattPowerEquipment() * dto.getHoursOfUse()) / 1000;
        cr.setKwhConsumption(kwhConsumption);
        cr.setCostTotal(kwhConsumption * PRECIO_KWH_TEMPORAL);

        crS.update(cr);

        ConsumptionRecordDTOList responseDTO = mP.map(cr, ConsumptionRecordDTOList.class);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
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

    @GetMapping("/department/{idDepartment}")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorArea(@PathVariable Long idDepartment) {
        List<ConsumptionRecordDTOList> lista = crS.listByDepartment(idDepartment)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/branch/{idBranch}")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorSede(@PathVariable Long idBranch) {
        List<ConsumptionRecordDTOList> lista = crS.listByBranch(idBranch)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/equipment/{idEquipment}")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorEquipo(@PathVariable Long idEquipment) {
        List<ConsumptionRecordDTOList> lista = crS.listByEquipment(idEquipment)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/range")
    public ResponseEntity<List<ConsumptionRecordDTOList>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<ConsumptionRecordDTOList> lista = crS.listByDateRange(desde, hasta)
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/department/{idDepartment}/average")
    public ResponseEntity<Double> promedioKwhPorArea(@PathVariable Long idDepartment) {
        return ResponseEntity.ok(crS.averageKwhByDepartment(idDepartment));
    }

    @GetMapping("/department/{idDepartment}/elevated-consumption")
    public ResponseEntity<List<ElevatedConsumptionDTO>> equiposConsumoElevado(@PathVariable Long idDepartment) {
        // 1. Consumo total (kWh) de cada equipo del área
        Map<Equipment, Double> totalPorEquipo = crS.listByDepartment(idDepartment)
                .stream()
                .collect(Collectors.groupingBy(
                        ConsumptionRecord::getEquipment,
                        Collectors.summingDouble(ConsumptionRecord::getKwhConsumption)
                ));

        if (totalPorEquipo.isEmpty()) {
            return ResponseEntity.ok(List.of());
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

        return ResponseEntity.ok(resultado);
    }

    private void validarEquipoActivo(Equipment equipment) {
        if ("Inactivo".equalsIgnoreCase(equipment.getStatusEquipment())) {
            throw new BusinessRuleException(
                    "El equipo con id " + equipment.getIdEquipment()
                            + " está dado de baja y no puede registrar consumo"
            );
        }
    }
}