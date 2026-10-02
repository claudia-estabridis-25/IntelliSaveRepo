package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.dtos.DailyConsumptionPointDTO;
import pe.edu.upc.intellisaveapp.dtos.PredictionDTOList;
import pe.edu.upc.intellisaveapp.dtos.PredictionResultDTO;
import pe.edu.upc.intellisaveapp.entities.ConsumptionPrediction;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.entities.Tariff;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.repositories.IConsumptionPredictionRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionPredictionService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ITariffService;
import pe.edu.upc.intellisaveapp.dtos.PredictionDepartmentDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsumptionPredictionServiceImplement implements IConsumptionPredictionService {
    // Reglas del modelo
    private static final int DIAS_HISTORIAL = 60;          // Días de historial que se analizan
    private static final int MIN_DIAS_CON_DATOS = 7;       // HU013 T03: mínimo de días con consumo
    private static final double MARGEN_TOLERANCIA = 0.15;  // HU055 T01: ±15%
    private static final String MODELO = "Regresión lineal";

    // Estados de la predicción
    private static final String EN_CURSO = "En curso";
    private static final String CUMPLIDA = "Cumplida";
    private static final String ERRADA = "Errada";

    private final IConsumptionPredictionRepository pR;
    private final IConsumptionRecordService crS;
    private final ITariffService tS;

    public ConsumptionPredictionServiceImplement(IConsumptionPredictionRepository pR,
                                                 IConsumptionRecordService crS,
                                                 ITariffService tS) {
        this.pR = pR;
        this.crS = crS;
        this.tS = tS;
    }

    // ===================== HU013: GENERAR PREDICCIÓN =====================

    @Override
    public PredictionResultDTO generate(Department department, Equipment equipment,
                                        LocalDate inicio, LocalDate fin) {
        Long idDepartment = department.getIdDepartment();
        Long idEquipment = (equipment != null) ? equipment.getIdEquipment() : null;

        // 1. Consumo diario de los 60 días anteriores al periodo a predecir
        LocalDate finHistorial = inicio.minusDays(1);
        LocalDate inicioHistorial = inicio.minusDays(DIAS_HISTORIAL);
        Map<LocalDate, Double> consumoPorDia =
                consumoDiario(idDepartment, idEquipment, inicioHistorial, finHistorial);

        // HU013 CA04: datos insuficientes
        if (consumoPorDia.size() < MIN_DIAS_CON_DATOS) {
            throw new BusinessRuleException(
                    "No existen suficientes datos históricos para realizar una predicción de consumo"
            );
        }

        // 2. Serie diaria desde el primer día con datos (un día sin registros cuenta como 0 kWh)
        LocalDate primerDia = Collections.min(consumoPorDia.keySet());
        List<DailyConsumptionPointDTO> historico = new ArrayList<>();
        for (LocalDate dia = primerDia; !dia.isAfter(finHistorial); dia = dia.plusDays(1)) {
            historico.add(new DailyConsumptionPointDTO(dia, redondear(consumoPorDia.getOrDefault(dia, 0.0))));
        }

        // 3. Regresión lineal: kWh = a + b · día
        int n = historico.size();
        double mediaX = (n - 1) / 2.0;
        double mediaY = historico.stream().mapToDouble(DailyConsumptionPointDTO::getKwh).average().orElse(0.0);

        double sxy = 0;
        double sxx = 0;
        for (int i = 0; i < n; i++) {
            double dx = i - mediaX;
            sxy += dx * (historico.get(i).getKwh() - mediaY);
            sxx += dx * dx;
        }
        double b = (sxx == 0) ? 0 : sxy / sxx;   // pendiente: tendencia diaria
        double a = mediaY - b * mediaX;          // punto de partida

        // Nivel de confianza: R² (qué tan bien se ajusta la recta a los datos)
        double ssRes = 0;
        double ssTot = 0;
        for (int i = 0; i < n; i++) {
            double real = historico.get(i).getKwh();
            double estimadoHistorico = a + b * i;
            ssRes += Math.pow(real - estimadoHistorico, 2);
            ssTot += Math.pow(real - mediaY, 2);
        }
        double r2 = (ssTot == 0) ? 1.0 : Math.max(0, 1 - ssRes / ssTot);

        // 4. Proyección sobre el periodo a predecir
        List<DailyConsumptionPointDTO> estimado = new ArrayList<>();
        double totalEstimado = 0;
        for (LocalDate dia = inicio; !dia.isAfter(fin); dia = dia.plusDays(1)) {
            long x = ChronoUnit.DAYS.between(primerDia, dia);
            double kwhDia = Math.max(0, a + b * x);   // el consumo no puede ser negativo
            totalEstimado += kwhDia;
            estimado.add(new DailyConsumptionPointDTO(dia, redondear(kwhDia)));
        }

        // 5. Costo estimado con la tarifa vigente de la sede al inicio del periodo
        Long idBranch = department.getBranch().getIdBranch();
        double costoKwh = tS.findCurrentByBranch(idBranch, inicio)
                .map(Tariff::getCostPerKwh)
                .orElse(0.0);

        // 6. Guardar la predicción
        ConsumptionPrediction p = new ConsumptionPrediction();
        p.setDepartment(department);
        p.setEquipment(equipment);
        p.setGenerationDatePrediction(LocalDate.now());
        p.setInitialDatePrediction(inicio);
        p.setEndDatePrediction(fin);
        p.setKwhPrediction(redondear(totalEstimado));
        p.setCostPrediction(redondear(totalEstimado * costoKwh));
        p.setModelAI(MODELO);
        p.setConfidenceLevelAI(redondear(r2 * 100));
        p.setDescriptionPrediction("Predicción para "
                + (equipment != null ? "el equipo " + equipment.getNameEquipment()
                : "el área " + department.getNameDepartment())
                + " basada en " + consumoPorDia.size() + " días con consumo");
        p.setStatusPrediction(EN_CURSO);
        pR.save(p);

        // 7. Respuesta con los datos del gráfico (HU013 CA03)
        PredictionResultDTO resultado = new PredictionResultDTO();
        resultado.setPrediction(toDTO(p));
        resultado.setHistoricalDaysWithData(consumoPorDia.size());
        resultado.setHistorical(historico);
        resultado.setEstimated(estimado);
        return resultado;
    }

    // ===================== HU051: HISTORIAL =====================

    @Override
    public List<PredictionDTOList> list(Long idDepartment, Long idEquipment, String status) {
        // HU055: antes de mostrar el historial, se evalúan las predicciones cuyo periodo ya terminó
        evaluateFinishedPredictions();

        return pR.findAll(Sort.by(Sort.Direction.DESC, "generationDatePrediction", "idPrediction"))
                .stream()
                .filter(p -> idDepartment == null
                        || p.getDepartment().getIdDepartment().equals(idDepartment))
                .filter(p -> idEquipment == null
                        || (p.getEquipment() != null && p.getEquipment().getIdEquipment().equals(idEquipment)))
                .filter(p -> status == null || p.getStatusPrediction().equalsIgnoreCase(status))
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Optional<PredictionDTOList> listById(Long id) {
        // HU055: se actualiza el estado antes de mostrar el detalle
        evaluateFinishedPredictions();

        return pR.findById(id).map(this::toDTO);
    }

    // ===================== HU055: COMPARAR CON EL CONSUMO REAL =====================

    @Override
    public List<PredictionDTOList> evaluateFinishedPredictions() {
        // CA04: solo se evalúan las predicciones cuyo periodo ya terminó
        List<ConsumptionPrediction> terminadas =
                pR.findByStatusPredictionAndEndDatePredictionBefore(EN_CURSO, LocalDate.now());

        for (ConsumptionPrediction p : terminadas) {
            double real = consumoReal(p);
            p.setStatusPrediction(dentroDelMargen(p.getKwhPrediction(), real) ? CUMPLIDA : ERRADA);
            pR.save(p);
        }

        return terminadas.stream().map(this::toDTO).toList();
    }

    // Consulta nativa: predicciones por área y estado
    @Override
    public List<PredictionDepartmentDTO> predictionsByDepartment() {
        // Se actualizan antes los estados de las predicciones cuyo periodo ya terminó (HU055)
        evaluateFinishedPredictions();

        return pR.predictionsByDepartment()
                .stream()
                .map(fila -> new PredictionDepartmentDTO(
                        ((Number) fila[0]).longValue(),                  // id_department
                        (String) fila[1],                                // name_department
                        (String) fila[2],                                // status_prediction
                        ((Number) fila[3]).longValue(),                  // total_predicciones
                        redondear(((Number) fila[4]).doubleValue()),     // kwh_predicho
                        redondear(((Number) fila[5]).doubleValue())      // confianza_promedio
                ))
                .toList();
    }

    // ===================== MÉTODOS DE APOYO =====================

    private Map<LocalDate, Double> consumoDiario(Long idDepartment, Long idEquipment,
                                                 LocalDate desde, LocalDate hasta) {
        return crS.listHistory(null, idDepartment, idEquipment, desde.atStartOfDay(), hasta.atTime(23, 59, 59))
                .stream()
                .collect(Collectors.groupingBy(
                        cr -> cr.getDateTimeRecord().toLocalDate(),
                        Collectors.summingDouble(ConsumptionRecord::getKwhConsumption)
                ));
    }

    private double consumoReal(ConsumptionPrediction p) {
        Long idEquipment = (p.getEquipment() != null) ? p.getEquipment().getIdEquipment() : null;
        return crS.listHistory(null, p.getDepartment().getIdDepartment(), idEquipment,
                        p.getInitialDatePrediction().atStartOfDay(),
                        p.getEndDatePrediction().atTime(23, 59, 59))
                .stream()
                .mapToDouble(ConsumptionRecord::getKwhConsumption)
                .sum();
    }

    private boolean dentroDelMargen(double predicho, double real) {
        if (predicho == 0) {
            return real == 0;
        }
        return Math.abs(real - predicho) / predicho <= MARGEN_TOLERANCIA;
    }

    private PredictionDTOList toDTO(ConsumptionPrediction p) {
        PredictionDTOList dto = new PredictionDTOList();
        dto.setIdPrediction(p.getIdPrediction());
        dto.setIdDepartment(p.getDepartment().getIdDepartment());
        dto.setNameDepartment(p.getDepartment().getNameDepartment());
        if (p.getEquipment() != null) {
            dto.setIdEquipment(p.getEquipment().getIdEquipment());
            dto.setNameEquipment(p.getEquipment().getNameEquipment());
        }
        dto.setGenerationDatePrediction(p.getGenerationDatePrediction());
        dto.setInitialDatePrediction(p.getInitialDatePrediction());
        dto.setEndDatePrediction(p.getEndDatePrediction());
        dto.setKwhPrediction(p.getKwhPrediction());
        dto.setCostPrediction(p.getCostPrediction());
        dto.setModelAI(p.getModelAI());
        dto.setConfidenceLevelAI(p.getConfidenceLevelAI());
        dto.setDescriptionPrediction(p.getDescriptionPrediction());
        dto.setStatusPrediction(p.getStatusPrediction());

        // Si ya fue evaluada, se muestra el consumo real y la desviación
        if (!EN_CURSO.equals(p.getStatusPrediction())) {
            double real = consumoReal(p);
            dto.setRealKwh(redondear(real));
            if (p.getKwhPrediction() > 0) {
                dto.setDeviationPercent(redondear(Math.abs(real - p.getKwhPrediction()) / p.getKwhPrediction() * 100));
            }
        }
        return dto;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}