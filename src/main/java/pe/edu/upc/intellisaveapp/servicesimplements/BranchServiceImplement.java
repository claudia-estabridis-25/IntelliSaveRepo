package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.repositories.IBranchRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;

import java.util.List;
import java.util.Optional;

@Service
public class BranchServiceImplement implements IBranchService {
    private final IBranchRepository bR;

    public BranchServiceImplement(IBranchRepository bR) {
        this.bR = bR;
    }

    @Override
    public List<Branch> list() {
        return bR.findAll();
    }

    @Override
    public void insert(Branch b) {
        bR.save(b);
    }

    @Override
    public void update(Branch b) {
        bR.save(b);
    }

    @Override
    public Optional<Branch> listById(Long id) {
        return bR.findById(id);
    }

    @Override
    public void delete(Long id) {
        bR.deleteById(id);
    }
}
