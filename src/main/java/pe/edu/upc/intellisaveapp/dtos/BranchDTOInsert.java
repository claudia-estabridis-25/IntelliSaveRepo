package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BranchDTOInsert {
    private Long idBranch;
    @NotBlank(message = "El nombre de la sede es obligatorio")
    private String nameBranch;
    @NotBlank(message = "La dirección de la sede es obligatoria")
    private String addressBranch;
    @NotBlank(message = "La descripción de la sede es obligatoria")
    private String descriptionBranch;
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

    public Long getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(Long idCompany) {
        this.idCompany = idCompany;
    }
}
