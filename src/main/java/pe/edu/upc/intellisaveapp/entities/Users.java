package pe.edu.upc.intellisaveapp.entities;


import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username")
        }
)
public class Users implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;

    @Column(nullable = false, unique = true, length = 60)
    private String emailUser; //Funcionará como su username

    @Column(nullable = false, length = 25)
    private String positionUser; //cargo, puesto

    @Column(nullable = false, length = 8)
    private String dniUser;

    @Column(nullable = false, length = 20)
    private String firstName;

    @Column(length = 20)
    private String secondName;

    @Column(nullable = false, length = 25)
    private String paternalSurname;

    @Column(nullable = false, length = 25)
    private String maternalSurname;

    @Column(nullable = false, length = 200)
    private String passwordUser;

    @Column(nullable = false, length = 9)
    private String telephoneUser;

    @Column(nullable = false)
    private Boolean statusUser = true;

    //Muchos usuarios pertenecen a 1 área (departamento)
    @ManyToOne
    @JoinColumn(name="idDepartment")
    private Department department; //FK

    /*
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Role> roles = new ArrayList<>(); //FK
    */

    public Users() {
    }

    public Users(Long idUser, String emailUser, String positionUser, String dniUser, String firstName, String secondName,
                 String paternalSurname, String maternalSurname, String passwordUser, String telephoneUser,
                 Boolean statusUser, Department department) {
        this.idUser = idUser;
        this.emailUser = emailUser;
        this.positionUser = positionUser;
        this.dniUser = dniUser;
        this.firstName = firstName;
        this.secondName = secondName;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.passwordUser = passwordUser;
        this.telephoneUser = telephoneUser;
        this.statusUser = statusUser;
        this.department = department;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getPositionUser() {
        return positionUser;
    }

    public void setPositionUser(String positionUser) {
        this.positionUser = positionUser;
    }

    public String getDniUser() {
        return dniUser;
    }

    public void setDniUser(String dniUser) {
        this.dniUser = dniUser;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getPaternalSurname() {
        return paternalSurname;
    }

    public void setPaternalSurname(String paternalSurname) {
        this.paternalSurname = paternalSurname;
    }

    public String getMaternalSurname() {
        return maternalSurname;
    }

    public void setMaternalSurname(String maternalSurname) {
        this.maternalSurname = maternalSurname;
    }

    public String getPasswordUser() {
        return passwordUser;
    }

    public void setPasswordUser(String passwordUser) {
        this.passwordUser = passwordUser;
    }

    public String getTelephoneUser() {
        return telephoneUser;
    }

    public void setTelephoneUser(String telephoneUser) {
        this.telephoneUser = telephoneUser;
    }

    public Boolean getStatusUser() {
        return statusUser;
    }

    public void setStatusUser(Boolean statusUser) {
        this.statusUser = statusUser;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
