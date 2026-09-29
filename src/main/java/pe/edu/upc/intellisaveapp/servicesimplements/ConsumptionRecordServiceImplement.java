package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.repositories.IConsumptionRecordRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsumptionRecordServiceImplement implements IConsumptionRecordService {
    private final IConsumptionRecordRepository crR;

    public ConsumptionRecordServiceImplement(IConsumptionRecordRepository crR) {
        this.crR = crR;
    }

    @Override
    public List<ConsumptionRecord> list() {
        return crR.findAll();
    }

    @Override
    public List<ConsumptionRecord> listByDepartment(Long idDepartment) {
        return crR.findByEquipment_Department_IdDepartment(idDepartment);
    }

    @Override
    public List<ConsumptionRecord> listByBranch(Long idBranch) {
        return crR.findByEquipment_Department_Branch_IdBranch(idBranch);
    }

    @Override
    public List<ConsumptionRecord> listByEquipment(Long idEquipment) {
        return crR.findByEquipment_IdEquipment(idEquipment);
    }

    @Override
    public List<ConsumptionRecord> listByDateRange(LocalDateTime desde, LocalDateTime hasta) {
        return crR.findByDateTimeRecordBetween(desde, hasta);
    }

    @Override
    public Double averageKwhByDepartment(Long idDepartment) {
        Double promedio = crR.averageKwhByDepartment(idDepartment);
        return promedio != null ? promedio : 0.0;
    }

    @Override
    public void insert(ConsumptionRecord cr) {
        crR.save(cr);
    }

    @Override
    public void update(ConsumptionRecord cr) {
        crR.save(cr);
    }

    @Override
    public Optional<ConsumptionRecord> listById(Long id) {
        return crR.findById(id);
    }

    @Override
    public void delete(Long id) {
        crR.deleteById(id);
    }
}