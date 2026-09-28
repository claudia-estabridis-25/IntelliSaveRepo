package pe.edu.upc.intellisaveapp.dtos;

public class EquipmentDTOList {
    private Long idEquipment;
    private Long idDepartment;
    private String nameEquipment;
    private String categoryEquipment;
    private String statusEquipment;

    public Long getIdEquipment() {
        return idEquipment;
    }

    public void setIdEquipment(Long idEquipment) {
        this.idEquipment = idEquipment;
    }

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public String getNameEquipment() {
        return nameEquipment;
    }

    public void setNameEquipment(String nameEquipment) {
        this.nameEquipment = nameEquipment;
    }

    public String getCategoryEquipment() {
        return categoryEquipment;
    }

    public void setCategoryEquipment(String categoryEquipment) {
        this.categoryEquipment = categoryEquipment;
    }

    public String getStatusEquipment() {
        return statusEquipment;
    }

    public void setStatusEquipment(String statusEquipment) {
        this.statusEquipment = statusEquipment;
    }
}
