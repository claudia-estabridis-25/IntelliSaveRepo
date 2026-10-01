package pe.edu.upc.intellisaveapp.dtos;

//Potencia instalada total (W) por sede (suma la potencia en watts de todos los equipos que tiene una sede)
public class BranchPowerDTO {
    private Long idBranch;
    private String nameBranch;
    private Long totalEquipments; //cantidad total de equipos en la sede
    private Double totalWatts; //cantidad total de potencia (en W) de la sede

    public BranchPowerDTO() {
    }

    public BranchPowerDTO(Long idBranch, String nameBranch, Long totalEquipments, Double totalWatts) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.totalEquipments = totalEquipments;
        this.totalWatts = totalWatts;
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

    public Long getTotalEquipments() {
        return totalEquipments;
    }

    public void setTotalEquipments(Long totalEquipments) {
        this.totalEquipments = totalEquipments;
    }

    public Double getTotalWatts() {
        return totalWatts;
    }

    public void setTotalWatts(Double totalWatts) {
        this.totalWatts = totalWatts;
    }
}
