package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.repositories.IConsumptionRecordRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;
import pe.edu.upc.intellisaveapp.dtos.CategoryConsumptionDTO;
import pe.edu.upc.intellisaveapp.dtos.DepartmentConsumptionDTO;

import java.time.LocalDateTime;
import java.util.Comparator;
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
    public List<ConsumptionRecord> listHistory(Long idBranch, Long idDepartment, Long idEquipment,
                                               LocalDateTime desde, LocalDateTime hasta) {
        return crR.findAll()
                .stream()
                .filter(cr -> idEquipment == null
                        || cr.getEquipment().getIdEquipment().equals(idEquipment))
                .filter(cr -> idDepartment == null
                        || cr.getEquipment().getDepartment().getIdDepartment().equals(idDepartment))
                .filter(cr -> idBranch == null
                        || cr.getEquipment().getDepartment().getBranch().getIdBranch().equals(idBranch))
                .filter(cr -> desde == null || !cr.getDateTimeRecord().isBefore(desde))
                .filter(cr -> hasta == null || !cr.getDateTimeRecord().isAfter(hasta))
                .sorted(Comparator.comparing(ConsumptionRecord::getDateTimeRecord).reversed())
                .toList();
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

    @Override
    public List<DepartmentConsumptionDTO> consumptionByDepartmentOfBranch(Long idBranch) {
        return crR.consumptionByDepartmentOfBranch(idBranch)
                .stream()
                .map(fila -> new DepartmentConsumptionDTO(
                        ((Number) fila[0]).longValue(),       // id_department
                        (String) fila[1],                     // name_department
                        ((Number) fila[2]).longValue(),       // total_registros
                        redondear(((Number) fila[3]).doubleValue()),  // total_kwh
                        redondear(((Number) fila[4]).doubleValue())   // total_costo
                ))
                .toList();
    }

    @Override
    public List<CategoryConsumptionDTO> consumptionByEquipmentCategory(LocalDateTime desde, LocalDateTime hasta) {
        return crR.consumptionByEquipmentCategory(desde, hasta)
                .stream()
                .map(fila -> new CategoryConsumptionDTO(
                        (String) fila[0],                     // category_equipment
                        ((Number) fila[1]).longValue(),       // cantidad_equipos
                        ((Number) fila[2]).longValue(),       // total_registros
                        redondear(((Number) fila[3]).doubleValue()),  // total_kwh
                        redondear(((Number) fila[4]).doubleValue())   // total_costo
                ))
                .toList();
    }

    private Double redondear(Double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}