package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_user", "name_role"})
        }
)
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRole; //PK

    @Column(name = "name_role", nullable = false, length = 20)
    private String nameRole;

    @Column(nullable = false, length = 60)
    private String descriptionRole;

    //Un usuario puede tener varios roles (ejem: Admin y Supervisor a la vez)
    @ManyToOne(fetch = FetchType.LAZY) //N roles -> 1 user
    @JoinColumn(name = "id_user", nullable = false)
    private Users user; //FK

    public Role() {
    }

    public Role(Long idRole, String nameRole, String descriptionRole, Users user) {
        this.idRole = idRole;
        this.nameRole = nameRole;
        this.descriptionRole = descriptionRole;
        this.user = user;
    }

    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public String getNameRole() {
        return nameRole;
    }

    public void setNameRole(String nameRole) {
        this.nameRole = nameRole;
    }

    public String getDescriptionRole() {
        return descriptionRole;
    }

    public void setDescriptionRole(String descriptionRole) {
        this.descriptionRole = descriptionRole;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
