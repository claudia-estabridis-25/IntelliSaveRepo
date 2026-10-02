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
import pe.edu.upc.intellisaveapp.dtos.BranchEmissionDTO;
import pe.edu.upc.intellisaveapp.dtos.CarbonFootprintComparisonDTO;
import pe.edu.upc.intellisaveapp.dtos.DepartmentEmissionDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.CarbonFootprint;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.repositories.ICarbonFootprintRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICarbonFootprintService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class CarbonFootprintServiceImplement implements ICarbonFootprintService {
    // Periodos permitidos y su duración en meses
    private static final Map<String, Integer> PERIODOS = new LinkedHashMap<>();

    static {
        PERIODOS.put("Mensual", 1);
        PERIODOS.put("Bimestral", 2);
        PERIODOS.put("Trimestral", 3);
        PERIODOS.put("Semestral", 6);
        PERIODOS.put("Anual", 12);
    }

    private final ICarbonFootprintRepository cfR;
    private final IConsumptionRecordService crS;

    public CarbonFootprintServiceImplement(ICarbonFootprintRepository cfR, IConsumptionRecordService crS) {
        this.cfR = cfR;
        this.crS = crS;
    }

    @Override
    public List<CarbonFootprint> list() {
        return cfR.findAll();
    }

    @Override
    public List<CarbonFootprint> listByDepartment(Long idDepartment) {
        return cfR.findByDepartment_IdDepartment(idDepartment);
    }

    @Override
    public List<CarbonFootprint> listByBranch(Long idBranch) {
        return cfR.findByDepartment_Branch_IdBranch(idBranch);
    }

    @Override
    public Optional<CarbonFootprint> listById(Long id) {
        return cfR.findById(id);
    }

    @Override
    public void delete(Long id) {
        cfR.deleteById(id);
    }

    @Override
    public List<DepartmentEmissionDTO> emissionsByDepartment(Long idBranch) {
        List<Object[]> filas = (idBranch == null)
                ? cfR.emissionsByDepartment()
                : cfR.emissionsByDepartmentOfBranch(idBranch);

        return filas.stream()
                .map(row -> new DepartmentEmissionDTO(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2],
                        redondear(((Number) row[3]).doubleValue()),
                        redondear(((Number) row[4]).doubleValue())
                ))
                .toList();
    }

    // Consulta nativa: emisiones totales de CO2 por sede
    @Override
    public List<BranchEmissionDTO> emissionsByBranch() {
        return cfR.emissionsByBranch()
                .stream()
                .map(row -> new BranchEmissionDTO(
                        ((Number) row[0]).longValue(),                 // id_branch
                        (String) row[1],                               // name_branch
                        ((Number) row[2]).longValue(),                 // total_calculos
                        redondear(((Number) row[3]).doubleValue()),    // total_kwh
                        redondear(((Number) row[4]).doubleValue())     // total_co2
                ))
                .toList();
    }

    // Emisiones CO2 (kg) = Consumo (kWh) x Factor de emisión (kg CO2/kWh)
    // El consumo es la suma de los registros del área entre el inicio del periodo y la fecha de cálculo
    @Override
    public CarbonFootprint calculate(CarbonFootprint footprint, Department department, String timePeriod,
                                     LocalDate calculationDate, double emissionFactor) {
        String periodo = normalizarPeriodo(timePeriod);

        if (calculationDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La fecha de cálculo no puede ser una fecha futura");
        }

        // No se repite el mismo cálculo (misma área, periodo y fecha)
        boolean duplicado = cfR
                .findByDepartment_IdDepartmentAndTimePeriodIgnoreCaseAndCalculationDate(
                        department.getIdDepartment(), periodo, calculationDate)
                .stream()
                .anyMatch(cf -> footprint.getIdFootprint() == null
                        || !cf.getIdFootprint().equals(footprint.getIdFootprint()));

        if (duplicado) {
            throw new BusinessRuleException(
                    "Ya existe una huella de carbono " + periodo.toLowerCase()
                            + " del área con fecha de cálculo " + calculationDate
            );
        }

        LocalDate inicio = periodStart(periodo, calculationDate);

        double kwhTotal = crS.listHistory(null, department.getIdDepartment(), null,
                        inicio.atStartOfDay(), calculationDate.atTime(23, 59, 59))
                .stream()
                .mapToDouble(ConsumptionRecord::getKwhConsumption)
                .sum();

        if (kwhTotal == 0) {
            throw new BusinessRuleException(
                    "El área no tiene consumos registrados entre " + inicio + " y " + calculationDate
            );
        }

        footprint.setDepartment(department);
        footprint.setTimePeriod(periodo);
        footprint.setCalculationDate(calculationDate);
        footprint.setEmissionFactor(emissionFactor);
        footprint.setKwhTotalConsumption(redondear(kwhTotal));
        footprint.setCo2Emissions(redondear(kwhTotal * emissionFactor));

        return cfR.save(footprint);
    }

    // HU19: calcula la huella de todas las áreas; omite las que no tienen consumos o ya tienen el cálculo
    @Override
    public List<CarbonFootprint> calculateAll(List<Department> departments, String timePeriod,
                                              LocalDate calculationDate, double emissionFactor) {
        // Se valida primero el periodo y la fecha, para no omitir todas las áreas por un dato inválido
        normalizarPeriodo(timePeriod);
        if (calculationDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La fecha de cálculo no puede ser una fecha futura");
        }

        List<CarbonFootprint> calculadas = new ArrayList<>();
        for (Department department : departments) {
            try {
                calculadas.add(calculate(new CarbonFootprint(), department, timePeriod,
                        calculationDate, emissionFactor));
            } catch (BusinessRuleException e) {
                // El área no tiene consumos en el periodo, o ya tiene ese cálculo: se omite
            }
        }
        return calculadas;
    }

    // Primer día del periodo: ejm. Mensual con fecha 2026-10-15 -> 2026-09-16
    // Si la fecha de cálculo es fin de mes, el periodo empieza el día 1: ejm. 2026-09-30 -> 2026-09-01
    @Override
    public LocalDate periodStart(String timePeriod, LocalDate calculationDate) {
        if (timePeriod == null || calculationDate == null) {
            return null;
        }
        Integer meses = PERIODOS.get(normalizarPeriodo(timePeriod));
        LocalDate inicio = calculationDate.minusMonths(meses).plusDays(1);
        if (calculationDate.getDayOfMonth() == calculationDate.lengthOfMonth()) {
            inicio = calculationDate.withDayOfMonth(1).minusMonths(meses - 1);
        }
        return inicio;
    }

    // ===================== HU050: COMPARAR PERIODOS =====================

    // Compara el cálculo de la fecha A (base) con el de la fecha B, para un área o una sede
    // Variación = B - A (positiva: aumento de emisiones; negativa: reducción)
    @Override
    public CarbonFootprintComparisonDTO compare(Long idDepartment, Long idBranch, String timePeriod,
                                                LocalDate dateA, LocalDate dateB) {
        String periodo = normalizarPeriodo(timePeriod);

        List<CarbonFootprint> calculosA = buscarCalculos(idDepartment, idBranch, periodo, dateA);
        List<CarbonFootprint> calculosB = buscarCalculos(idDepartment, idBranch, periodo, dateB);

        // CA03: si falta el cálculo de alguno de los periodos
        if (calculosA.isEmpty() || calculosB.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un cálculo de huella de carbono para el periodo seleccionado"
            );
        }

        double kwhA = calculosA.stream().mapToDouble(CarbonFootprint::getKwhTotalConsumption).sum();
        double co2A = calculosA.stream().mapToDouble(CarbonFootprint::getCo2Emissions).sum();
        double kwhB = calculosB.stream().mapToDouble(CarbonFootprint::getKwhTotalConsumption).sum();
        double co2B = calculosB.stream().mapToDouble(CarbonFootprint::getCo2Emissions).sum();

        double variacion = co2B - co2A;

        CarbonFootprintComparisonDTO dto = new CarbonFootprintComparisonDTO();
        dto.setScope(idDepartment != null
                ? "Área: " + calculosA.get(0).getDepartment().getNameDepartment()
                : "Sede: " + calculosA.get(0).getDepartment().getBranch().getNameBranch());
        dto.setTimePeriod(periodo);
        dto.setDateA(dateA);
        dto.setKwhA(redondear(kwhA));
        dto.setCo2A(redondear(co2A));
        dto.setDateB(dateB);
        dto.setKwhB(redondear(kwhB));
        dto.setCo2B(redondear(co2B));
        dto.setVariationCo2(redondear(variacion));
        dto.setVariationPercent(co2A == 0 ? null : redondear(variacion / co2A * 100));
        dto.setTrend(variacion > 0 ? "Aumento" : (variacion < 0 ? "Reducción" : "Sin cambio"));
        return dto;
    }

    private List<CarbonFootprint> buscarCalculos(Long idDepartment, Long idBranch, String periodo, LocalDate fecha) {
        return (idDepartment != null)
                ? cfR.findByDepartment_IdDepartmentAndTimePeriodIgnoreCaseAndCalculationDate(idDepartment, periodo, fecha)
                : cfR.findByDepartment_Branch_IdBranchAndTimePeriodIgnoreCaseAndCalculationDate(idBranch, periodo, fecha);
    }

    // ===================== HU057: CONSTANCIA EN PDF =====================

    @Override
    public Optional<CarbonFootprint> findCalculation(Long idDepartment, String timePeriod, LocalDate calculationDate) {
        return cfR.findByDepartment_IdDepartmentAndTimePeriodIgnoreCaseAndCalculationDate(
                        idDepartment, timePeriod.trim(), calculationDate)
                .stream()
                .findFirst();
    }

    // CA02: empresa, sede, área, periodo, emisiones de CO2 y consumo total en kWh
    @Override
    public byte[] generateCertificatePdf(CarbonFootprint footprint) {
        Department department = footprint.getDepartment();
        Branch branch = department.getBranch();
        Company company = branch.getCompany();
        LocalDate inicio = periodStart(footprint.getTimePeriod(), footprint.getCalculationDate());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph marca = new Paragraph("IntelliSave - Lumen Save", fuenteNormal);
            marca.setAlignment(Element.ALIGN_CENTER);
            document.add(marca);
            document.add(Chunk.NEWLINE);

            Paragraph titulo = new Paragraph("Constancia de Huella de Carbono", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(
                    "Se deja constancia de que, según los registros de consumo energético de la plataforma "
                            + "IntelliSave, la huella de carbono calculada es la siguiente:", fuenteNormal));
            document.add(Chunk.NEWLINE);

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{2f, 3f});
            agregarFila(tabla, "Empresa", company.getFormalNameCompany());
            agregarFila(tabla, "Sede", branch.getNameBranch());
            agregarFila(tabla, "Área evaluada", department.getNameDepartment());
            agregarFila(tabla, "Periodo", footprint.getTimePeriod() + " (del " + inicio
                    + " al " + footprint.getCalculationDate() + ")");
            agregarFila(tabla, "Consumo total (kWh)", formatear(footprint.getKwhTotalConsumption()));
            agregarFila(tabla, "Factor de emisión (kg CO2/kWh)", String.valueOf(footprint.getEmissionFactor()));
            agregarFila(tabla, "Emisiones de CO2 (kg)", formatear(footprint.getCo2Emissions()));
            document.add(tabla);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Documento generado el "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fuenteNormal));
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar la constancia en PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return out.toByteArray();
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor) {
        Font fuenteEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fuenteValor = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteEtiqueta));
        celdaEtiqueta.setBackgroundColor(new Color(220, 230, 241));
        celdaEtiqueta.setPadding(6);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuenteValor));
        celdaValor.setPadding(6);

        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    // ===================== MÉTODOS DE APOYO =====================

    // Devuelve el periodo con su escritura oficial (sin importar mayúsculas)
    private String normalizarPeriodo(String timePeriod) {
        return PERIODOS.keySet()
                .stream()
                .filter(p -> p.equalsIgnoreCase(timePeriod.trim()))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "El periodo '" + timePeriod + "' no es válido. Valores permitidos: "
                                        + PERIODOS.keySet()
                        )
                );
    }

    private String formatear(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}