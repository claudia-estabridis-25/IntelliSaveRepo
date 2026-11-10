package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Company;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

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


    // Las siguientes consultas permite borrar una empresa (borran todas las entidades relacionadas a la empresa,
    // para evitar errores de integridad referencial).
    // Además, las consultas están escritas en JPQL, no SQL. Se escriben con los nombres de las clases y atributos
    // de Java (ConsumptionRecord, e.department.branch.company), no con los nombres de las tablas. Así no hay que
    // saber cómo se llaman las columnas en PostgreSQL.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ConsumptionRecord c " +
            "WHERE c.equipment.idEquipment IN (SELECT e.idEquipment FROM Equipment e WHERE e.department.branch.company.idCompany = :idCompany) " +
            "OR c.tariff.idTariff IN (SELECT t.idTariff FROM Tariff t WHERE t.branch.company.idCompany = :idCompany)")
    int deleteConsumptionRecordsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Alert a " +
            "WHERE a.department.idDepartment IN (SELECT d.idDepartment FROM Department d WHERE d.branch.company.idCompany = :idCompany) " +
            "OR a.equipment.idEquipment IN (SELECT e.idEquipment FROM Equipment e WHERE e.department.branch.company.idCompany = :idCompany)")
    int deleteAlertsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ConsumptionPrediction p " +
            "WHERE p.department.idDepartment IN (SELECT d.idDepartment FROM Department d WHERE d.branch.company.idCompany = :idCompany) " +
            "OR p.equipment.idEquipment IN (SELECT e.idEquipment FROM Equipment e WHERE e.department.branch.company.idCompany = :idCompany)")
    int deletePredictionsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CarbonFootprint f " +
            "WHERE f.department.idDepartment IN (SELECT d.idDepartment FROM Department d WHERE d.branch.company.idCompany = :idCompany)")
    int deleteCarbonFootprintsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Equipment e " +
            "WHERE e.department.idDepartment IN (SELECT d.idDepartment FROM Department d WHERE d.branch.company.idCompany = :idCompany)")
    int deleteEquipmentsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Role r " +
            "WHERE r.user.idUser IN (SELECT u.idUser FROM Users u WHERE u.department.branch.company.idCompany = :idCompany)")
    int deleteRolesByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Users u " +
            "WHERE u.department.idDepartment IN (SELECT d.idDepartment FROM Department d WHERE d.branch.company.idCompany = :idCompany)")
    int deleteUsersByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Department d " +
            "WHERE d.branch.idBranch IN (SELECT b.idBranch FROM Branch b WHERE b.company.idCompany = :idCompany)")
    int deleteDepartmentsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Tariff t " +
            "WHERE t.branch.idBranch IN (SELECT b.idBranch FROM Branch b WHERE b.company.idCompany = :idCompany)")
    int deleteTariffsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ClimateRecord c " +
            "WHERE c.branch.idBranch IN (SELECT b.idBranch FROM Branch b WHERE b.company.idCompany = :idCompany)")
    int deleteClimateRecordsByCompany(@Param("idCompany") Long idCompany);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Branch b WHERE b.company.idCompany = :idCompany")
    int deleteBranchesByCompany(@Param("idCompany") Long idCompany);
}
// - @Modifying: Le avisa a Spring que la consulta cambia datos (DELETE o UPDATE) y no es un SELECT.
// Sin esto, Spring da error al ejecutarla.
// - <flushAutomatically = true>: Antes de borrar, guarda en la base cualquier cambio pendiente.
// - <clearAutomatically = true>: Después de borrar, limpia la memoria de JPA para que no queden
// objetos de registros que ya no existen.
// - Los metodos son de tipo int, porque devuelven cuántas filas borró. No lo usamos, pero
// sirve si se imprimirlo para probar.