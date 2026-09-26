package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumption_records")
public class ConsumptionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsumptionRecord;

    @ManyToOne
    @JoinColumn(name = "idEquipment", nullable = false)
    private Equipment equipment;


    @Column(name = "idTariff", nullable = false)
    private Long idTariff;

    @Column(name = "dateTimeRecord", nullable = false)
    private LocalDateTime dateTimeRecord;

    @Column(name = "hoursOfUse", nullable = false)
    private Double hoursOfUse;


    @Column(name = "kwhConsumption", nullable = false)
    private Double kwhConsumption;

    @Column(name = "costTotal", nullable = false)
    private Double costTotal;

    @Column(name = "observationRecord", length = 90)
    private String observationRecord;

    public ConsumptionRecord() {
    }

    public ConsumptionRecord(Long idConsumptionRecord, Equipment equipment, Long idTariff,
                             LocalDateTime dateTimeRecord, Double hoursOfUse, Double kwhConsumption,
                             Double costTotal, String observationRecord) {
        this.idConsumptionRecord = idConsumptionRecord;
        this.equipment = equipment;
        this.idTariff = idTariff;
        this.dateTimeRecord = dateTimeRecord;
        this.hoursOfUse = hoursOfUse;
        this.kwhConsumption = kwhConsumption;
        this.costTotal = costTotal;
        this.observationRecord = observationRecord;
    }

    public Long getIdConsumptionRecord() {
        return idConsumptionRecord;
    }

    public void setIdConsumptionRecord(Long idConsumptionRecord) {
        this.idConsumptionRecord = idConsumptionRecord;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
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