package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlert; //PK

    @ManyToOne
    @JoinColumn(name = "idDepartment", nullable = false)
    private Department department; //FK

    @ManyToOne
    @JoinColumn(name = "idEquipment", nullable = false)
    private Equipment equipment; //FK

    @Column(name = "dateTimeAlert", nullable = false)
    private LocalDateTime dateTimeAlert;

    @Column(name = "typeAlert", length = 20, nullable = false)
    private String typeAlert;

    @Column(name = "descriptionAlert", length = 90, nullable = false)
    private String descriptionAlert;

    @Column(name = "priorityLevelAlert", length = 20, nullable = false)
    private String priorityLevelAlert;

    @Column(name = "kwhDetected", nullable = false)
    private double kwhDetected; //Consumo (en kWh) detectado que generó la alerta

    @Column(name = "statusAlert", length = 20, nullable = false)
    private String statusAlert; //Pendiente, En revisión, Atendida, Descartada

    @Column(name = "dateTimeResolution") //Puede ser nulo: se llena cuando la alerta pasa a "Atendida" o "Descartada"
    private LocalDateTime dateTimeResolution;

    public Alert() {
    }

    public Alert(Long idAlert, Department department, Equipment equipment, LocalDateTime dateTimeAlert,
                 String typeAlert, String descriptionAlert, String priorityLevelAlert, double kwhDetected,
                 String statusAlert) {
        this.idAlert = idAlert;
        this.department = department;
        this.equipment = equipment;
        this.dateTimeAlert = dateTimeAlert;
        this.typeAlert = typeAlert;
        this.descriptionAlert = descriptionAlert;
        this.priorityLevelAlert = priorityLevelAlert;
        this.kwhDetected = kwhDetected;
        this.statusAlert = statusAlert;
    }

    public Long getIdAlert() {
        return idAlert;
    }

    public void setIdAlert(Long idAlert) {
        this.idAlert = idAlert;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public LocalDateTime getDateTimeAlert() {
        return dateTimeAlert;
    }

    public void setDateTimeAlert(LocalDateTime dateTimeAlert) {
        this.dateTimeAlert = dateTimeAlert;
    }

    public String getTypeAlert() {
        return typeAlert;
    }

    public void setTypeAlert(String typeAlert) {
        this.typeAlert = typeAlert;
    }

    public String getDescriptionAlert() {
        return descriptionAlert;
    }

    public void setDescriptionAlert(String descriptionAlert) {
        this.descriptionAlert = descriptionAlert;
    }

    public String getPriorityLevelAlert() {
        return priorityLevelAlert;
    }

    public void setPriorityLevelAlert(String priorityLevelAlert) {
        this.priorityLevelAlert = priorityLevelAlert;
    }

    public double getKwhDetected() {
        return kwhDetected;
    }

    public void setKwhDetected(double kwhDetected) {
        this.kwhDetected = kwhDetected;
    }

    public String getStatusAlert() {
        return statusAlert;
    }

    public void setStatusAlert(String statusAlert) {
        this.statusAlert = statusAlert;
    }

    public LocalDateTime getDateTimeResolution() {
        return dateTimeResolution;
    }

    public void setDateTimeResolution(LocalDateTime dateTimeResolution) {
        this.dateTimeResolution = dateTimeResolution;
    }
}