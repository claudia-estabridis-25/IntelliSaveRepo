package pe.edu.upc.intellisaveapp.servicesinterfaces;

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
    public List<Object[]> structureByBranch(); //cantidad áreas y equipos por sede
}