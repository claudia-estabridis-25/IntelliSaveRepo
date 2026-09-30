package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.dtos.PredictionDTOList;
import pe.edu.upc.intellisaveapp.dtos.PredictionResultDTO;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.entities.Equipment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IConsumptionPredictionService {
    public PredictionResultDTO generate(Department department, Equipment equipment,
                                        LocalDate inicio, LocalDate fin);
    public List<PredictionDTOList> list(Long idDepartment, Long idEquipment, String status);
    public Optional<PredictionDTOList> listById(Long id);
    public List<PredictionDTOList> evaluateFinishedPredictions();
}