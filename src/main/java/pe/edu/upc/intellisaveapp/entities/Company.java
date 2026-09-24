package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompany; //PK

    @Column(name = "formalNameCompany", length = 40, nullable = false)
    private String formalNameCompany;

    @Column(name = "rucCompany", length = 11, nullable = false)
    private String rucCompany;

    @Column(name = "comercialNamecompany", length = 40, nullable = false)
    private String comercialNamecompany;

    @Column(name = "addressCompany", length = 60, nullable = false)
    private String addressCompany;

    @Column(name = "phoneNumberCompany", length = 7, nullable = false)
    private String phoneNumberCompany;

    @Column(name = "emailCompany", length = 25, nullable = false)
    private String emailCompany;

    @Column(name = "sectorCompany", length = 20, nullable = false)
    private String sectorCompany;

    @Column(name = "nameOwnerCompany", length = 16, nullable = false)
    private String nameOwnerCompany;

    @Column(name = "surnameOwnerCompany", length = 20, nullable = false)
    private String surnameOwnerCompany;

    @Column(name = "descriptionCompany", length = 90, nullable = false)
    private String descriptionCompany;

    public Company() {
    }

    public Company(Long idCompany, String formalNameCompany, String rucCompany, String comercialNamecompany,
                   String addressCompany, String phoneNumberCompany, String emailCompany, String sectorCompany,
                   String nameOwnerCompany, String surnameOwnerCompany, String descriptionCompany) {
        this.idCompany = idCompany;
        this.formalNameCompany = formalNameCompany;
        this.rucCompany = rucCompany;
        this.comercialNamecompany = comercialNamecompany;
        this.addressCompany = addressCompany;
        this.phoneNumberCompany = phoneNumberCompany;
        this.emailCompany = emailCompany;
        this.sectorCompany = sectorCompany;
        this.nameOwnerCompany = nameOwnerCompany;
        this.surnameOwnerCompany = surnameOwnerCompany;
        this.descriptionCompany = descriptionCompany;
    }

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
