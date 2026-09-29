package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.Equipment;

import java.util.List;

@Repository
public interface IEquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatusEquipment(String statusEquipment);
}