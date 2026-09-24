package pe.edu.upc.trabajoavance_kevin.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRole;
    @Column(name = "roleName", length = 50, nullable = false)
    private String roleName;
    @Column(name = "roleDescription", length = 150, nullable = false)
    private String roleDescription;

    public Role() {}

    public Role(Long idRole, String roleName, String roleDescription) {
        this.idRole = idRole;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }

    public Long getIdRole() { return idRole; }
    public void setIdRole(Long idRole) { this.idRole = idRole; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleDescription() { return roleDescription; }
    public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }
}
