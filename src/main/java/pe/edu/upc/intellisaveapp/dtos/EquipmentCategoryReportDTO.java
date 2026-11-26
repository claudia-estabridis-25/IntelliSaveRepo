package pe.edu.upc.intellisaveapp.dtos;

public class EquipmentCategoryReportDTO {
    private Long idCompany;
    private String nameCompany;
    private String categoryEquipment;
    private Long totalEquipments;
    private Long activeEquipments;
    private Long inactiveEquipments;
    private Double activePowerWatts;
    private Double totalKwh;
    private Double totalCost;

    public EquipmentCategoryReportDTO() {
    }

    public EquipmentCategoryReportDTO(Long idCompany, String nameCompany, String categoryEquipment,
                                      Long totalEquipments, Long activeEquipments, Long inactiveEquipments,
                                      Double activePowerWatts, Double totalKwh, Double totalCost) {
        this.idCompany = idCompany;
        this.nameCompany = nameCompany;
        this.categoryEquipment = categoryEquipment;
        this.totalEquipments = totalEquipments;
        this.activeEquipments = activeEquipments;
        this.inactiveEquipments = inactiveEquipments;
        this.activePowerWatts = activePowerWatts;
        this.totalKwh = totalKwh;
        this.totalCost = totalCost;
    }

    public Long getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(Long idCompany) {
        this.idCompany = idCompany;
    }

    public String getNameCompany() {
        return nameCompany;
    }

    public void setNameCompany(String nameCompany) {
        this.nameCompany = nameCompany;
    }

    public String getCategoryEquipment() {
        return categoryEquipment;
    }

    public void setCategoryEquipment(String categoryEquipment) {
        this.categoryEquipment = categoryEquipment;
    }

    public Long getTotalEquipments() {
        return totalEquipments;
    }

    public void setTotalEquipments(Long totalEquipments) {
        this.totalEquipments = totalEquipments;
    }

    public Long getActiveEquipments() {
        return activeEquipments;
    }

    public void setActiveEquipments(Long activeEquipments) {
        this.activeEquipments = activeEquipments;
    }

    public Long getInactiveEquipments() {
        return inactiveEquipments;
    }

    public void setInactiveEquipments(Long inactiveEquipments) {
        this.inactiveEquipments = inactiveEquipments;
    }

    public Double getActivePowerWatts() {
        return activePowerWatts;
    }

    public void setActivePowerWatts(Double activePowerWatts) {
        this.activePowerWatts = activePowerWatts;
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