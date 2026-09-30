package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.intellisaveapp.dtos.PredictionDTOList;
import pe.edu.upc.intellisaveapp.dtos.PredictionRequestDTO;
import pe.edu.upc.intellisaveapp.dtos.PredictionResultDTO;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionPredictionService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class ConsumptionPredictionController {
    private final IConsumptionPredictionService pS;
    private final IDepartmentService dS;
    private final IEquipmentService eS;

    public ConsumptionPredictionController(IConsumptionPredictionService pS,
                                           IDepartmentService dS,
                                           IEquipmentService eS) {
        this.pS = pS;
        this.dS = dS;
        this.eS = eS;
    }

    // HU013: generar la predicción de un área o de un equipo
    @PostMapping
    public ResponseEntity<PredictionResultDTO> generar(@Valid @RequestBody PredictionRequestDTO dto) {
        LocalDate inicio = dto.getInitialDatePrediction();
        LocalDate fin = dto.getEndDatePrediction();

        if (inicio.isAfter(fin)) {
            throw new BusinessRuleException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) > 90) {
            throw new BusinessRuleException("El periodo a predecir no puede superar los 90 días");
        }

        Equipment equipment = null;
        Department department;

        if (dto.getIdEquipment() != null) {
            // Predicción por equipo: el área se toma del equipo
            equipment = eS.listById(dto.getIdEquipment())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("No existe un equipo con el id: " + dto.getIdEquipment())
                    );
            department = equipment.getDepartment();

            if (dto.getIdDepartment() != null && !dto.getIdDepartment().equals(department.getIdDepartment())) {
                throw new BusinessRuleException("El equipo no pertenece al área indicada");
            }
        } else if (dto.getIdDepartment() != null) {
            // Predicción por área completa
            department = dS.listById(dto.getIdDepartment())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("No existe un área con el id: " + dto.getIdDepartment())
                    );
        } else {
            throw new BusinessRuleException("Debe indicar un área o un equipo para generar la predicción");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(pS.generate(department, equipment, inicio, fin));
    }

    // HU051: historial de predicciones, con filtros opcionales
    // (antes de listar, el service evalúa automáticamente las predicciones terminadas: HU055)
    @GetMapping
    public ResponseEntity<List<PredictionDTOList>> historial(
            @RequestParam(required = false) Long idDepartment,
            @RequestParam(required = false) Long idEquipment,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(pS.list(idDepartment, idEquipment, status));
    }

    // Detalle de una predicción
    @GetMapping("/{id}")
    public ResponseEntity<PredictionDTOList> listarPorId(@PathVariable Long id) {
        PredictionDTOList dto = pS.listById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una predicción con el id: " + id));
        return ResponseEntity.ok(dto);
    }

    // HU055: evaluar a mano las predicciones terminadas
    @PostMapping("/evaluate")
    public ResponseEntity<List<PredictionDTOList>> evaluar() {
        return ResponseEntity.ok(pS.evaluateFinishedPredictions());
    }
}