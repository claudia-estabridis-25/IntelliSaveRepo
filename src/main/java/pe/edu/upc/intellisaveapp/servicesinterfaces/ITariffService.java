package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.Tariff;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ITariffService {
    public List<Tariff> list();
    public List<Tariff> listByBranch(Long idBranch);
    public Optional<Tariff> findCurrentByBranch(Long idBranch, LocalDate fecha);
    public void insert(Tariff t);
    public void update(Tariff t);
    public Optional<Tariff> listById(Long id);
}