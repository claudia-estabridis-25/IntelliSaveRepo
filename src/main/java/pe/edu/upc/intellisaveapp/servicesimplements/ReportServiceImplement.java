package pe.edu.upc.intellisaveapp.servicesimplements;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionReportDTO;
import pe.edu.upc.intellisaveapp.dtos.ReportItemDTO;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IReportService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportServiceImplement implements IReportService {
    private final IConsumptionRecordService crS;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReportServiceImplement(IConsumptionRecordService crS) {
        this.crS = crS;
    }

    // ===================== REPORTE (JSON) =====================

    @Override
    public ConsumptionReportDTO generateConsumptionReport(LocalDateTime desde, LocalDateTime hasta, Long idBranch) {
        List<ConsumptionRecord> registros = crS.listHistory(idBranch, null, null, desde, hasta);

        ConsumptionReportDTO report = new ConsumptionReportDTO();
        report.setDesde(desde);
        report.setHasta(hasta);
        report.setIdBranch(idBranch);
        report.setTotalRecords(registros.size());
        report.setTotalKwh(redondear(sumarKwh(registros)));
        report.setTotalCost(redondear(sumarCosto(registros)));

        // Consumo por sede
        report.setByBranch(agrupar(registros,
                cr -> cr.getEquipment().getDepartment().getBranch().getIdBranch(),
                cr -> cr.getEquipment().getDepartment().getBranch().getNameBranch(),
                cr -> cr.getEquipment().getDepartment().getBranch().getAddressBranch()));

        // Consumo por área
        report.setByDepartment(agrupar(registros,
                cr -> cr.getEquipment().getDepartment().getIdDepartment(),
                cr -> cr.getEquipment().getDepartment().getNameDepartment(),
                cr -> cr.getEquipment().getDepartment().getBranch().getNameBranch()));

        // Top 5 equipos con mayor consumo
        report.setTopEquipments(agrupar(registros,
                cr -> cr.getEquipment().getIdEquipment(),
                cr -> cr.getEquipment().getNameEquipment(),
                cr -> cr.getEquipment().getDepartment().getNameDepartment())
                .stream()
                .limit(5)
                .toList());

        return report;
    }

    // ===================== REPORTE (PDF) =====================

    @Override
    public byte[] generateConsumptionReportPdf(LocalDateTime desde, LocalDateTime hasta, Long idBranch) {
        ConsumptionReportDTO report = generateConsumptionReport(desde, hasta, idBranch);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph titulo = new Paragraph("IntelliSave - Reporte de Consumo Energético", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Periodo: " + desde.format(FORMATO_FECHA)
                    + " al " + hasta.format(FORMATO_FECHA), fuenteNormal));
            if (idBranch != null) {
                document.add(new Paragraph("Sede filtrada (id): " + idBranch, fuenteNormal));
            }
            document.add(new Paragraph("Fecha de generación: "
                    + LocalDateTime.now().format(FORMATO_FECHA), fuenteNormal));
            document.add(Chunk.NEWLINE);

            // Resumen
            document.add(new Paragraph("Resumen del periodo", fuenteSubtitulo));
            document.add(Chunk.NEWLINE);

            PdfPTable resumen = new PdfPTable(2);
            resumen.setWidthPercentage(60);
            resumen.setHorizontalAlignment(Element.ALIGN_LEFT);
            agregarFilaResumen(resumen, "Registros de consumo", String.valueOf(report.getTotalRecords()));
            agregarFilaResumen(resumen, "Consumo total (kWh)", formatear(report.getTotalKwh()));
            agregarFilaResumen(resumen, "Costo total (S/)", formatear(report.getTotalCost()));
            document.add(resumen);

            if (report.getTotalRecords() == 0) {
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("No se encontraron registros de consumo en el periodo seleccionado.",
                        fuenteNormal));
            } else {
                agregarSeccion(document, "Consumo por sede", "Sede", "Dirección",
                        report.getByBranch(), fuenteSubtitulo);
                agregarSeccion(document, "Consumo por área", "Área", "Sede",
                        report.getByDepartment(), fuenteSubtitulo);
                agregarSeccion(document, "Top 5 equipos con mayor consumo", "Equipo", "Área",
                        report.getTopEquipments(), fuenteSubtitulo);
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return out.toByteArray();
    }

    // ===================== MÉTODOS DE APOYO =====================

    private List<ReportItemDTO> agrupar(List<ConsumptionRecord> registros,
                                        Function<ConsumptionRecord, Long> id,
                                        Function<ConsumptionRecord, String> nombre,
                                        Function<ConsumptionRecord, String> detalle) {
        Map<Long, List<ConsumptionRecord>> grupos = registros.stream()
                .collect(Collectors.groupingBy(id, LinkedHashMap::new, Collectors.toList()));

        return grupos.values()
                .stream()
                .map(lista -> {
                    ConsumptionRecord primero = lista.get(0);
                    ReportItemDTO item = new ReportItemDTO();
                    item.setId(id.apply(primero));
                    item.setName(nombre.apply(primero));
                    item.setDetail(detalle.apply(primero));
                    item.setRecordCount(lista.size());
                    item.setTotalKwh(redondear(sumarKwh(lista)));
                    item.setTotalCost(redondear(sumarCosto(lista)));
                    return item;
                })
                .sorted(Comparator.comparing(ReportItemDTO::getTotalKwh).reversed())
                .toList();
    }

    private void agregarSeccion(Document document, String tituloSeccion, String columnaNombre,
                                String columnaDetalle, List<ReportItemDTO> items, Font fuenteSubtitulo)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(tituloSeccion, fuenteSubtitulo));
        document.add(Chunk.NEWLINE);

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3f, 3f, 1.5f, 1.5f, 1.5f});

        agregarCeldaCabecera(tabla, columnaNombre);
        agregarCeldaCabecera(tabla, columnaDetalle);
        agregarCeldaCabecera(tabla, "Registros");
        agregarCeldaCabecera(tabla, "kWh");
        agregarCeldaCabecera(tabla, "Costo (S/)");

        Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (ReportItemDTO item : items) {
            tabla.addCell(new Phrase(item.getName(), fuenteCelda));
            tabla.addCell(new Phrase(item.getDetail(), fuenteCelda));
            tabla.addCell(new Phrase(String.valueOf(item.getRecordCount()), fuenteCelda));
            tabla.addCell(new Phrase(formatear(item.getTotalKwh()), fuenteCelda));
            tabla.addCell(new Phrase(formatear(item.getTotalCost()), fuenteCelda));
        }

        document.add(tabla);
    }

    private void agregarCeldaCabecera(PdfPTable tabla, String texto) {
        Font fuenteCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuenteCabecera));
        celda.setBackgroundColor(new Color(220, 230, 241));
        celda.setPadding(5);
        tabla.addCell(celda);
    }

    private void agregarFilaResumen(PdfPTable tabla, String etiqueta, String valor) {
        Font fuenteEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fuenteValor = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteEtiqueta));
        celdaEtiqueta.setBackgroundColor(new Color(220, 230, 241));
        celdaEtiqueta.setPadding(5);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuenteValor));
        celdaValor.setPadding(5);

        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    private Double sumarKwh(List<ConsumptionRecord> registros) {
        return registros.stream()
                .mapToDouble(ConsumptionRecord::getKwhConsumption)
                .sum();
    }

    private Double sumarCosto(List<ConsumptionRecord> registros) {
        return registros.stream()
                .mapToDouble(ConsumptionRecord::getCostTotal)
                .sum();
    }

    private Double redondear(Double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String formatear(Double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}