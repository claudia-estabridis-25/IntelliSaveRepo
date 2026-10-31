package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Company;
import pe.edu.upc.intellisaveapp.repositories.ICompanyRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ICompanyService;

import java.util.List;
import java.util.Optional;

// @Service -> Pertenece a Spring Core o Inversión de Control (IoC)

// @Service es una anotación de Spring que indica que esta clase es un servicio, osea que contiene la lógica de
// negocio de la aplicación. Los servicios son clases que contienen métodos que realizan operaciones específicas,
// como acceder a la base de datos, procesar datos, etc. Al marcar esta clase con @Service, Spring la detecta
// automáticamente y la registra como un bean en el contenedor de Spring, lo que permite que pueda ser inyectada en
// otras clases mediante inyección de dependencias.
// En resumen: @Service hace que Spring marque la clase como servicio de lógica de negocio y la convierte en Bean.

@Service
public class CompanyServiceImplement implements ICompanyService {
    private final ICompanyRepository cR;

    public CompanyServiceImplement(ICompanyRepository cR) {
        this.cR = cR;
    }

    // @Override indica que este metodo está sobrescribiendo un metodo de la interfaz ICompanyService.
    // Esto es útil para asegurarse de que el metodo realmente existe en la interfaz y que se está implementando
    // correctamente. Si el metodo no coincide con la firma del metodo en la interfaz, el compilador generará un error.
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

    @Override
    public List<Object[]> countCompaniesBySector() {
        return cR.countCompaniesBySector();
    }

    @Override
    public List<Company> findByRucCompany(String rucCompany) {
        return cR.findByRucCompany(rucCompany);
    }

    @Override
    public List<Object[]> structureByCompany() {
        return cR.structureByCompany();
    }
}
