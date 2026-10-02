package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Users;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmailUser(String emailUser);

    Optional<Users> findByDniUser(String dniUser);

    // Consulta con JOIN 1: cantidad de usuarios por rol (total, activos e inactivos)
    @Query(value = "SELECT r.name_role, " +
            "COUNT(u.id_user) AS total_usuarios, " +
            "COUNT(CASE WHEN u.status_user = true THEN 1 END) AS usuarios_activos, " +
            "COUNT(CASE WHEN u.status_user = false THEN 1 END) AS usuarios_inactivos " +
            "FROM roles r " +
            "JOIN users u ON r.id_user = u.id_user " +
            "GROUP BY r.name_role " +
            "ORDER BY total_usuarios DESC, r.name_role",
            nativeQuery = true)
    List<Object[]> countUsersByRole();

    // Consulta con JOIN 2: cantidad de usuarios por cada área de cada sede
    // LEFT JOIN para que también salgan las áreas que todavía no tienen usuarios (con conteo 0)
    @Query(value = "SELECT b.id_branch, b.name_branch, d.id_department, d.name_department, " +
            "COUNT(u.id_user) AS total_usuarios " +
            "FROM departments d " +
            "JOIN branches b ON d.id_branch = b.id_branch " +
            "LEFT JOIN users u ON u.id_department = d.id_department " +
            "GROUP BY b.id_branch, b.name_branch, d.id_department, d.name_department " +
            "ORDER BY b.id_branch, d.id_department",
            nativeQuery = true)
    List<Object[]> countUsersByDepartment();

    // Correos de los supervisores activos de un área (HU049)
    @Query(value = "SELECT u.email_user FROM users u " +
            "JOIN roles r ON r.id_user = u.id_user " +
            "WHERE u.id_department = :idDepartment " +
            "AND r.name_role = 'ROLE_SUPERVISOR' AND u.status_user = true",
            nativeQuery = true)
    List<String> findSupervisorEmailsByDepartment(@Param("idDepartment") Long idDepartment);
}