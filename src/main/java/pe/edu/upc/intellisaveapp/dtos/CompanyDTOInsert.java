package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotBlank;

public class CompanyDTOInsert {
    private Long idCompany;
    @NotBlank(message = "La razón social de la empresa es obligatoria")
    private String formalNameCompany;
    @NotBlank(message = "El RUC de la empresa es obligatorio")
    private String rucCompany;
    @NotBlank(message = "El nombre comercial de la empresa es obligatorio")
    private String comercialNamecompany;
    @NotBlank(message = "La dirección de la empresa es obligatoria")
    private String addressCompany;
    @NotBlank(message = "El número de teléfono de la empresa es obligatorio")
    private String phoneNumberCompany;
    @NotBlank(message = "El correo electrónico de la empresa es obligatorio")
    private String emailCompany;
    @NotBlank(message = "El sector de mercado de la empresa es obligatorio")
    private String sectorCompany;
    @NotBlank(message = "El nombre del dueño de la empresa es obligatorio")
    private String nameOwnerCompany;
    @NotBlank(message = "El apellido del dueño de la empresa es obligatorio")
    private String surnameOwnerCompany;
    @NotBlank(message = "La descripción de la empresa es obligatoria")
    private String descriptionCompany;


    public Long getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(Long idCompany) {
        this.idCompany = idCompany;
    }

    public String getFormalNameCompany() {
        return formalNameCompany;
    }

    public void setFormalNameCompany(String formalNameCompany) {
        this.formalNameCompany = formalNameCompany;
    }

    public String getRucCompany() {
        return rucCompany;
    }

    public void setRucCompany(String rucCompany) {
        this.rucCompany = rucCompany;
    }

    public String getComercialNamecompany() {
        return comercialNamecompany;
    }

    public void setComercialNamecompany(String comercialNamecompany) {
        this.comercialNamecompany = comercialNamecompany;
    }

    public String getAddressCompany() {
        return addressCompany;
    }

    public void setAddressCompany(String addressCompany) {
        this.addressCompany = addressCompany;
    }

    public String getPhoneNumberCompany() {
        return phoneNumberCompany;
    }

    public void setPhoneNumberCompany(String phoneNumberCompany) {
        this.phoneNumberCompany = phoneNumberCompany;
    }

    public String getEmailCompany() {
        return emailCompany;
    }

    public void setEmailCompany(String emailCompany) {
        this.emailCompany = emailCompany;
    }

    public String getSectorCompany() {
        return sectorCompany;
    }

    public void setSectorCompany(String sectorCompany) {
        this.sectorCompany = sectorCompany;
    }

    public String getNameOwnerCompany() {
        return nameOwnerCompany;
    }

    public void setNameOwnerCompany(String nameOwnerCompany) {
        this.nameOwnerCompany = nameOwnerCompany;
    }

    public String getSurnameOwnerCompany() {
        return surnameOwnerCompany;
    }

    public void setSurnameOwnerCompany(String surnameOwnerCompany) {
        this.surnameOwnerCompany = surnameOwnerCompany;
    }

    public String getDescriptionCompany() {
        return descriptionCompany;
    }

    public void setDescriptionCompany(String descriptionCompany) {
        this.descriptionCompany = descriptionCompany;
    }
}
