package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class PredictionRequestDTO {
    private Long idDepartment;   // Opcional si se envía idEquipment
    private Long idEquipment;    // Opcional: vacío = predicción de toda el área
    @NotNull(message = "La fecha de inicio del periodo a predecir es obligatoria")
    private LocalDate initialDatePrediction;
    @NotNull(message = "La fecha de fin del periodo a predecir es obligatoria")
    private LocalDate endDatePrediction;

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public Long getIdEquipment() {
        return idEquipment;
    }

    public void setIdEquipment(Long idEquipment) {
        this.idEquipment = idEquipment;
    }

    public LocalDate getInitialDatePrediction() {
        return initialDatePrediction;
    }

    public void setInitialDatePrediction(LocalDate initialDatePrediction) {
        this.initialDatePrediction = initialDatePrediction;
    }

    public LocalDate getEndDatePrediction() {
        return endDatePrediction;
    }

    public void setEndDatePrediction(LocalDate endDatePrediction) {
        this.endDatePrediction = endDatePrediction;
    }
}