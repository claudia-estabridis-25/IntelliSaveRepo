package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "predictions")
public class ConsumptionPrediction { //FALTA CRUD Y QUERIES
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrediction; //PK

    @ManyToOne
    @JoinColumn(name = "idDepartment", nullable = false)
    private Department department; //FK

    @ManyToOne
    @JoinColumn(name = "idEquipment")   // Opcional: vacío = predicción de toda el área
    private Equipment equipment; //FK

    @Column(name = "generationDatePrediction", nullable = false)
    private LocalDate generationDatePrediction; //Fecha en que se generó la predicción

    @Column(name = "initialDatePrediction", nullable = false)
    private LocalDate initialDatePrediction; //Fecha de inicio de la predicción (intervalo)

    @Column(name = "endDatePrediction", nullable = false)
    private LocalDate endDatePrediction; //Fecha de fin de la predicción (intervalo)

    @Column(name = "kwhPrediction", nullable = false)
    private double kwhPrediction; //Consumo kWh predicho

    @Column(name = "modelAI", length = 30, nullable = false)
    private String modelAI;

    @Column(name = "confidenceLevelAI", nullable = false)
    private double confidenceLevelAI;

    @Column(name = "descriptionPrediction", length = 150, nullable = false)
    private String descriptionPrediction;

    @Column(name = "costPrediction", nullable = false)
    private double costPrediction;

    @Column(name = "statusPrediction", length = 20, nullable = false)
    private String statusPrediction; //En curso, Cumplida, Errada


    public ConsumptionPrediction() {
    }

    public ConsumptionPrediction(Long idPrediction, Department department, Equipment equipment,
                                 LocalDate generationDatePrediction, LocalDate initialDatePrediction,
                                 LocalDate endDatePrediction, double kwhPrediction, String modelAI,
                                 double confidenceLevelAI, String descriptionPrediction, double costPrediction,
                                 String statusPrediction) {
        this.idPrediction = idPrediction;
        this.department = department;
        this.equipment = equipment;
        this.generationDatePrediction = generationDatePrediction;
        this.initialDatePrediction = initialDatePrediction;
        this.endDatePrediction = endDatePrediction;
        this.kwhPrediction = kwhPrediction;
        this.modelAI = modelAI;
        this.confidenceLevelAI = confidenceLevelAI;
        this.descriptionPrediction = descriptionPrediction;
        this.costPrediction = costPrediction;
        this.statusPrediction = statusPrediction;
    }

    public Long getIdPrediction() {
        return idPrediction;
    }

    public void setIdPrediction(Long idPrediction) {
        this.idPrediction = idPrediction;
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

    public double getKwhPrediction() {
        return kwhPrediction;
    }

    public void setKwhPrediction(double kwhPrediction) {
        this.kwhPrediction = kwhPrediction;
    }

    public String getModelAI() {
        return modelAI;
    }

    public void setModelAI(String modelAI) {
        this.modelAI = modelAI;
    }

    public double getConfidenceLevelAI() {
        return confidenceLevelAI;
    }

    public void setConfidenceLevelAI(double confidenceLevelAI) {
        this.confidenceLevelAI = confidenceLevelAI;
    }

    public String getDescriptionPrediction() {
        return descriptionPrediction;
    }

    public void setDescriptionPrediction(String descriptionPrediction) { this.descriptionPrediction = descriptionPrediction;}

    public double getCostPrediction() {
        return costPrediction;
    }

    public void setCostPrediction(double costPrediction) {
        this.costPrediction = costPrediction;
    }

    public String getStatusPrediction() { return statusPrediction; }

    public void setStatusPrediction(String statusPrediction) { this.statusPrediction = statusPrediction; }
}
