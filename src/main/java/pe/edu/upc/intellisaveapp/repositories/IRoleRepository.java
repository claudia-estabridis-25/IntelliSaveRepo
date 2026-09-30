package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Role;

import java.util.List;

@Repository
public interface IRoleRepository extends JpaRepository<Role,Long> {
    List<Role> findByUser_IdUser(Long idUser);
}
