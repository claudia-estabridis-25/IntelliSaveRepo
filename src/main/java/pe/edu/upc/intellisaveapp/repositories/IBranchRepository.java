package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Branch;

import java.util.List;

@Repository
public interface IBranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByCompany_IdCompany(Long idCompany);
}