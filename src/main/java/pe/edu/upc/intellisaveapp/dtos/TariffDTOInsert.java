package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TariffDTOInsert {
    private Long idTariff;
    @NotNull(message = "El id de la sede es obligatorio")
    private Long idBranch;
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate initialEffectiveDate;
    @NotNull(message = "La fecha de fin de vigencia es obligatoria")
    private LocalDate endEffectiveDate;
    @NotNull(message = "El costo por kWh es obligatorio")
    @Positive(message = "El costo por kWh debe ser mayor a 0")
    private Double costPerKwh;
    @Size(max = 60, message = "El proveedor no puede superar 60 caracteres")
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