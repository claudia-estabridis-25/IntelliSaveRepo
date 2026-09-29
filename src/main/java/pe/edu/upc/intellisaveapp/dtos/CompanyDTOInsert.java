package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CompanyDTOInsert {
    private Long idCompany;
    @NotBlank(message = "La razón social de la empresa es obligatoria")
    @Size(max = 40, message = "La razón social no puede superar 40 caracteres")
    private String formalNameCompany;
    @NotBlank(message = "El RUC de la empresa es obligatorio")
    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener exactamente 11 dígitos numéricos")
    private String rucCompany;
    @NotBlank(message = "El nombre comercial de la empresa es obligatorio")
    @Size(max = 40, message = "El nombre comercial no puede superar 40 caracteres")
    private String comercialNamecompany;
    @NotBlank(message = "La dirección de la empresa es obligatoria")
    @Size(max = 60, message = "La dirección no puede superar 60 caracteres")
    private String addressCompany;
    @NotBlank(message = "El número de teléfono de la empresa es obligatorio")
    @Pattern(regexp = "^[0-9]{7,9}$", message = "El teléfono debe tener entre 7 y 9 dígitos numéricos")
    private String phoneNumberCompany;
    @NotBlank(message = "El correo electrónico de la empresa es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 50, message = "El correo no puede superar 50 caracteres")
    private String emailCompany;
    @NotBlank(message = "El sector de mercado de la empresa es obligatorio")
    @Size(max = 20, message = "El sector no puede superar 20 caracteres")
    private String sectorCompany;
    @NotBlank(message = "El nombre del dueño de la empresa es obligatorio")
    @Size(max = 16, message = "El nombre del dueño no puede superar 16 caracteres")
    private String nameOwnerCompany;
    @NotBlank(message = "El apellido del dueño de la empresa es obligatorio")
    @Size(max = 20, message = "El apellido del dueño no puede superar 20 caracteres")
    private String surnameOwnerCompany;
    @NotBlank(message = "La descripción de la empresa es obligatoria")
    @Size(max = 90, message = "La descripción no puede superar 90 caracteres")
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
