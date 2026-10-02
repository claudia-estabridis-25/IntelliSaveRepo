package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDate;

public class CarbonFootprintDTOList {
    private Long idFootprint;
    private Long idDepartment;
    private String nameDepartment;
    private Long idBranch;
    private String nameBranch;
    private String timePeriod;
    private LocalDate periodStartDate; // Primer día del periodo calculado
    private LocalDate calculationDate; // Último día del periodo calculado
    private Double emissionFactor;
    private Double kwhTotalConsumption;
    private Double co2Emissions;

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

    public String getNameDepartment() {
        return nameDepartment;
    }

    public void setNameDepartment(String nameDepartment) {
        this.nameDepartment = nameDepartment;
    }

    public Long getIdBranch() {
        return idBranch;
    }

    public void setIdBranch(Long idBranch) {
        this.idBranch = idBranch;
    }

    public String getNameBranch() {
        return nameBranch;
    }

    public void setNameBranch(String nameBranch) {
        this.nameBranch = nameBranch;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
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

    public Double getKwhTotalConsumption() {
        return kwhTotalConsumption;
    }

    public void setKwhTotalConsumption(Double kwhTotalConsumption) {
        this.kwhTotalConsumption = kwhTotalConsumption;
    }

    public Double getCo2Emissions() {
        return co2Emissions;
    }

    public void setCo2Emissions(Double co2Emissions) {
        this.co2Emissions = co2Emissions;
    }
}