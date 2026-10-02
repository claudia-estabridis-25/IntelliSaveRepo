package pe.edu.upc.intellisaveapp.dtos;

public class BranchEmissionDTO {
    private Long idBranch;
    private String nameBranch;
    private Long totalCalculations;
    private Double totalKwh;
    private Double totalCo2Emissions;

    public BranchEmissionDTO() {
    }

    public BranchEmissionDTO(Long idBranch, String nameBranch, Long totalCalculations, Double totalKwh, Double totalCo2Emissions) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.totalCalculations = totalCalculations;
        this.totalKwh = totalKwh;
        this.totalCo2Emissions = totalCo2Emissions;
    }

    public Long getIdBranch() { return idBranch; }
    public void setIdBranch(Long idBranch) { this.idBranch = idBranch; }

    public String getNameBranch() { return nameBranch; }
    public void setNameBranch(String nameBranch) { this.nameBranch = nameBranch; }

    public Long getTotalCalculations() { return totalCalculations; }
    public void setTotalCalculations(Long totalCalculations) { this.totalCalculations = totalCalculations; }

    public Double getTotalKwh() { return totalKwh; }
    public void setTotalKwh(Double totalKwh) { this.totalKwh = totalKwh; }

    public Double getTotalCo2Emissions() { return totalCo2Emissions; }
    public void setTotalCo2Emissions(Double totalCo2Emissions) { this.totalCo2Emissions = totalCo2Emissions; }
}