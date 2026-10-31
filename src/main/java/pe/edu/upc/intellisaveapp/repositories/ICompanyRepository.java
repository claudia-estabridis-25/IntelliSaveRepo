package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Company;

import java.util.List;
// @Repository -> Pertenece a Spring Data

// @Repository es una anotación de Spring que indica que esta interfaz es un repositorio de Spring Data JPA,
// lo que permite a Spring manejar la persistencia de datos y las operaciones de base de datos para la entidad Company.
// JpaRepository es una interfaz de Spring Data JPA que proporciona métodos CRUD (Create, Read, Update, Delete) y
// de paginación para la entidad Company. Al extender JpaRepository, ICompanyRepository hereda automáticamente estos
// métodos y puede definir consultas personalizadas mediante anotaciones como @Query.
@Repository
public interface ICompanyRepository extends JpaRepository<Company, Long> {
    // Consulta simple 1: cantidad de empresas por sector
    @Query(value = "SELECT c.sector_company, COUNT(c.id_company) AS total_empresas " +
            "FROM companies c " +
            "GROUP BY c.sector_company " +
            "ORDER BY total_empresas DESC, c.sector_company",
            nativeQuery = true)
    List<Object[]> countCompaniesBySector();

    // Consulta simple 2: buscar empresas por RUC (metodo derivado de Spring)
    List<Company> findByRucCompany(String rucCompany);

    // Consulta con JOIN 1: cantidad de sedes y áreas por empresa
    // LEFT JOIN para que también salgan las empresas sin sedes o sedes sin áreas (con conteo 0)
    @Query(value = "SELECT c.id_company, c.formal_name_company, " +
            "COUNT(DISTINCT b.id_branch) AS total_sedes, " +
            "COUNT(d.id_department) AS total_areas " +
            "FROM companies c " +
            "LEFT JOIN branches b ON b.id_company = c.id_company " +
            "LEFT JOIN departments d ON d.id_branch = b.id_branch " +
            "GROUP BY c.id_company, c.formal_name_company " +
            "ORDER BY c.id_company",
            nativeQuery = true)
    List<Object[]> structureByCompany();

}
