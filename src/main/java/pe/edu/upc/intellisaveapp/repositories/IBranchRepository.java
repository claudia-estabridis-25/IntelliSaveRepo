package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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


}