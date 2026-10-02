package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

// El consumo (kWh) y las emisiones (kg CO₂) no se envían: se calculan con los registros de consumo del área
public class CarbonFootprintDTOInsert {
    private Long idFootprint;
    @NotNull(message = "El id del área es obligatorio")
    private Long idDepartment;
    @NotBlank(message = "El periodo es obligatorio")
    private String timePeriod; // Mensual, Bimestral, Trimestral, Semestral, Anual
    private LocalDate calculationDate; // Opcional: vacío = hoy. Es el último día del periodo calculado
    @NotNull(message = "El factor de emisión es obligatorio")
    @Positive(message = "El factor de emisión debe ser mayor a 0")
    private Double emissionFactor; // kg CO₂ por kWh. Ejm: 0.52

    public Long getIdFootprint() {
        return idFootprint;
    }

    public void setIdFootprint(Long idFootprint) {
        this.idFootprint = idFootprint;
    }

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public Double getEmissionFactor() {
        return emissionFactor;
    }

    public void setEmissionFactor(Double emissionFactor) {
        this.emissionFactor = emissionFactor;
    }
}