package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ClimateRecord;

import java.util.List;

@Repository
public interface IClimateRecordRepository extends JpaRepository<ClimateRecord, Long> {
    List<ClimateRecord> findByBranch_IdBranch(Long idBranch);
}
