package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDate;

public class PredictionDTOList {
    private Long idPrediction;
    private Long idDepartment;
    private String nameDepartment;
    private Long idEquipment;          // null si es predicción de área
    private String nameEquipment;      // null si es predicción de área
    private LocalDate generationDatePrediction;
    private LocalDate initialDatePrediction;
    private LocalDate endDatePrediction;
    private Double kwhPrediction;
    private Double costPrediction;
    private String modelAI;
    private Double confidenceLevelAI;  // en porcentaje (0 a 100)
    private String descriptionPrediction;
    private String statusPrediction;   // En curso, Cumplida, Errada
    private Double realKwh;            // solo cuando ya fue evaluada
    private Double deviationPercent;   // % de diferencia entre lo real y lo predicho

    public Long getIdPrediction() {
        return idPrediction;
    }

    public void setIdPrediction(Long idPrediction) {
        this.idPrediction = idPrediction;
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

    public LocalDate getGenerationDatePrediction() {
        return generationDatePrediction;
    }

    public void setGenerationDatePrediction(LocalDate generationDatePrediction) {
        this.generationDatePrediction = generationDatePrediction;
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

    public Double getKwhPrediction() {
        return kwhPrediction;
    }

    public void setKwhPrediction(Double kwhPrediction) {
        this.kwhPrediction = kwhPrediction;
    }

    public Double getCostPrediction() {
        return costPrediction;
    }

    public void setCostPrediction(Double costPrediction) {
        this.costPrediction = costPrediction;
    }

    public String getModelAI() {
        return modelAI;
    }

    public void setModelAI(String modelAI) {
        this.modelAI = modelAI;
    }

    public Double getConfidenceLevelAI() {
        return confidenceLevelAI;
    }

    public void setConfidenceLevelAI(Double confidenceLevelAI) {
        this.confidenceLevelAI = confidenceLevelAI;
    }

    public String getDescriptionPrediction() {
        return descriptionPrediction;
    }

    public void setDescriptionPrediction(String descriptionPrediction) {
        this.descriptionPrediction = descriptionPrediction;
    }

    public String getStatusPrediction() {
        return statusPrediction;
    }

    public void setStatusPrediction(String statusPrediction) {
        this.statusPrediction = statusPrediction;
    }

    public Double getRealKwh() {
        return realKwh;
    }

    public void setRealKwh(Double realKwh) {
        this.realKwh = realKwh;
    }

    public Double getDeviationPercent() {
        return deviationPercent;
    }

    public void setDeviationPercent(Double deviationPercent) {
        this.deviationPercent = deviationPercent;
    }
}