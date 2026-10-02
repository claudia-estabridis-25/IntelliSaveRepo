package pe.edu.upc.intellisaveapp.dtos;

//Emisiones totales de CO₂ por área
public class DepartmentEmissionDTO {
    private Long idDepartment;
    private String nameDepartment;
    private Long totalCalculations;
    private Double totalKwh;
    private Double totalCo2Emissions; //en kilogramos

    public DepartmentEmissionDTO() {
    }

    public DepartmentEmissionDTO(Long idDepartment, String nameDepartment, Long totalCalculations,
                                 Double totalKwh, Double totalCo2Emissions) {
        this.idDepartment = idDepartment;
        this.nameDepartment = nameDepartment;
        this.totalCalculations = totalCalculations;
        this.totalKwh = totalKwh;
        this.totalCo2Emissions = totalCo2Emissions;
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

    public Long getTotalCalculations() {
        return totalCalculations;
    }

    public void setTotalCalculations(Long totalCalculations) {
        this.totalCalculations = totalCalculations;
    }

    public Double getTotalKwh() {
        return totalKwh;
    }

    public void setTotalKwh(Double totalKwh) {
        this.totalKwh = totalKwh;
    }

    public Double getTotalCo2Emissions() {
        return totalCo2Emissions;
    }

    public void setTotalCo2Emissions(Double totalCo2Emissions) {
        this.totalCo2Emissions = totalCo2Emissions;
    }
}