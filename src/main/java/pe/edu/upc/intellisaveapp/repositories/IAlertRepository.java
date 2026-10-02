package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Alert;

import java.util.List;

@Repository
public interface IAlertRepository extends JpaRepository<Alert, Long> {
    // Alertas de un área
    List<Alert> findByDepartment_IdDepartment(Long idDepartment);

    // Alertas de un equipo
    List<Alert> findByEquipment_IdEquipment(Long idEquipment);

    // Alertas por estado (Pendiente, En revisión, Atendida, Descartada)
    List<Alert> findByStatusAlertIgnoreCase(String statusAlert);

    // HU056: alertas que están en alguno de los estados indicados (Atendida, Descartada)
    List<Alert> findByStatusAlertIn(List<String> estados);

    // Evita duplicar una alerta abierta del mismo tipo para el mismo equipo
    boolean existsByEquipment_IdEquipmentAndTypeAlertIgnoreCaseAndStatusAlertIn(
            Long idEquipment, String typeAlert, List<String> estados);

    // Consulta simple: cantidad de alertas por estado
    @Query("SELECT a.statusAlert, COUNT(a) FROM Alert a " +
            "GROUP BY a.statusAlert " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> countAlertsByStatus();

    // Consulta simple: cantidad de alertas por nivel de prioridad
    @Query("SELECT a.priorityLevelAlert, COUNT(a) FROM Alert a " +
            "GROUP BY a.priorityLevelAlert " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> countAlertsByPriority();

    // Consulta nativa con JOIN: cantidad de alertas por área, tipo y estado
    @Query(value = "SELECT d.id_department, d.name_department, a.type_alert, a.status_alert, " +
            "COUNT(a.id_alert) AS total_alertas " +
            "FROM alerts a " +
            "JOIN departments d ON a.id_department = d.id_department " +
            "GROUP BY d.id_department, d.name_department, a.type_alert, a.status_alert " +
            "ORDER BY d.id_department, a.type_alert, a.status_alert",
            nativeQuery = true)
    List<Object[]> countAlertsByDepartment();
}