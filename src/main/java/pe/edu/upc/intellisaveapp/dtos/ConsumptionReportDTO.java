package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class ConsumptionReportDTO {
    private LocalDateTime desde;
    private LocalDateTime hasta;
    private Long idBranch;
    private Integer totalRecords;
    private Double totalKwh;
    private Double totalCost;
    private List<ReportItemDTO> byBranch;
    private List<ReportItemDTO> byDepartment;
    private List<ReportItemDTO> topEquipments;

    public LocalDateTime getDesde() {
        return desde;
    }

    public void setDesde(LocalDateTime desde) {
        this.desde = desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public void setHasta(LocalDateTime hasta) {
        this.hasta = hasta;
    }

    public Long getIdBranch() {
        return idBranch;
    }

    public void setIdBranch(Long idBranch) {
        this.idBranch = idBranch;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Double getTotalKwh() {
        return totalKwh;
    }

    public void setTotalKwh(Double totalKwh) {
        this.totalKwh = totalKwh;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public List<ReportItemDTO> getByBranch() {
        return byBranch;
    }

    public void setByBranch(List<ReportItemDTO> byBranch) {
        this.byBranch = byBranch;
    }

    public List<ReportItemDTO> getByDepartment() {
        return byDepartment;
    }

    public void setByDepartment(List<ReportItemDTO> byDepartment) {
        this.byDepartment = byDepartment;
    }

    public List<ReportItemDTO> getTopEquipments() {
        return topEquipments;
    }

    public void setTopEquipments(List<ReportItemDTO> topEquipments) {
        this.topEquipments = topEquipments;
    }
}