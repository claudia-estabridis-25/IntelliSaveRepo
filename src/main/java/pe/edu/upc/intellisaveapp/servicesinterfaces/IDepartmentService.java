package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.Department;

import java.util.List;
import java.util.Optional;

public interface IDepartmentService {
    public List<Department> list();
    public void insert(Department d);
    public void update(Department d);
    public Optional<Department> listById(Long id);
    public void delete(Long id);
}
