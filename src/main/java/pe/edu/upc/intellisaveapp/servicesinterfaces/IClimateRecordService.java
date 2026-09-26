package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.ClimateRecord;

import java.util.List;
import java.util.Optional;

public interface IClimateRecordService {
    public List<ClimateRecord> list();
    public Optional<ClimateRecord> listById(Long id);
    // Listar historial climático de una sede específica
    public List<ClimateRecord> listByBranch(Long idBranch);

}
