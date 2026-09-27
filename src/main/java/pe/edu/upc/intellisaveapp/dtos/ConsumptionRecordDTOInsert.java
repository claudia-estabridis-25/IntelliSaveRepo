package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ConsumptionRecordDTOInsert {
    private Long idConsumptionRecord;
    @NotNull(message = "El id del equipo relacionado es obligatorio")
    private Long idEquipment;
    @NotNull(message = "El id de la tarifa relacionada es obligatorio")
    private Long idTariff;
    @NotNull(message = "La fecha y hora del registro son obligatorias")
    private LocalDateTime dateTimeRecord;
    @NotNull(message = "Las horas de uso son obligatorias")
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