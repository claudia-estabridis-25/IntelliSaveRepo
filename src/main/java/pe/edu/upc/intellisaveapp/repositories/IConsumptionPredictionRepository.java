package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ConsumptionPrediction;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IConsumptionPredictionRepository extends JpaRepository<ConsumptionPrediction, Long> {
    // Predicciones en un estado cuyo periodo terminó antes de la fecha dada (para HU055)
    List<ConsumptionPrediction> findByStatusPredictionAndEndDatePredictionBefore(String statusPrediction,
                                                                                 LocalDate fecha);
}