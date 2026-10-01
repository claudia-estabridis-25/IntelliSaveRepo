package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Branch;

import java.util.List;

@Repository
public interface IBranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByCompany_IdCompany(Long idCompany);

    // Consulta con JOIN 2: cantidad de áreas y de equipos por sede
    @Query(value = "SELECT b.id_branch, b.name_branch, " +
            "COUNT(DISTINCT d.id_department) AS total_areas, " +
            "COUNT(e.id_equipment) AS total_equipos " +
            "FROM branches b " +
            "LEFT JOIN departments d ON d.id_branch = b.id_branch " +
            "LEFT JOIN equipments e ON e.id_department = d.id_department " +
            "GROUP BY b.id_branch, b.name_branch " +
            "ORDER BY b.id_branch",
            nativeQuery = true)
    List<Object[]> structureByBranch();

    // Consulta con JOIN 3: Cantidad de equipos activos e inactivos por cada área de cada sede
    @Query(value = "SELECT b.id_branch, b.name_branch, d.id_department, d.name_department, " +
            "COUNT(CASE WHEN e.status_equipment = 'Activo' THEN 1 END) AS equipos_activos, " +
            "COUNT(CASE WHEN e.status_equipment = 'Inactivo' THEN 1 END) AS equipos_inactivos " +
            "FROM departments d " +
            "JOIN branches b ON d.id_branch = b.id_branch " +
            "LEFT JOIN equipments e ON e.id_department = d.id_department " +
            "GROUP BY b.id_branch, b.name_branch, d.id_department, d.name_department " +
            "ORDER BY b.id_branch, d.id_department",
            nativeQuery = true)
    List<Object[]> equipmentStatusByDepartment();

    // Consulta con JOIN 5: Consumo total (kWh) y costo total (S/) de cada sede de una empresa (se envía idCompany)
    @Query(value = "SELECT b.id_branch, b.name_branch, " +
            "COUNT(cr.id_consumption_record) AS total_registros, " +
            "COALESCE(SUM(cr.kwh_consumption), 0) AS total_kwh, " +
            "COALESCE(SUM(cr.cost_total), 0) AS total_costo " +
            "FROM branches b " +
            "LEFT JOIN departments d ON d.id_branch = b.id_branch " +
            "LEFT JOIN equipments e ON e.id_department = d.id_department " +
            "LEFT JOIN consumption_records cr ON cr.id_equipment = e.id_equipment " +
            "WHERE b.id_company = :idCompany " +
            "GROUP BY b.id_branch, b.name_branch " +
            "ORDER BY total_kwh DESC, b.id_branch",
            nativeQuery = true)
    List<Object[]> consumptionByBranchOfCompany(@Param("idCompany") Long idCompany);

    // Consulta con JOIN 4: potencia instalada total (W) por sede
    // Se refiere a la suma de la potencia en watts de todos los equipos activos que tiene una sede
    @Query(value = "SELECT b.id_branch, b.name_branch, " +
            "COUNT(e.id_equipment) AS total_equipos, " +
            "COALESCE(SUM(e.watt_power_equipment), 0) AS total_watts " +
            "FROM branches b " +
            "LEFT JOIN departments d ON d.id_branch = b.id_branch " +
            "LEFT JOIN equipments e ON e.id_department = d.id_department AND e.status_equipment = 'Activo' " +
            "GROUP BY b.id_branch, b.name_branch " +
            "ORDER BY total_watts DESC, b.id_branch",
            nativeQuery = true)
    List<Object[]> installedPowerByBranch();


}