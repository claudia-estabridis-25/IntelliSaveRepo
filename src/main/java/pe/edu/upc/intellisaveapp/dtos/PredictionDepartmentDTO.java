package pe.edu.upc.intellisaveapp.dtos;

public class PredictionDepartmentDTO {
    private Long idDepartment;
    private String nameDepartment;
    private String statusPrediction;
    private Long totalPredictions;
    private Double totalKwhPrediction;
    private Double averageConfidence;

    public PredictionDepartmentDTO() {
    }

    public PredictionDepartmentDTO(Long idDepartment, String nameDepartment, String statusPrediction, Long totalPredictions, Double totalKwhPrediction, Double averageConfidence) {
        this.idDepartment = idDepartment;
        this.nameDepartment = nameDepartment;
        this.statusPrediction = statusPrediction;
        this.totalPredictions = totalPredictions;
        this.totalKwhPrediction = totalKwhPrediction;
        this.averageConfidence = averageConfidence;
    }

    public Long getIdDepartment() { return idDepartment; }
    public void setIdDepartment(Long idDepartment) { this.idDepartment = idDepartment; }

    public String getNameDepartment() { return nameDepartment; }
    public void setNameDepartment(String nameDepartment) { this.nameDepartment = nameDepartment; }

    public String getStatusPrediction() { return statusPrediction; }
    public void setStatusPrediction(String statusPrediction) { this.statusPrediction = statusPrediction; }

    public Long getTotalPredictions() { return totalPredictions; }
    public void setTotalPredictions(Long totalPredictions) { this.totalPredictions = totalPredictions; }

    public Double getTotalKwhPrediction() { return totalKwhPrediction; }
    public void setTotalKwhPrediction(Double totalKwhPrediction) { this.totalKwhPrediction = totalKwhPrediction; }

    public Double getAverageConfidence() { return averageConfidence; }
    public void setAverageConfidence(Double averageConfidence) { this.averageConfidence = averageConfidence; }
}