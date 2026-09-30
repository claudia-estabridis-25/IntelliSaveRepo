package pe.edu.upc.intellisaveapp.dtos;

public class DepartmentConsumptionDTO {
    private Long idDepartment;
    private String nameDepartment;
    private Long totalRecords;
    private Double totalKwh;
    private Double totalCost;

    public DepartmentConsumptionDTO() {
    }

    public DepartmentConsumptionDTO(Long idDepartment, String nameDepartment, Long totalRecords,
                                    Double totalKwh, Double totalCost) {
        this.idDepartment = idDepartment;
        this.nameDepartment = nameDepartment;
        this.totalRecords = totalRecords;
        this.totalKwh = totalKwh;
        this.totalCost = totalCost;
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

    public Long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Double getTotalKwh() {
        return totalKwh;
    }

    public void setTotalKwh(Double totalKwh) {
        this.totalKwh = totalKwh;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}