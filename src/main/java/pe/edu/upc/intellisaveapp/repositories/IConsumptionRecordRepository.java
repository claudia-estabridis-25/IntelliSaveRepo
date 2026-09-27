package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;

@Repository
public interface IConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long> {

}