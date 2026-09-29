package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IConsumptionRecordService {
    public List<ConsumptionRecord> list();
    public List<ConsumptionRecord> listByDepartment(Long idDepartment);
    public List<ConsumptionRecord> listByBranch(Long idBranch);
    public List<ConsumptionRecord> listByEquipment(Long idEquipment);
    public List<ConsumptionRecord> listByDateRange(LocalDateTime desde, LocalDateTime hasta);
    public Double averageKwhByDepartment(Long idDepartment);
    public void insert(ConsumptionRecord cr);
    public void update(ConsumptionRecord cr);
    public Optional<ConsumptionRecord> listById(Long id);
    public void delete(Long id);
}