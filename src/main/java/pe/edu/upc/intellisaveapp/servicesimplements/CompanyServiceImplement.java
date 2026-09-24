package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.repositories.ICompanyRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICompanyService;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImplement implements ICompanyService {
    private final ICompanyRepository cR;

    public CompanyServiceImplement(ICompanyRepository cR) {
        this.cR = cR;
    }


    @Override
    public List<Company> list() {
        return cR.findAll();
    }

    @Override
    public void insert(Company c) {
        cR.save(c);
    }

    @Override
    public void update(Company c) {
        cR.save(c);
    }

    @Override
    public Optional<Company> listById(Long id) {
        return cR.findById(id);
    }

    @Override
    public void delete(Long id) {
        cR.deleteById(id);
    }
}
