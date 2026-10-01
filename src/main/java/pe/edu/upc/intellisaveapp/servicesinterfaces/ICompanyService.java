package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.Company;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface ICompanyService {
    public List<Company> list();
    public void insert(Company c);
    public void update(Company c);
    public Optional<Company> listById(Long id);
    public void delete(Long id);
    List<Object[]> countCompaniesBySector(); //cantidad de empresas por sector
    List<Company> findByRucCompany(String rucCompany); //buscar empresa por ruc
    List<Object[]> structureByCompany(); //sede y áreas por empresa
}