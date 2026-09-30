package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class ConsumptionRecordDTOInsert {
    private Long idConsumptionRecord;
    @NotNull(message = "El id del equipo relacionado es obligatorio")
    private Long idEquipment;
    private Long idTariff; // Opcional: si no se envía, se usa la tarifa vigente de la sede
    @NotNull(message = "La fecha y hora del registro son obligatorias")
    private LocalDateTime dateTimeRecord;
    @NotNull(message = "Las horas de uso son obligatorias")
    @Positive(message = "Ingrese un valor de horas de uso válido")
    @DecimalMax(value = "24", message = "Las horas de uso no pueden superar 24 por registro")
    private Double hoursOfUse;
    private String observationRecord;

    public Long getIdConsumptionRecord() {
        return idConsumptionRecord;
    }

    public void setIdConsumptionRecord(Long idConsumptionRecord) {
        this.idConsumptionRecord = idConsumptionRecord;
    }

    public Long getIdEquipment() {
        return idEquipment;
    }

    public void setIdEquipment(Long idEquipment) {
        this.idEquipment = idEquipment;
    }

    public Long getIdTariff() {
        return idTariff;
    }

    public void setIdTariff(Long idTariff) {
        this.idTariff = idTariff;
    }

    public LocalDateTime getDateTimeRecord() {
        return dateTimeRecord;
    }

    public void setDateTimeRecord(LocalDateTime dateTimeRecord) {
        this.dateTimeRecord = dateTimeRecord;
    }

    public Double getHoursOfUse() {
        return hoursOfUse;
    }

    public void setHoursOfUse(Double hoursOfUse) {
        this.hoursOfUse = hoursOfUse;
    }

    public String getObservationRecord() {
        return observationRecord;
    }

    public void setObservationRecord(String observationRecord) {
        this.observationRecord = observationRecord;
    }
}