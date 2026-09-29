package pe.edu.upc.intellisaveapp.dtos;

public class ElevatedConsumptionDTO {
    private Long idEquipment;
    private String nameEquipment;
    private Double totalKwhConsumption;
    private Double departmentAverageKwh;
    private Boolean elevatedConsumption;

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

    public Double getTotalKwhConsumption() {
        return totalKwhConsumption;
    }

    public void setTotalKwhConsumption(Double totalKwhConsumption) {
        this.totalKwhConsumption = totalKwhConsumption;
    }

    public Double getDepartmentAverageKwh() {
        return departmentAverageKwh;
    }

    public void setDepartmentAverageKwh(Double departmentAverageKwh) {
        this.departmentAverageKwh = departmentAverageKwh;
    }

    public Boolean getElevatedConsumption() {
        return elevatedConsumption;
    }

    public void setElevatedConsumption(Boolean elevatedConsumption) {
        this.elevatedConsumption = elevatedConsumption;
    }
}