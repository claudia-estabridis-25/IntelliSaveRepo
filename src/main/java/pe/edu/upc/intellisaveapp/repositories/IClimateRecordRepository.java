package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ClimateRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface IClimateRecordRepository extends JpaRepository<ClimateRecord, Long> {
    List<ClimateRecord> findByBranch_IdBranch(Long idBranch);

    // Último registro de clima de una sede
    Optional<ClimateRecord> findFirstByBranch_IdBranchOrderByClimateDateTimeDesc(Long idBranch);
}