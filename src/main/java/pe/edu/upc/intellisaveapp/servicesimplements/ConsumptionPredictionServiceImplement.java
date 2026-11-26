package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.clients.GeminiApiClient;
import pe.edu.upc.intellisaveapp.dtos.DailyConsumptionPointDTO;
import pe.edu.upc.intellisaveapp.dtos.PredictionDTOList;
import pe.edu.upc.intellisaveapp.dtos.PredictionResultDTO;
import pe.edu.upc.intellisaveapp.entities.ConsumptionPrediction;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.entities.Tariff;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.repositories.IConsumptionPredictionRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionPredictionService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ITariffService;
import pe.edu.upc.intellisaveapp.dtos.PredictionDepartmentDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final int MAX_ANALISIS = 1000;
    private static final int MAX_RECOMENDACIONES = 2000;
    private static final int MAX_EQUIPOS_EN_PROMPT = 10;
    private static final Logger log = LoggerFactory.getLogger(ConsumptionPredictionServiceImplement.class);

    private final IConsumptionPredictionRepository pR;
    private final IConsumptionRecordService crS;
    private final ITariffService tS;
    private final GeminiApiClient gemini;

    public ConsumptionPredictionServiceImplement(IConsumptionPredictionRepository pR,
                                                 IConsumptionRecordService crS,
                                                 ITariffService tS,
                                                 GeminiApiClient gemini) {
        this.pR = pR;
        this.crS = crS;
        this.tS = tS;
        this.gemini = gemini;
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
        analizarConIA(p);

        // 7. Respuesta con los datos del gráfico (HU013 CA03)
        PredictionResultDTO resultado = new PredictionResultDTO();
        resultado.setPrediction(toDTO(p));
        resultado.setHistoricalDaysWithData(consumoPorDia.size());
        resultado.setHistorical(historico);
        resultado.setEstimated(estimado);
        return resultado;
    }

    @Override
    public PredictionDTOList analyze(Long id) {
        ConsumptionPrediction p = pR.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una predicción con el id: " + id));

        if (!gemini.isConfigured()) {
            throw new BusinessRuleException("El análisis con IA no está configurado: falta la variable GEMINI_API_KEY");
        }
        if (!analizarConIA(p)) {
            throw new BusinessRuleException("No se pudo generar el análisis con IA. Intenta de nuevo en unos minutos");
        }
        return toDTO(p);
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

    @Override
    public void delete(Long id) {
        pR.deleteById(id);
    }

    // ===================== HU055: COMPARAR CON EL CONSUMO REAL =====================

    @Override
    public List<PredictionDTOList> evaluateFinishedPredictions() {
        // CA04: solo se evalúan las predicciones cuyo periodo ya terminó.
        // Se reevalúan también las que ya estaban Cumplida o Errada: si después se registra o corrige
        // un consumo de ese periodo, el consumo real cambia y el estado debe seguirlo (antes quedaba congelado).
        List<ConsumptionPrediction> cambiadas = new ArrayList<>();

        for (ConsumptionPrediction p : pR.findByEndDatePredictionBefore(LocalDate.now())) {
            double real = consumoReal(p);
            String nuevoEstado = dentroDelMargen(p.getKwhPrediction(), real) ? CUMPLIDA : ERRADA;
            if (!nuevoEstado.equals(p.getStatusPrediction())) {
                p.setStatusPrediction(nuevoEstado);
                pR.save(p);
                cambiadas.add(p);
            }
        }

        return cambiadas.stream().map(this::toDTO).toList();
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

        dto.setAiAnalysis(p.getAiAnalysisPrediction());
        dto.setAiRecommendations(p.getAiRecommendationsPrediction() == null
                ? List.of()
                : List.of(p.getAiRecommendationsPrediction().split("\n")));

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

    private boolean analizarConIA(ConsumptionPrediction p) {
        if (!gemini.isConfigured()) {
            return false;
        }
        try {
            String respuesta = gemini.generateText(construirPrompt(p));
            String analisis = extraerAnalisis(respuesta);
            List<String> recomendaciones = extraerRecomendaciones(respuesta);
            if (analisis.isBlank() && recomendaciones.isEmpty()) {
                return false;
            }
            p.setAiAnalysisPrediction(recortar(analisis, MAX_ANALISIS));
            p.setAiRecommendationsPrediction(recortar(String.join("\n", recomendaciones), MAX_RECOMENDACIONES));
            pR.save(p);
            return true;
        } catch (RuntimeException e) {
            log.warn("No se pudo generar el análisis con Gemini: {}", e.getMessage());
            return false;
        }
    }

    private String construirPrompt(ConsumptionPrediction p) {
        Department department = p.getDepartment();
        Equipment equipment = p.getEquipment();
        Long idEquipment = (equipment != null) ? equipment.getIdEquipment() : null;
        LocalDate inicio = p.getInitialDatePrediction();
        LocalDate finHistorial = inicio.minusDays(1);
        LocalDate inicioHistorial = inicio.minusDays(DIAS_HISTORIAL);

        List<ConsumptionRecord> registros = crS.listHistory(null, department.getIdDepartment(), idEquipment,
                inicioHistorial.atStartOfDay(), finHistorial.atTime(23, 59, 59));

        Map<LocalDate, Double> porDia = registros.stream()
                .collect(Collectors.groupingBy(
                        cr -> cr.getDateTimeRecord().toLocalDate(),
                        Collectors.summingDouble(ConsumptionRecord::getKwhConsumption)
                ));
        double totalHistorial = porDia.values().stream().mapToDouble(Double::doubleValue).sum();
        double promedioDiaConDatos = porDia.isEmpty() ? 0 : totalHistorial / porDia.size();
        double ultimaSemana = sumarEntre(porDia, inicio.minusDays(7), finHistorial);
        double semanaAnterior = sumarEntre(porDia, inicio.minusDays(14), inicio.minusDays(8));
        double tendencia = pendienteDiaria(porDia, finHistorial);
        long diasPeriodo = ChronoUnit.DAYS.between(inicio, p.getEndDatePrediction()) + 1;
        double tarifa = p.getKwhPrediction() > 0 ? p.getCostPrediction() / p.getKwhPrediction() : 0;

        StringBuilder equipos = new StringBuilder();
        registros.stream()
                .collect(Collectors.groupingBy(ConsumptionRecord::getEquipment))
                .entrySet()
                .stream()
                .sorted(Comparator.comparingDouble((Map.Entry<Equipment, List<ConsumptionRecord>> e) ->
                        e.getValue().stream().mapToDouble(ConsumptionRecord::getKwhConsumption).sum()).reversed())
                .limit(MAX_EQUIPOS_EN_PROMPT)
                .forEach(e -> {
                    Equipment eq = e.getKey();
                    double kwh = e.getValue().stream().mapToDouble(ConsumptionRecord::getKwhConsumption).sum();
                    double horas = e.getValue().stream().mapToDouble(ConsumptionRecord::getHoursOfUse).sum();
                    equipos.append(String.format(Locale.US,
                            "- %s (categoría %s, %.0f W, estado %s): %.2f kWh en %d registros, %.1f horas en total, %.1f horas por registro en promedio%n",
                            eq.getNameEquipment(), eq.getCategoryEquipment(), eq.getWattPowerEquipment(),
                            eq.getStatusEquipment(), kwh, e.getValue().size(), horas, horas / e.getValue().size()));
                });

        String objetivo = (equipment != null)
                ? "el equipo " + equipment.getNameEquipment() + " del área " + department.getNameDepartment()
                : "toda el área " + department.getNameDepartment();

        return String.format(Locale.US, """
                Eres un analista de eficiencia energética de IntelliSave, una plataforma que gestiona el consumo eléctrico de empresas con varias sedes en el Perú.
                Analiza esta predicción de consumo y da recomendaciones de ahorro.

                DATOS
                Empresa: %s
                Sede: %s
                Predicción de: %s
                Historial analizado: %s al %s (%d días con consumo, %.2f kWh en total, %.2f kWh promedio por día con consumo)
                Consumo de los últimos 7 días: %.2f kWh. Los 7 días anteriores: %.2f kWh
                Tendencia de la regresión lineal: %+.3f kWh por día
                Periodo predicho: %s al %s (%d días)
                Consumo estimado: %.2f kWh, costo estimado S/ %.2f, tarifa S/ %.4f por kWh
                Confianza del modelo (R²): %.1f %%
                Equipos con consumo en el historial:
                %s
                INSTRUCCIONES
                Responde en español, en texto plano, sin markdown ni asteriscos, usando exactamente este formato:
                ANALISIS: dos o tres oraciones que expliquen la tendencia del consumo y qué significa la predicción para el costo.
                RECOMENDACIONES:
                - recomendación 1
                - recomendación 2
                - recomendación 3
                Da entre 3 y 4 recomendaciones concretas, de máximo 30 palabras cada una, sobre mantenimiento preventivo, optimización de horarios de operación o reemplazo de equipos ineficientes.
                Usa los nombres de los equipos y las cifras de los datos. No inventes datos que no estén arriba.
                """,
                department.getBranch().getCompany().getComercialNamecompany(),
                department.getBranch().getNameBranch(),
                objetivo,
                inicioHistorial, finHistorial, porDia.size(), totalHistorial, promedioDiaConDatos,
                ultimaSemana, semanaAnterior,
                tendencia,
                inicio, p.getEndDatePrediction(), diasPeriodo,
                p.getKwhPrediction(), p.getCostPrediction(), tarifa,
                p.getConfidenceLevelAI(),
                equipos.isEmpty() ? "- Sin registros en el historial\n" : equipos.toString());
    }

    private double sumarEntre(Map<LocalDate, Double> porDia, LocalDate desde, LocalDate hasta) {
        return porDia.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(desde) && !e.getKey().isAfter(hasta))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    private double pendienteDiaria(Map<LocalDate, Double> porDia, LocalDate hasta) {
        if (porDia.isEmpty()) {
            return 0;
        }
        LocalDate primerDia = Collections.min(porDia.keySet());
        List<Double> serie = new ArrayList<>();
        for (LocalDate dia = primerDia; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            serie.add(porDia.getOrDefault(dia, 0.0));
        }
        int n = serie.size();
        double mediaX = (n - 1) / 2.0;
        double mediaY = serie.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double sxy = 0;
        double sxx = 0;
        for (int i = 0; i < n; i++) {
            double dx = i - mediaX;
            sxy += dx * (serie.get(i) - mediaY);
            sxx += dx * dx;
        }
        return (sxx == 0) ? 0 : sxy / sxx;
    }

    private String extraerAnalisis(String respuesta) {
        String limpio = respuesta.replace("*", "").replace("#", "");
        String[] lineas = limpio.split("\\R");
        StringBuilder analisis = new StringBuilder();
        boolean leyendo = false;
        for (String linea : lineas) {
            String texto = linea.trim();
            String mayus = texto.toUpperCase(Locale.ROOT);
            if (mayus.startsWith("ANALISIS") || mayus.startsWith("ANÁLISIS")) {
                leyendo = true;
                int dosPuntos = texto.indexOf(':');
                texto = dosPuntos >= 0 ? texto.substring(dosPuntos + 1).trim() : "";
            } else if (mayus.startsWith("RECOMENDACIONES")) {
                break;
            }
            if (leyendo && !texto.isEmpty()) {
                if (!analisis.isEmpty()) {
                    analisis.append(' ');
                }
                analisis.append(texto);
            }
        }
        return analisis.toString().trim();
    }

    private List<String> extraerRecomendaciones(String respuesta) {
        String limpio = respuesta.replace("*", "").replace("#", "");
        String[] lineas = limpio.split("\\R");
        List<String> recomendaciones = new ArrayList<>();
        boolean leyendo = false;
        for (String linea : lineas) {
            String texto = linea.trim();
            if (texto.toUpperCase(Locale.ROOT).startsWith("RECOMENDACIONES")) {
                leyendo = true;
                continue;
            }
            if (leyendo && !texto.isEmpty()) {
                String item = texto.replaceFirst("^(-|•|\\d+[.)])\\s*", "").trim();
                if (!item.isEmpty()) {
                    recomendaciones.add(item);
                }
            }
        }
        return recomendaciones.stream().limit(4).toList();
    }

    private String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1).trim() + "…";
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}