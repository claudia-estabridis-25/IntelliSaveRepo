package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.dtos.ConsumptionReportDTO;

import java.time.LocalDateTime;

public interface IReportService {
    public ConsumptionReportDTO generateConsumptionReport(LocalDateTime desde, LocalDateTime hasta, Long idBranch);
    public byte[] generateConsumptionReportPdf(LocalDateTime desde, LocalDateTime hasta, Long idBranch);
    public byte[] generateConsumptionReportCsv(LocalDateTime desde, LocalDateTime hasta, Long idBranch);
}