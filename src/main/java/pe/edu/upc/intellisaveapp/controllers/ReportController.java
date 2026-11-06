package pe.edu.upc.intellisaveapp.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionReportDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IReportService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final IReportService rS;
    private final IBranchService bS; //Para validar a qué empresa pertenece la sede filtrada
    private final CompanyAccess acceso; //Para validar la empresa del usuario autenticado

    public ReportController(IReportService rS, IBranchService bS, CompanyAccess acceso) {
        this.rS = rS;
        this.bS = bS;
        this.acceso = acceso;
    }

    // HU014: reporte de consumo del periodo (JSON)
    @GetMapping("/consumption")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ConsumptionReportDTO> reporteConsumo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Long idBranch) {

        validarRangoFechas(desde, hasta);
        //Si se filtra por una sede, se valida que sea de la empresa del usuario autenticado
        validateBranch(idBranch);

        return ResponseEntity.ok(rS.generateConsumptionReport(desde, hasta, idBranch));
    }

    // HU014: descarga del reporte en PDF
    @GetMapping("/consumption/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<byte[]> reporteConsumoPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Long idBranch) {

        validarRangoFechas(desde, hasta);
        //Si se filtra por una sede, se valida que sea de la empresa del usuario autenticado
        validateBranch(idBranch);

        byte[] pdf = rS.generateConsumptionReportPdf(desde, hasta, idBranch);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-consumo.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // HU048: descarga del reporte en CSV (se abre en Excel)
    @GetMapping("/consumption/csv")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<byte[]> reporteConsumoCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Long idBranch) {

        validarRangoFechas(desde, hasta);
        //Si se filtra por una sede, se valida que sea de la empresa del usuario autenticado
        validateBranch(idBranch);

        byte[] csv = rS.generateConsumptionReportCsv(desde, hasta, idBranch);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-consumo.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    //Si se envía una sede, valida que exista y que sea de la empresa del usuario autenticado.
    //Si no existe devuelve 404, y si es de otra empresa devuelve 403.
    //Si no se envía sede, no valida nada: el service ya deja solo los consumos de su empresa.
    private void validateBranch(Long idBranch) {
        if (idBranch == null) {
            return;
        }

        Branch branch = bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una sede con el id: " + idBranch)
                );

        acceso.validarEmpresa(branch.getCompany().getIdCompany());
    }

    private void validarRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde.isAfter(hasta)) {
            throw new BusinessRuleException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
    }
}