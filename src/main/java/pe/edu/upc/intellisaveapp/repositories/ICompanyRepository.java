package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Company;

@Repository
public interface ICompanyRepository extends JpaRepository<Company, Long> {

}
