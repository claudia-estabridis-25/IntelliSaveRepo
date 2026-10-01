package pe.edu.upc.intellisaveapp.dtos;

public class CompanyDTOList {
    private Long idCompany;
    private String rucCompany;
    private String comercialNamecompany;
    private String addressCompany;
    private String emailCompany;

    public Long getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(Long idCompany) {
        this.idCompany = idCompany;
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

    public String getEmailCompany() {
        return emailCompany;
    }

    public void setEmailCompany(String emailCompany) {
        this.emailCompany = emailCompany;
    }
}
