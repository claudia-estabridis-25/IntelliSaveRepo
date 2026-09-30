package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long> {

    List<ConsumptionRecord> findByEquipment_Department_IdDepartment(Long idDepartment);

    List<ConsumptionRecord> findByEquipment_Department_Branch_IdBranch(Long idBranch);

    List<ConsumptionRecord> findByEquipment_IdEquipment(Long idEquipment);

    List<ConsumptionRecord> findByDateTimeRecordBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT AVG(cr.kwhConsumption) FROM ConsumptionRecord cr WHERE cr.equipment.department.idDepartment = :idDepartment")
    Double averageKwhByDepartment(@Param("idDepartment") Long idDepartment);

    // Consulta nativa 1: consumo total por área de una sede
    @Query(value = "SELECT d.id_department, d.name_department, " +
            "COUNT(cr.id_consumption_record) AS total_registros, " +
            "SUM(cr.kwh_consumption) AS total_kwh, " +
            "SUM(cr.cost_total) AS total_costo " +
            "FROM consumption_records cr " +
            "JOIN equipments e ON cr.id_equipment = e.id_equipment " +
            "JOIN departments d ON e.id_department = d.id_department " +
            "WHERE d.id_branch = :idBranch " +
            "GROUP BY d.id_department, d.name_department " +
            "ORDER BY total_kwh DESC",
            nativeQuery = true)
    List<Object[]> consumptionByDepartmentOfBranch(@Param("idBranch") Long idBranch);

    // Consulta nativa 2: consumo por categoría de equipo en un periodo
    @Query(value = "SELECT e.category_equipment, " +
            "COUNT(DISTINCT e.id_equipment) AS cantidad_equipos, " +
            "COUNT(cr.id_consumption_record) AS total_registros, " +
            "SUM(cr.kwh_consumption) AS total_kwh, " +
            "SUM(cr.cost_total) AS total_costo " +
            "FROM consumption_records cr " +
            "JOIN equipments e ON cr.id_equipment = e.id_equipment " +
            "WHERE cr.date_time_record BETWEEN :desde AND :hasta " +
            "GROUP BY e.category_equipment " +
            "ORDER BY total_kwh DESC",
            nativeQuery = true)
    List<Object[]> consumptionByEquipmentCategory(@Param("desde") LocalDateTime desde,
                                                  @Param("hasta") LocalDateTime hasta);
}