package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.dtos.BranchEmissionDTO;
import pe.edu.upc.intellisaveapp.dtos.CarbonFootprintComparisonDTO;
import pe.edu.upc.intellisaveapp.dtos.DepartmentEmissionDTO;
import pe.edu.upc.intellisaveapp.entities.CarbonFootprint;
import pe.edu.upc.intellisaveapp.entities.Department;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ICarbonFootprintService {
    public List<CarbonFootprint> list();
    public List<CarbonFootprint> listByDepartment(Long idDepartment);
    public List<CarbonFootprint> listByBranch(Long idBranch);
    public Optional<CarbonFootprint> listById(Long id);
    public void delete(Long id);
    public List<DepartmentEmissionDTO> emissionsByDepartment(Long idBranch);
    public List<BranchEmissionDTO> emissionsByBranch();
    public CarbonFootprint calculate(CarbonFootprint footprint, Department department, String timePeriod,
                                     LocalDate calculationDate, double emissionFactor);
    public List<CarbonFootprint> calculateAll(List<Department> departments, String timePeriod,
                                              LocalDate calculationDate, double emissionFactor);
    public LocalDate periodStart(String timePeriod, LocalDate calculationDate);
    public CarbonFootprintComparisonDTO compare(Long idDepartment, Long idBranch, String timePeriod,
                                                LocalDate dateA, LocalDate dateB);
    public Optional<CarbonFootprint> findCalculation(Long idDepartment, String timePeriod, LocalDate calculationDate);
    public byte[] generateCertificatePdf(CarbonFootprint footprint);
}