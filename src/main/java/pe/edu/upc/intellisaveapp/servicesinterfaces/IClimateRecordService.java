package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.dtos.ClimateRefreshResultDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.ClimateRecord;

import java.util.List;
import java.util.Optional;

public interface IClimateRecordService {
    public List<ClimateRecord> list();
    public Optional<ClimateRecord> listById(Long id);
    // Listar historial climático de una sede específica (del más reciente al más antiguo)
    public List<ClimateRecord> listByBranch(Long idBranch);
    // Consulta la API con las coordenadas de la sede y guarda el registro
    public ClimateRecord fetchAndSave(Branch branch);
    // Si la sede no tiene un registro de la última hora, consulta la API
    public void refreshIfOutdated(Branch branch);
    // Consulta la API para todas las sedes; si una falla, sigue con las demás
    public ClimateRefreshResultDTO refreshAllBranches();
}