package pe.edu.upc.trabajoavance_kevin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.trabajoavance_kevin.entities.User;
import java.util.List;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    List<User> findByStatus(boolean status);
}
