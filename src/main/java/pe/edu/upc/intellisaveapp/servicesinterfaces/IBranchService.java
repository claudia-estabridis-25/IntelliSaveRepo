package pe.edu.upc.intellisaveapp.servicesinterfaces;

import org.springframework.data.repository.query.Param;
import pe.edu.upc.intellisaveapp.dtos.BranchStructureDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;

import java.util.List;
import java.util.Optional;

public interface IBranchService {
    public List<Branch> list();
    public List<Branch> listByCompany(Long idCompany);
    public void insert(Branch b);
    public void update(Branch b);
    public Optional<Branch> listById(Long id);
    public void delete(Long id);
    public List<Object[]> structureByBranch(); //cantidad de áreas y equipos por sede
    //cantidad de equipos activos e inactivos por cada área de cada sede
    public List<Object[]> equipmentStatusByDepartment();
    //consumo total (kWh) y costo total (S/) de cada sede de una empresa
    public List<Object[]> consumptionByBranchOfCompany(Long idCompany);
    //potencia instalada total (W) por sede (de sus equipos activos)
    public List<Object[]> installedPowerByBranch();
}