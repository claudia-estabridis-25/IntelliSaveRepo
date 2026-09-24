package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.repositories.IDepartmentRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentServiceImplement implements IDepartmentService {
    private final IDepartmentRepository dR;

    public DepartmentServiceImplement(IDepartmentRepository dR) {
        this.dR = dR;
    }


    @Override
    public List<Department> list() {
        return dR.findAll();
    }

    @Override
    public void insert(Department d) {
        dR.save(d);
    }

    @Override
    public void update(Department d) {
        dR.save(d);
    }

    @Override
    public Optional<Department> listById(Long id) {
        return dR.findById(id);
    }

    @Override
    public void delete(Long id) {
        dR.deleteById(id);
    }
}
