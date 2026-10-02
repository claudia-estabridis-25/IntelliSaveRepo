package pe.edu.upc.intellisaveapp.dtos;

public class AlertDepartmentCountDTO {
    private Long idDepartment;
    private String nameDepartment;
    private String typeAlert;
    private String statusAlert;
    private Long totalAlerts;

    public AlertDepartmentCountDTO() {
    }

    public AlertDepartmentCountDTO(Long idDepartment, String nameDepartment, String typeAlert, String statusAlert, Long totalAlerts) {
        this.idDepartment = idDepartment;
        this.nameDepartment = nameDepartment;
        this.typeAlert = typeAlert;
        this.statusAlert = statusAlert;
        this.totalAlerts = totalAlerts;
    }

    public Long getIdDepartment() { return idDepartment; }
    public void setIdDepartment(Long idDepartment) { this.idDepartment = idDepartment; }

    public String getNameDepartment() { return nameDepartment; }
    public void setNameDepartment(String nameDepartment) { this.nameDepartment = nameDepartment; }

    public String getTypeAlert() { return typeAlert; }
    public void setTypeAlert(String typeAlert) { this.typeAlert = typeAlert; }

    public String getStatusAlert() { return statusAlert; }
    public void setStatusAlert(String statusAlert) { this.statusAlert = statusAlert; }

    public Long getTotalAlerts() { return totalAlerts; }
    public void setTotalAlerts(Long totalAlerts) { this.totalAlerts = totalAlerts; }
}