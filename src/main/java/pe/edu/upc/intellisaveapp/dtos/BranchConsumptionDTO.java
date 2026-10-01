package pe.edu.upc.intellisaveapp.dtos;

//Consumo total (kWh) y Costo total (S/) de cada sede de una empresa
public class BranchConsumptionDTO {
    private Long idBranch;
    private String nameBranch;
    private Long totalRecords; //cantidad total de registros
    private Double totalKwh; //consumo de energía eléctrica (en kWh)
    private Double totalCost; //costo total por el consumo (en soles)

    public BranchConsumptionDTO() {
    }

    public BranchConsumptionDTO(Long idBranch, String nameBranch, Long totalRecords, Double totalKwh,
                                Double totalCost) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.totalRecords = totalRecords;
        this.totalKwh = totalKwh;
        this.totalCost = totalCost;
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
