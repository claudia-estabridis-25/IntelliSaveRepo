package pe.edu.upc.intellisaveapp.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upc.intellisaveapp.dtos.ClimateRefreshResultDTO;
import pe.edu.upc.intellisaveapp.entities.Alert;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IAlertService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IClimateRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;

import java.time.LocalDateTime;
import java.util.List;

/* Esta clase ScheduledTasks ejecuta tareas automáticas cada cierto tiempo, sin que nadie
llame a un endpoint. Así se cumplen las HU que dicen "automáticamente":
- HU17: el sistema consulta el clima de todas las sedes cada hora.
- HU21: el sistema analiza los consumos de cada área y genera alertas cada noche.
@Scheduled(cron = ...) indica cuándo se ejecuta cada método (segundo, minuto, hora, día, mes, día de la semana).
Para que funcione, la clase principal (IntelliSaveAppApplication) tiene la anotación @EnableScheduling.
Los endpoints manuales (POST /api/climate-records/refresh y POST /api/alerts/department/{id}/generate)
siguen funcionando igual; estas tareas solo reutilizan los mismos métodos de los services.
*/

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    // Días hacia atrás que se analizan para detectar consumos anormales
    private static final int DIAS_ANALISIS_ALERTAS = 7;

    private final IClimateRecordService cS;
    private final IAlertService aS;
    private final IDepartmentService dS;

    public ScheduledTasks(IClimateRecordService cS, IAlertService aS, IDepartmentService dS) {
        this.cS = cS;
        this.aS = aS;
        this.dS = dS;
    }

    // HU17: consultar automáticamente el clima de todas las sedes (cada hora, en el minuto 0)
    @Scheduled(cron = "0 0 * * * *", zone = "America/Lima")
    public void actualizarClimaSedes() {
        ClimateRefreshResultDTO resultado = cS.refreshAllBranches();
        log.info("Tarea automática de clima ejecutada: {} de {} sedes actualizadas. Errores: {}",
                resultado.getSavedRecords(), resultado.getTotalBranches(), resultado.getErrors());
    }

    // HU21: detectar automáticamente consumos anormales de la última semana (todos los días a las 23:00)
    @Scheduled(cron = "0 0 23 * * *", zone = "America/Lima")
    public void detectarConsumosAnormales() {
        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = hasta.minusDays(DIAS_ANALISIS_ALERTAS);
        int totalAlertas = 0;

        for (Department department : dS.list()) {
            // Si falla un área, se registra el error y se continúa con las demás
            try {
                List<Alert> generadas = aS.generateElevatedConsumptionAlerts(department, desde, hasta);
                totalAlertas += generadas.size();
            } catch (RuntimeException e) {
                log.error("No se pudo analizar el consumo del área {}: {}",
                        department.getNameDepartment(), e.getMessage());
            }
        }

        log.info("Tarea automática de alertas ejecutada: {} alertas generadas", totalAlertas);
    }
}
