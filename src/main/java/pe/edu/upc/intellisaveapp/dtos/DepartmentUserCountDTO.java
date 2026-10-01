package pe.edu.upc.intellisaveapp.dtos;

public class DepartmentUserCountDTO {
    private Long idBranch;
    private String nameBranch;
    private Long idDepartment;
    private String nameDepartment;
    private Long totalUsers;

    public DepartmentUserCountDTO() {
    }

    public DepartmentUserCountDTO(Long idBranch, String nameBranch, Long idDepartment, String nameDepartment, Long totalUsers) {
        this.idBranch = idBranch;
        this.nameBranch = nameBranch;
        this.idDepartment = idDepartment;
        this.nameDepartment = nameDepartment;
        this.totalUsers = totalUsers;
    }

    public Long getIdBranch() { return idBranch; }
    public void setIdBranch(Long idBranch) { this.idBranch = idBranch; }

    public String getNameBranch() { return nameBranch; }
    public void setNameBranch(String nameBranch) { this.nameBranch = nameBranch; }

    public Long getIdDepartment() { return idDepartment; }
    public void setIdDepartment(Long idDepartment) { this.idDepartment = idDepartment; }

    public String getNameDepartment() { return nameDepartment; }
    public void setNameDepartment(String nameDepartment) { this.nameDepartment = nameDepartment; }

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
}