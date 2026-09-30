package pe.edu.upc.intellisaveapp.servicesimplements;

import org.springframework.stereotype.Service;
import pe.edu.upc.intellisaveapp.entities.Tariff;
import pe.edu.upc.intellisaveapp.repositories.ITariffRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ITariffService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TariffServiceImplement implements ITariffService {
    private final ITariffRepository tR;

    public TariffServiceImplement(ITariffRepository tR) {
        this.tR = tR;
    }

    @Override
    public List<Tariff> list() {
        return tR.findAll();
    }

    @Override
    public List<Tariff> listByBranch(Long idBranch) {
        return tR.findByBranch_IdBranch(idBranch);
    }

    @Override
    public Optional<Tariff> findCurrentByBranch(Long idBranch, LocalDate fecha) {
        return tR.findByBranch_IdBranchAndInitialEffectiveDateLessThanEqualAndEndEffectiveDateGreaterThanEqual(
                        idBranch, fecha, fecha)
                .stream()
                .findFirst();
    }

    @Override
    public void insert(Tariff t) {
        tR.save(t);
    }

    @Override
    public void update(Tariff t) {
        tR.save(t);
    }

    @Override
    public Optional<Tariff> listById(Long id) {
        return tR.findById(id);
    }
}