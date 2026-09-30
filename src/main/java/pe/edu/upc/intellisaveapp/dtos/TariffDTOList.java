package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDate;

public class TariffDTOList {
    private Long idTariff;
    private Long idBranch;
    private String nameBranch;
    private LocalDate initialEffectiveDate;
    private LocalDate endEffectiveDate;
    private Double costPerKwh;
    private String supplier;

    public Long getIdTariff() {
        return idTariff;
    }

    public void setIdTariff(Long idTariff) {
        this.idTariff = idTariff;
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

    public LocalDate getInitialEffectiveDate() {
        return initialEffectiveDate;
    }

    public void setInitialEffectiveDate(LocalDate initialEffectiveDate) {
        this.initialEffectiveDate = initialEffectiveDate;
    }

    public LocalDate getEndEffectiveDate() {
        return endEffectiveDate;
    }

    public void setEndEffectiveDate(LocalDate endEffectiveDate) {
        this.endEffectiveDate = endEffectiveDate;
    }

    public Double getCostPerKwh() {
        return costPerKwh;
    }

    public void setCostPerKwh(Double costPerKwh) {
        this.costPerKwh = costPerKwh;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
}