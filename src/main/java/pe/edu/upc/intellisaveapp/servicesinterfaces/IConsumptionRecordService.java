package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;

import java.util.List;
import java.util.Optional;

public interface IConsumptionRecordService {
    public List<ConsumptionRecord> list();
    public void insert(ConsumptionRecord cr);
    public void update(ConsumptionRecord cr);
    public Optional<ConsumptionRecord> listById(Long id);
    public void delete(Long id);
}