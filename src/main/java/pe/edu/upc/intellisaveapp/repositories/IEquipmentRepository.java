package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Equipment;

import java.util.List;

@Repository
public interface IEquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatusEquipmentIgnoreCase(String statusEquipment);

    @Query(value = "SELECT c.id_company, c.comercial_namecompany, e.category_equipment, " +
            "COUNT(e.id_equipment) AS total_equipos, " +
            "COUNT(CASE WHEN e.status_equipment = 'Activo' THEN 1 END) AS equipos_activos, " +
            "COUNT(CASE WHEN e.status_equipment = 'Inactivo' THEN 1 END) AS equipos_inactivos, " +
            "COALESCE(SUM(CASE WHEN e.status_equipment = 'Activo' THEN e.watt_power_equipment END), 0) AS potencia_activa_w, " +
            "COALESCE(SUM(cr.total_kwh), 0) AS total_kwh, " +
            "COALESCE(SUM(cr.total_costo), 0) AS total_costo " +
            "FROM equipments e " +
            "JOIN departments d ON e.id_department = d.id_department " +
            "JOIN branches b ON d.id_branch = b.id_branch " +
            "JOIN companies c ON b.id_company = c.id_company " +
            "LEFT JOIN (SELECT id_equipment, SUM(kwh_consumption) AS total_kwh, SUM(cost_total) AS total_costo " +
            "FROM consumption_records GROUP BY id_equipment) cr ON cr.id_equipment = e.id_equipment " +
            "GROUP BY c.id_company, c.comercial_namecompany, e.category_equipment " +
            "ORDER BY c.id_company, total_kwh DESC",
            nativeQuery = true)
    List<Object[]> equipmentReportByCategory();
}