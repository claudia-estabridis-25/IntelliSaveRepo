package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDateTime;

public class ConsumptionRecordDTOList {
    private Long idConsumptionRecord;
    private Long idEquipment;
    private Long idTariff;
    private LocalDateTime dateTimeRecord;
    private Double hoursOfUse;
    private Double kwhConsumption;
    private Double costTotal;
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

    public Double getKwhConsumption() {
        return kwhConsumption;
    }

    public void setKwhConsumption(Double kwhConsumption) {
        this.kwhConsumption = kwhConsumption;
    }

    public Double getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(Double costTotal) {
        this.costTotal = costTotal;
    }

    public String getObservationRecord() {
        return observationRecord;
    }

    public void setObservationRecord(String observationRecord) {
        this.observationRecord = observationRecord;
    }
}