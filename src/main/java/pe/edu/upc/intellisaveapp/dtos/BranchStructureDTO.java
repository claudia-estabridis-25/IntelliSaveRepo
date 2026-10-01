package pe.edu.upc.intellisaveapp.dtos;

//Cantidad de áreas y de equipos por sede
public class BranchStructureDTO {
    private Long idBranch;
    private String nameBranch;
    private Long totalDepartments; //cantidad de áreas
    private Long totalEquipments; //cantidad de equipos

    public BranchStructureDTO() {
    }

    public BranchStructureDTO(Long idBranch, String nameBranch, Long totalDepartments, Long totalEquipments) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.totalDepartments = totalDepartments;
        this.totalEquipments = totalEquipments;
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

    public Long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(Long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public Long getTotalEquipments() {
        return totalEquipments;
    }

    public void setTotalEquipments(Long totalEquipments) {
        this.totalEquipments = totalEquipments;
    }
}
