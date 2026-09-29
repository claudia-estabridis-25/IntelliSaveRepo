package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.repositories.IEquipmentRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentServiceImplement implements IEquipmentService {
    private final IEquipmentRepository eR;

    public EquipmentServiceImplement(IEquipmentRepository eR) {
        this.eR = eR;
    }

    @Override
    public List<Equipment> list() {
        return eR.findAll();
    }

    @Override
    public List<Equipment> listByStatus(String status) {
        return eR.findByStatusEquipmentIgnoreCase(status);
    }

    @Override
    public void insert(Equipment e) {
        eR.save(e);
    }

    @Override
    public void update(Equipment e) {
        eR.save(e);
    }

    @Override
    public Optional<Equipment> listById(Long id) {
        return eR.findById(id);
    }

    @Override
    public void delete(Long id) {
        eR.deleteById(id);
    }
}