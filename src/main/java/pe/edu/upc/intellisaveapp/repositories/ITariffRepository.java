package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Tariff;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ITariffRepository extends JpaRepository<Tariff, Long> {
    List<Tariff> findByBranch_IdBranch(Long idBranch);

    // Tarifa vigente: la fecha dada está entre el inicio y el fin de vigencia
    List<Tariff> findByBranch_IdBranchAndInitialEffectiveDateLessThanEqualAndEndEffectiveDateGreaterThanEqual(
            Long idBranch, LocalDate fecha1, LocalDate fecha2);
}