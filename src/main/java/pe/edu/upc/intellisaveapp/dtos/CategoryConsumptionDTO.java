package pe.edu.upc.intellisaveapp.dtos;

public class CategoryConsumptionDTO {
    private String categoryEquipment;
    private Long equipmentCount;
    private Long totalRecords;
    private Double totalKwh;
    private Double totalCost;

    public CategoryConsumptionDTO() {
    }

    public CategoryConsumptionDTO(String categoryEquipment, Long equipmentCount, Long totalRecords,
                                  Double totalKwh, Double totalCost) {
        this.categoryEquipment = categoryEquipment;
        this.equipmentCount = equipmentCount;
        this.totalRecords = totalRecords;
        this.totalKwh = totalKwh;
        this.totalCost = totalCost;
    }

    public String getCategoryEquipment() {
        return categoryEquipment;
    }

    public void setCategoryEquipment(String categoryEquipment) {
        this.categoryEquipment = categoryEquipment;
    }

    public Long getEquipmentCount() {
        return equipmentCount;
    }

    public void setEquipmentCount(Long equipmentCount) {
        this.equipmentCount = equipmentCount;
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
