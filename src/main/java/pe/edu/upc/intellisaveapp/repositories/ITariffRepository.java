package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Tariff;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ITariffRepository extends JpaRepository<Tariff, Long> {
    List<Tariff> findByBranch_IdBranch(Long idBranch);

    // Tarifa vigente: la fecha dada está entre el inicio y el fin de vigencia
    List<Tariff> findByBranch_IdBranchAndInitialEffectiveDateLessThanEqualAndEndEffectiveDateGreaterThanEqual(
            Long idBranch, LocalDate fecha1, LocalDate fecha2);

    // Consulta nativa con JOIN: cantidad de tarifas y costo promedio por proveedor y sede
    @Query(value = "SELECT b.id_branch, b.name_branch, COALESCE(t.supplier, 'Sin proveedor') AS proveedor, " +
            "COUNT(t.id_tariff) AS total_tarifas, " +
            "AVG(t.cost_per_kwh) AS costo_promedio " +
            "FROM tariffs t " +
            "JOIN branches b ON t.id_branch = b.id_branch " +
            "GROUP BY b.id_branch, b.name_branch, COALESCE(t.supplier, 'Sin proveedor') " +
            "ORDER BY b.id_branch, proveedor",
            nativeQuery = true)
    List<Object[]> tariffsBySupplier();

}