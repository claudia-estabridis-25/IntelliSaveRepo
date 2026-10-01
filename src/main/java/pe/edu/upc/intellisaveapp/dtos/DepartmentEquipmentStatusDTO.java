package pe.edu.upc.intellisaveapp.dtos;

//Cantidad de equipos activos e inactivos por cada área de cada sede
public class DepartmentEquipmentStatusDTO {
    private Long idBranch;
    private String nameBranch;
    private Long idDepartment;
    private String nameDepartment;
    private Long activeEquipments;
    private Long inactiveEquipments;

    public DepartmentEquipmentStatusDTO() {
    }

    public DepartmentEquipmentStatusDTO(Long idBranch, String nameBranch, Long idDepartment, String nameDepartment,
                                        Long activeEquipments, Long inactiveEquipments) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.idDepartment = idDepartment;
        this.nameDepartment = nameDepartment;
        this.activeEquipments = activeEquipments;
        this.inactiveEquipments = inactiveEquipments;
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
}
