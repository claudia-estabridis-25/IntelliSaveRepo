package pe.edu.upc.intellisaveapp.dtos;

public class TariffSupplierDTO {
    private Long idBranch;
    private String nameBranch;
    private String supplier;
    private Long totalTariffs;
    private Double averageCostPerKwh;

    public TariffSupplierDTO() {
    }

    public TariffSupplierDTO(Long idBranch, String nameBranch, String supplier, Long totalTariffs, Double averageCostPerKwh) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.supplier = supplier;
        this.totalTariffs = totalTariffs;
        this.averageCostPerKwh = averageCostPerKwh;
    }

    public Long getIdBranch() { return idBranch; }
    public void setIdBranch(Long idBranch) { this.idBranch = idBranch; }

    public String getNameBranch() { return nameBranch; }
    public void setNameBranch(String nameBranch) { this.nameBranch = nameBranch; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public Long getTotalTariffs() { return totalTariffs; }
    public void setTotalTariffs(Long totalTariffs) { this.totalTariffs = totalTariffs; }

    public Double getAverageCostPerKwh() { return averageCostPerKwh; }
    public void setAverageCostPerKwh(Double averageCostPerKwh) { this.averageCostPerKwh = averageCostPerKwh; }
}