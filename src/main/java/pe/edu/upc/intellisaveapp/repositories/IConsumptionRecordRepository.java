package pe.edu.upc.intellisaveapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long> {

    //Buscar consumo de equipo por cada área
    List<ConsumptionRecord> findByEquipment_Department_IdDepartment(Long idDepartment);

    //Buscar
    List<ConsumptionRecord> findByEquipment_Department_Branch_IdBranch(Long idBranch);

    //Buscar consumo por equipo
    List<ConsumptionRecord> findByEquipment_IdEquipment(Long idEquipment);

    //Buscar consumo por intervalo de fecha
    List<ConsumptionRecord> findByDateTimeRecordBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT AVG(cr.kwhConsumption) FROM ConsumptionRecord cr WHERE cr.equipment.department.idDepartment = :idDepartment")
    Double averageKwhByDepartment(@Param("idDepartment") Long idDepartment);
}