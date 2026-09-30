package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Users;


import java.util.Optional;

@Repository
public interface IUsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmailUser(String emailUser);
}
