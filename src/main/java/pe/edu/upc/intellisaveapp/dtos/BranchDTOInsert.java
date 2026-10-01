package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BranchDTOInsert {
    private Long idBranch;
    @NotBlank(message = "El nombre de la sede es obligatorio")
    @Size(max = 30, message = "El nombre de la sede no puede superar 30 caracteres")
    private String nameBranch;

    @NotBlank(message = "La dirección de la sede es obligatoria")
    @Size(max = 60, message = "La dirección de la sede no puede superar 60 caracteres")
    private String addressBranch;

    @NotBlank(message = "La descripción de la sede es obligatoria")
    @Size(max = 90, message = "La descripción de la sede no puede superar 90 caracteres")
    private String descriptionBranch;

    @NotNull(message = "La latitud de la sede es obligatoria")
    @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
    private Double latitudeBranch;

    @NotNull(message = "La longitud de la sede es obligatoria")
    @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
    private Double longitudeBranch;

    @NotNull(message = "El id de la empresa relacionada es obligatorio")
    private Long idCompany;


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

    public String getAddressBranch() {
        return addressBranch;
    }

    public void setAddressBranch(String addressBranch) {
        this.addressBranch = addressBranch;
    }

    public String getDescriptionBranch() {
        return descriptionBranch;
    }

    public void setDescriptionBranch(String descriptionBranch) {
        this.descriptionBranch = descriptionBranch;
    }

    public Double getLatitudeBranch() {
        return latitudeBranch;
    }

    public void setLatitudeBranch(Double latitudeBranch) {
        this.latitudeBranch = latitudeBranch;
    }

    public Double getLongitudeBranch() {
        return longitudeBranch;
    }

    public void setLongitudeBranch(Double longitudeBranch) {
        this.longitudeBranch = longitudeBranch;
    }

    public Long getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(Long idCompany) {
        this.idCompany = idCompany;
    }
}