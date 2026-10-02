package pe.edu.upc.intellisaveapp.servicesimplements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.dtos.AlertCountDTO;
import pe.edu.upc.intellisaveapp.dtos.AlertDepartmentCountDTO;
import pe.edu.upc.intellisaveapp.entities.Alert;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.repositories.IAlertRepository;
import pe.edu.upc.intellisaveapp.repositories.IUsersRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IAlertService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertServiceImplement implements IAlertService {
    private static final Logger log = LoggerFactory.getLogger(AlertServiceImplement.class);

    // HU21: tipos de alerta que el sistema detecta automáticamente
    private static final String TIPO_EXCESIVO = "Consumo excesivo";
    private static final String TIPO_PICO = "Pico anormal";

    // HU16 y HU056: estados de una alerta
    private static final String PENDIENTE = "Pendiente";
    private static final List<String> ESTADOS_ABIERTOS = List.of("Pendiente", "En revisión");
    private static final List<String> ESTADOS_CERRADOS = List.of("Atendida", "Descartada");

    // Consumo excesivo: se reutiliza el umbral de 30% de HU012
    private static final double UMBRAL_ELEVADO = 1.3;  // 30% o más sobre el promedio del área
    private static final double UMBRAL_MEDIA = 1.6;    // 60% o más: prioridad Media
    private static final double UMBRAL_ALTA = 2.0;     // el doble o más: prioridad Alta

    // Pico anormal: un registro que supera el doble del promedio de los demás registros del equipo
    private static final int MIN_REGISTROS_PICO = 4;
    private static final double UMBRAL_PICO = 2.0;

    private final IAlertRepository aR;
    private final IConsumptionRecordService crS;
    private final IUsersRepository uR;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailFrom;

    public AlertServiceImplement(IAlertRepository aR, IConsumptionRecordService crS, IUsersRepository uR,
                                 ObjectProvider<JavaMailSender> mailSenderProvider,
                                 @Value("${app.alerts.mail-from:no-reply@intellisave.pe}") String mailFrom) {
        this.aR = aR;
        this.crS = crS;
        this.uR = uR;
        this.mailSenderProvider = mailSenderProvider;
        this.mailFrom = mailFrom;
    }

    @Override
    public List<Alert> list() {
        return aR.findAll();
    }

    @Override
    public List<Alert> listByDepartment(Long idDepartment) {
        return aR.findByDepartment_IdDepartment(idDepartment);
    }

    @Override
    public List<Alert> listByEquipment(Long idEquipment) {
        return aR.findByEquipment_IdEquipment(idEquipment);
    }

    @Override
    public List<Alert> listByStatus(String status) {
        return aR.findByStatusAlertIgnoreCase(status);
    }

    // HU056: historial de alertas atendidas o descartadas, con filtros por tipo y rango de fechas
    @Override
    public List<Alert> history(String type, LocalDateTime desde, LocalDateTime hasta) {
        return aR.findByStatusAlertIn(ESTADOS_CERRADOS)
                .stream()
                .filter(a -> type == null || a.getTypeAlert().equalsIgnoreCase(type))
                .filter(a -> desde == null || !a.getDateTimeAlert().isBefore(desde))
                .filter(a -> hasta == null || !a.getDateTimeAlert().isAfter(hasta))
                .sorted(Comparator.comparing(Alert::getDateTimeAlert).reversed())
                .toList();
    }

    @Override
    public void insert(Alert a) {
        aR.save(a);
        notifyIfCritical(a);
    }

    @Override
    public void update(Alert a) {
        aR.save(a);
    }

    @Override
    public Optional<Alert> listById(Long id) {
        return aR.findById(id);
    }

    @Override
    public void delete(Long id) {
        aR.deleteById(id);
    }

    @Override
    public List<AlertCountDTO> countByStatus() {
        return aR.countAlertsByStatus()
                .stream()
                .map(row -> new AlertCountDTO((String) row[0], (Long) row[1]))
                .toList();
    }

    @Override
    public List<AlertCountDTO> countByPriority() {
        return aR.countAlertsByPriority()
                .stream()
                .map(row -> new AlertCountDTO((String) row[0], (Long) row[1]))
                .toList();
    }

    @Override
    public List<AlertDepartmentCountDTO> countByDepartment() {
        return aR.countAlertsByDepartment()
                .stream()
                .map(row -> new AlertDepartmentCountDTO(
                        ((Number) row[0]).longValue(),   // id_department
                        (String) row[1],                 // name_department
                        (String) row[2],                 // type_alert
                        (String) row[3],                 // status_alert
                        ((Number) row[4]).longValue()    // total_alertas
                ))
                .toList();
    }

    // HU21: analiza los consumos del área y genera alertas (Consumo excesivo y Pico anormal)
    @Override
    public List<Alert> generateElevatedConsumptionAlerts(Department department,
                                                         LocalDateTime desde, LocalDateTime hasta) {
        List<ConsumptionRecord> registros = crS
                .listHistory(null, department.getIdDepartment(), null, desde, hasta);

        List<Alert> generadas = new ArrayList<>();
        if (registros.isEmpty()) {
            return generadas;
        }

        detectarConsumoExcesivo(department, registros, generadas);
        detectarPicos(department, registros, generadas);

        return generadas;
    }

    // ===================== DETECCIÓN =====================

    // Un equipo consume 30% o más sobre el promedio de los equipos de su área (misma regla de HU012)
    private void detectarConsumoExcesivo(Department department, List<ConsumptionRecord> registros,
                                         List<Alert> generadas) {
        Map<Equipment, Double> totalPorEquipo = registros.stream()
                .collect(Collectors.groupingBy(
                        ConsumptionRecord::getEquipment,
                        Collectors.summingDouble(ConsumptionRecord::getKwhConsumption)
                ));

        double promedio = totalPorEquipo.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        if (promedio == 0) {
            return;
        }

        for (Map.Entry<Equipment, Double> entry : totalPorEquipo.entrySet()) {
            Equipment equipment = entry.getKey();
            double total = entry.getValue();
            double proporcion = total / promedio;

            if (proporcion < UMBRAL_ELEVADO
                    || estaInactivo(equipment)
                    || tieneAlertaAbierta(equipment, TIPO_EXCESIVO)) {
                continue;
            }

            generadas.add(crearAlerta(department, equipment, TIPO_EXCESIVO,
                    descripcionExcesivo(equipment, proporcion), prioridadExcesivo(proporcion), total));
        }
    }

    // Un registro individual supera el doble del promedio de los demás registros del mismo equipo
    private void detectarPicos(Department department, List<ConsumptionRecord> registros,
                               List<Alert> generadas) {
        Map<Equipment, List<ConsumptionRecord>> porEquipo = registros.stream()
                .collect(Collectors.groupingBy(ConsumptionRecord::getEquipment));

        for (Map.Entry<Equipment, List<ConsumptionRecord>> entry : porEquipo.entrySet()) {
            Equipment equipment = entry.getKey();
            List<ConsumptionRecord> lista = entry.getValue();

            if (lista.size() < MIN_REGISTROS_PICO
                    || estaInactivo(equipment)
                    || tieneAlertaAbierta(equipment, TIPO_PICO)) {
                continue;
            }

            double maximo = lista.stream().mapToDouble(ConsumptionRecord::getKwhConsumption).max().orElse(0.0);
            double suma = lista.stream().mapToDouble(ConsumptionRecord::getKwhConsumption).sum();
            double promedioDemas = (suma - maximo) / (lista.size() - 1);

            if (promedioDemas == 0) {
                continue;
            }

            double proporcion = maximo / promedioDemas;
            if (proporcion < UMBRAL_PICO) {
                continue;
            }

            generadas.add(crearAlerta(department, equipment, TIPO_PICO,
                    descripcionPico(equipment, maximo, proporcion), prioridadPico(proporcion), maximo));
        }
    }

    // HU21 CA02: se guarda con estado "Pendiente"
    private Alert crearAlerta(Department department, Equipment equipment, String tipo,
                              String descripcion, String prioridad, double kwh) {
        Alert alert = new Alert();
        alert.setDepartment(department);
        alert.setEquipment(equipment);
        alert.setDateTimeAlert(LocalDateTime.now());
        alert.setTypeAlert(tipo);
        alert.setDescriptionAlert(recortar(descripcion));
        alert.setPriorityLevelAlert(prioridad);
        alert.setKwhDetected(redondear(kwh));
        alert.setStatusAlert(PENDIENTE);

        aR.save(alert);
        notifyIfCritical(alert);

        return alert;
    }

    // ===================== HU049: CORREO POR ALERTA CRÍTICA =====================

    private void notifyIfCritical(Alert alert) {
        // CA03: solo las alertas de prioridad Alta generan correo
        if (!"Alta".equalsIgnoreCase(alert.getPriorityLevelAlert())) {
            return;
        }

        // CA04: un fallo del correo nunca afecta al guardado de la alerta
        try {
            List<String> destinatarios = uR.findSupervisorEmailsByDepartment(alert.getDepartment().getIdDepartment());
            if (destinatarios.isEmpty()) {
                log.warn("La alerta {} es crítica, pero el área no tiene un supervisor activo", alert.getIdAlert());
                return;
            }

            JavaMailSender sender = mailSenderProvider.getIfAvailable();
            if (sender == null) {
                log.warn("Correo no configurado (spring.mail.host). Se habría notificado a {}", destinatarios);
                return;
            }

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(mailFrom);
            mensaje.setTo(destinatarios.toArray(new String[0]));
            mensaje.setSubject("IntelliSave: alerta crítica - " + alert.getTypeAlert());
            mensaje.setText(cuerpoCorreo(alert));
            sender.send(mensaje);

            log.info("Correo de la alerta {} enviado a {}", alert.getIdAlert(), destinatarios);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de la alerta {}: {}", alert.getIdAlert(), e.getMessage());
        }
    }

    // CA02: tipo de alerta, equipo o área afectada y fecha y hora de detección
    private String cuerpoCorreo(Alert alert) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "Se detectó una alerta de prioridad ALTA en IntelliSave.\n\n"
                + "Tipo de alerta: " + alert.getTypeAlert() + "\n"
                + "Equipo afectado: " + alert.getEquipment().getNameEquipment() + "\n"
                + "Área: " + alert.getDepartment().getNameDepartment() + "\n"
                + "Fecha y hora de detección: " + alert.getDateTimeAlert().format(formato) + "\n"
                + "Consumo detectado: " + alert.getKwhDetected() + " kWh\n"
                + "Detalle: " + alert.getDescriptionAlert() + "\n";
    }

    // ===================== MÉTODOS DE APOYO =====================

    private boolean estaInactivo(Equipment equipment) {
        return "Inactivo".equalsIgnoreCase(equipment.getStatusEquipment());
    }

    private boolean tieneAlertaAbierta(Equipment equipment, String tipo) {
        return aR.existsByEquipment_IdEquipmentAndTypeAlertIgnoreCaseAndStatusAlertIn(
                equipment.getIdEquipment(), tipo, ESTADOS_ABIERTOS);
    }

    private String prioridadExcesivo(double proporcion) {
        if (proporcion >= UMBRAL_ALTA) {
            return "Alta";
        }
        if (proporcion >= UMBRAL_MEDIA) {
            return "Media";
        }
        return "Baja";
    }

    private String prioridadPico(double proporcion) {
        if (proporcion >= 4) {
            return "Alta";
        }
        if (proporcion >= 3) {
            return "Media";
        }
        return "Baja";
    }

    private String descripcionExcesivo(Equipment equipment, double proporcion) {
        long porcentaje = Math.round((proporcion - 1) * 100);
        return equipment.getNameEquipment() + " consume " + porcentaje + "% más que el promedio del área";
    }

    private String descripcionPico(Equipment equipment, double maximo, double proporcion) {
        double veces = Math.round(proporcion * 10) / 10.0;
        return "Pico de " + redondear(maximo) + " kWh en " + equipment.getNameEquipment()
                + " (" + veces + " veces su promedio)";
    }

    // La columna admite como máximo 90 caracteres
    private String recortar(String texto) {
        return texto.length() > 90 ? texto.substring(0, 90) : texto;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}