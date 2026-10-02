package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.dtos.AlertCountDTO;
import pe.edu.upc.intellisaveapp.dtos.AlertDepartmentCountDTO;
import pe.edu.upc.intellisaveapp.entities.Alert;
import pe.edu.upc.intellisaveapp.entities.Department;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IAlertService {
    public List<Alert> list();
    public List<Alert> listByDepartment(Long idDepartment);
    public List<Alert> listByEquipment(Long idEquipment);
    public List<Alert> listByStatus(String status);
    public List<Alert> history(String type, LocalDateTime desde, LocalDateTime hasta);
    public void insert(Alert a);
    public void update(Alert a);
    public Optional<Alert> listById(Long id);
    public void delete(Long id);
    public List<AlertCountDTO> countByStatus();
    public List<AlertCountDTO> countByPriority();
    public List<AlertDepartmentCountDTO> countByDepartment();
    public List<Alert> generateElevatedConsumptionAlerts(Department department,
                                                         LocalDateTime desde, LocalDateTime hasta);
}