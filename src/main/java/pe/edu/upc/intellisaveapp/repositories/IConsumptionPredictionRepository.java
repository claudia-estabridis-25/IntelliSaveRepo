package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ConsumptionPrediction;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IConsumptionPredictionRepository extends JpaRepository<ConsumptionPrediction, Long> {
    // Predicciones en un estado cuyo periodo terminó antes de la fecha dada (para HU055)
    List<ConsumptionPrediction> findByStatusPredictionAndEndDatePredictionBefore(String statusPrediction,
                                                                                 LocalDate fecha);
    // Consulta nativa con JOIN: cantidad de predicciones, kWh predicho y confianza promedio por área y estado
    @Query(value = "SELECT d.id_department, d.name_department, p.status_prediction, " +
            "COUNT(p.id_prediction) AS total_predicciones, " +
            "SUM(p.kwh_prediction) AS kwh_predicho, " +
            "AVG(p.confidence_levelai) AS confianza_promedio " +
            "FROM predictions p " +
            "JOIN departments d ON p.id_department = d.id_department " +
            "GROUP BY d.id_department, d.name_department, p.status_prediction " +
            "ORDER BY d.id_department, p.status_prediction",
            nativeQuery = true)
    List<Object[]> predictionsByDepartment();
}