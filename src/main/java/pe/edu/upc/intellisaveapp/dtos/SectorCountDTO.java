package pe.edu.upc.intellisaveapp.dtos;

//Cantidad de empresas por sector de empresa
public class SectorCountDTO {
    private String sectorCompany;
    private Long totalCompanies;

    public SectorCountDTO() {
    }

    public SectorCountDTO(String sectorCompany, Long totalCompanies) {
        this.sectorCompany = sectorCompany;
        this.totalCompanies = totalCompanies;
    }

    public String getSectorCompany() {
        return sectorCompany;
    }

    public void setSectorCompany(String sectorCompany) {
        this.sectorCompany = sectorCompany;
    }

    public Long getTotalCompanies() {
        return totalCompanies;
    }

    public void setTotalCompanies(Long totalCompanies) {
        this.totalCompanies = totalCompanies;
    }
}
