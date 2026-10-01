package pe.edu.upc.intellisaveapp.dtos;

public class RoleUserCountDTO {
    private String nameRole;
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;

    public RoleUserCountDTO() {
    }

    public RoleUserCountDTO(String nameRole, Long totalUsers, Long activeUsers, Long inactiveUsers) {
        this.nameRole = nameRole;
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
    }

    public String getNameRole() { return nameRole; }
    public void setNameRole(String nameRole) { this.nameRole = nameRole; }

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }

    public Long getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(Long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
}