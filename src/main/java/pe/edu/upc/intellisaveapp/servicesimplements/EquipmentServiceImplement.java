package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.repositories.IEquipmentRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;
import pe.edu.upc.intellisaveapp.dtos.EquipmentCategoryReportDTO;

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

    @Override
    public List<EquipmentCategoryReportDTO> reportByCategory() {
        return eR.equipmentReportByCategory()
                .stream()
                .map(fila -> new EquipmentCategoryReportDTO(
                        ((Number) fila[0]).longValue(),
                        (String) fila[1],
                        (String) fila[2],
                        ((Number) fila[3]).longValue(),
                        ((Number) fila[4]).longValue(),
                        ((Number) fila[5]).longValue(),
                        ((Number) fila[6]).doubleValue(),
                        Math.round(((Number) fila[7]).doubleValue() * 100.0) / 100.0,
                        Math.round(((Number) fila[8]).doubleValue() * 100.0) / 100.0
                ))
                .toList();
    }
}