package pe.edu.upc.intellisaveapp.dtos;

public class BranchDTOList {
    private Long idBranch;
    private String nameBranch;
    private String addressBranch;
    private String descriptionBranch;
    private Double latitudeBranch;
    private Double longitudeBranch;
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

    public Long getIdCompany() {
        return idCompany;
    }

    public void setIdCompany(Long idCompany) {
        this.idCompany = idCompany;
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
}
