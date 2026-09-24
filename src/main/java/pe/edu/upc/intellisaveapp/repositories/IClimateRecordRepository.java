package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ClimateRecord;

@Repository
public interface IClimateRecordRepository extends JpaRepository<ClimateRecord, Long> {

}
