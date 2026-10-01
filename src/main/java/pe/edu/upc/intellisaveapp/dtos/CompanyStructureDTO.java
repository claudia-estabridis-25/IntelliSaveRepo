package pe.edu.upc.intellisaveapp.dtos;

//Cantidad de sedes y áreas por empresa
public class CompanyStructureDTO {
    private Long idCompany;
    private String formalNameCompany;
    private Long totalBranches; //sedes
    private Long totalDepartments; //áreas

    public CompanyStructureDTO() {
    }

    public CompanyStructureDTO(Long idCompany, String formalNameCompany, Long totalBranches, Long totalDepartments) {
        this.idCompany = idCompany;
        this.formalNameCompany = formalNameCompany;
        this.totalBranches = totalBranches;
        this.totalDepartments = totalDepartments;
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

    public Long getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(Long totalBranches) {
        this.totalBranches = totalBranches;
    }

    public Long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(Long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }
}
