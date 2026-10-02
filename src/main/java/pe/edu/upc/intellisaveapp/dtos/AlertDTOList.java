package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDateTime;

public class AlertDTOList {
    private Long idAlert;
    private Long idDepartment;
    private String nameDepartment;
    private Long idEquipment;
    private String nameEquipment;
    private LocalDateTime dateTimeAlert;
    private String typeAlert;
    private String descriptionAlert;
    private String priorityLevelAlert;
    private Double kwhDetected;
    private String statusAlert;
    private LocalDateTime dateTimeResolution;

    public Long getIdAlert() {
        return idAlert;
    }

    public void setIdAlert(Long idAlert) {
        this.idAlert = idAlert;
    }

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public String getNameDepartment() {
        return nameDepartment;
    }

    public void setNameDepartment(String nameDepartment) {
        this.nameDepartment = nameDepartment;
    }

    public Long getIdEquipment() {
        return idEquipment;
    }

    public void setIdEquipment(Long idEquipment) {
        this.idEquipment = idEquipment;
    }

    public String getNameEquipment() {
        return nameEquipment;
    }

    public void setNameEquipment(String nameEquipment) {
        this.nameEquipment = nameEquipment;
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

    public Double getKwhDetected() {
        return kwhDetected;
    }

    public void setKwhDetected(Double kwhDetected) {
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