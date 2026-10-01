package pe.edu.upc.intellisaveapp.dtos;

import java.util.List;

public class ClimateRefreshResultDTO {
    private Integer totalBranches;
    private Integer savedRecords;
    private List<String> errors;

    public ClimateRefreshResultDTO() {
    }

    public ClimateRefreshResultDTO(Integer totalBranches, Integer savedRecords, List<String> errors) {
        this.totalBranches = totalBranches;
        this.savedRecords = savedRecords;
        this.errors = errors;
    }

    public Integer getTotalBranches() { return totalBranches; }
    public void setTotalBranches(Integer totalBranches) { this.totalBranches = totalBranches; }

    public Integer getSavedRecords() { return savedRecords; }
    public void setSavedRecords(Integer savedRecords) { this.savedRecords = savedRecords; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}