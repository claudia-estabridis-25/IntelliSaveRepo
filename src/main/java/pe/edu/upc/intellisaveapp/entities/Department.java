package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDepartment; //PK

    @Column(name = "name_boss", length = 40, nullable = false)
    private String nameBoss;

    @Column(name = "name_department", length = 30, nullable = false)
    private String nameDepartment;

    @Column(name = "description_department", length = 90, nullable = false)
    private String descriptionDepartment;

    @ManyToOne
    @JoinColumn(name = "idBranch", nullable = false)
    private Branch branch; //FK

    public Department() {
    }

    public Department(Long idDepartment, String nameBoss, String nameDepartment,
                      String descriptionDepartment, Branch branch) {
        this.idDepartment = idDepartment;
        this.nameBoss = nameBoss;
        this.nameDepartment = nameDepartment;
        this.descriptionDepartment = descriptionDepartment;
        this.branch = branch;
    }

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public String getNameBoss() {
        return nameBoss;
    }

    public void setNameBoss(String nameBoss) {
        this.nameBoss = nameBoss;
    }

    public String getNameDepartment() {
        return nameDepartment;
    }

    public void setNameDepartment(String nameDepartment) {
        this.nameDepartment = nameDepartment;
    }

    public String getDescriptionDepartment() {
        return descriptionDepartment;
    }

    public void setDescriptionDepartment(String descriptionDepartment) {
        this.descriptionDepartment = descriptionDepartment;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}
