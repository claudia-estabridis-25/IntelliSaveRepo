package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "branches")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBranch; //PK

    @Column(name = "name_branch", length = 30, nullable = false)
    private String nameBranch;

    @Column(name = "address_branch", length = 60, nullable = false)
    private String addressBranch;

    @Column(name = "description_branch", length = 90, nullable = false)
    private String descriptionBranch;

    @ManyToOne
    @JoinColumn(name = "idCompany", nullable = false)
    private Company company; //FK

    public Branch() {
    }

    public Branch(Long idBranch, String nameBranch, String addressBranch, String descriptionBranch,
                  Company company) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.addressBranch = addressBranch;
        this.descriptionBranch = descriptionBranch;
        this.company = company;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
