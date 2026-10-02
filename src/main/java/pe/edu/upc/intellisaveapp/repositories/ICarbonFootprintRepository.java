package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.CarbonFootprint;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ICarbonFootprintRepository extends JpaRepository<CarbonFootprint, Long> {
    // Huellas de carbono de un área
    List<CarbonFootprint> findByDepartment_IdDepartment(Long idDepartment);

    // Huellas de carbono de todas las áreas de una sede
    List<CarbonFootprint> findByDepartment_Branch_IdBranch(Long idBranch);

    // Evita registrar dos veces el mismo cálculo; también sirve para HU050 y HU057 (área)
    List<CarbonFootprint> findByDepartment_IdDepartmentAndTimePeriodIgnoreCaseAndCalculationDate(
            Long idDepartment, String timePeriod, LocalDate calculationDate);

    // HU050 (sede): cálculos de todas las áreas de la sede para un periodo y fecha
    List<CarbonFootprint> findByDepartment_Branch_IdBranchAndTimePeriodIgnoreCaseAndCalculationDate(
            Long idBranch, String timePeriod, LocalDate calculationDate);

    // Consulta con JOIN: emisiones totales de CO2 por área (de mayor a menor)
    @Query("SELECT d.idDepartment, d.nameDepartment, COUNT(cf), " +
            "SUM(cf.kwhTotalConsumption), SUM(cf.co2Emissions) " +
            "FROM CarbonFootprint cf JOIN cf.department d " +
            "GROUP BY d.idDepartment, d.nameDepartment " +
            "ORDER BY SUM(cf.co2Emissions) DESC")
    List<Object[]> emissionsByDepartment();

    // Lo mismo, solo para las áreas de una sede
    @Query("SELECT d.idDepartment, d.nameDepartment, COUNT(cf), " +
            "SUM(cf.kwhTotalConsumption), SUM(cf.co2Emissions) " +
            "FROM CarbonFootprint cf JOIN cf.department d " +
            "WHERE d.branch.idBranch = :idBranch " +
            "GROUP BY d.idDepartment, d.nameDepartment " +
            "ORDER BY SUM(cf.co2Emissions) DESC")
    List<Object[]> emissionsByDepartmentOfBranch(@Param("idBranch") Long idBranch);

    // Consulta nativa con JOIN: emisiones totales de CO2 por sede
    // LEFT JOIN para que también salgan las sedes que todavía no tienen cálculos (con 0)
    @Query(value = "SELECT b.id_branch, b.name_branch, " +
            "COUNT(cf.id_footprint) AS total_calculos, " +
            "COALESCE(SUM(cf.kwh_total_consumption), 0) AS total_kwh, " +
            "COALESCE(SUM(cf.co2_emissions), 0) AS total_co2 " +
            "FROM branches b " +
            "JOIN departments d ON d.id_branch = b.id_branch " +
            "LEFT JOIN carbon_footprints cf ON cf.id_department = d.id_department " +
            "GROUP BY b.id_branch, b.name_branch " +
            "ORDER BY total_co2 DESC, b.id_branch",
            nativeQuery = true)
    List<Object[]> emissionsByBranch();
}