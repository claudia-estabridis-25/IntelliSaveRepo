package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.repositories.IConsumptionRecordRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;

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