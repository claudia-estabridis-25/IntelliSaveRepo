package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.TariffDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.TariffDTOList;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.Tariff;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.ITariffService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
public class TariffController {
    private final ITariffService tS;
    private final IBranchService bS;

    public TariffController(ITariffService tS, IBranchService bS) {
        this.tS = tS;
        this.bS = bS;
    }

    // HU039: listar todas las tarifas
    @GetMapping
    public ResponseEntity<List<TariffDTOList>> listar() {
        List<TariffDTOList> lista = tS.list()
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU039: historial de tarifas de una sede
    @GetMapping("/branch/{idBranch}")
    public ResponseEntity<List<TariffDTOList>> listarPorSede(@PathVariable Long idBranch) {
        buscarSede(idBranch);

        List<TariffDTOList> lista = tS.listByBranch(idBranch)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(lista);
    }

    // HU044: tarifa vigente de una sede (hoy, o en la fecha indicada)
    @GetMapping("/branch/{idBranch}/current")
    public ResponseEntity<TariffDTOList> tarifaVigente(
            @PathVariable Long idBranch,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        buscarSede(idBranch);
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();

        Tariff tariff = tS.findCurrentByBranch(idBranch, dia)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "La sede con id " + idBranch + " no tiene una tarifa vigente en la fecha " + dia
                        )
                );

        return ResponseEntity.ok(toDTO(tariff));
    }

    // HU054: detalle de una tarifa
    @GetMapping("/{id}")
    public ResponseEntity<TariffDTOList> listarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(buscarTarifa(id)));
    }

    // HU15: registrar tarifa
    @PostMapping
    public ResponseEntity<TariffDTOList> registrar(@Valid @RequestBody TariffDTOInsert dto) {
        Branch branch = buscarSede(dto.getIdBranch());
        validarTarifa(dto, null);

        Tariff tariff = new Tariff();
        copiarDatos(dto, tariff, branch);
        tS.insert(tariff);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tariff.getIdTariff())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(tariff));
    }

    // HU040: actualizar tarifa
    @PutMapping
    public ResponseEntity<TariffDTOList> actualizar(@Valid @RequestBody TariffDTOInsert dto) {
        if (dto.getIdTariff() == null) {
            throw new BusinessRuleException("El id de la tarifa es obligatorio para actualizar");
        }

        Tariff tariff = buscarTarifa(dto.getIdTariff());
        Branch branch = buscarSede(dto.getIdBranch());
        validarTarifa(dto, dto.getIdTariff());

        copiarDatos(dto, tariff, branch);
        tS.update(tariff);

        return ResponseEntity.ok(toDTO(tariff));
    }

    // No hay DELETE: las tarifas forman parte del historial de costos (regla de negocio)

    // ===================== MÉTODOS DE APOYO =====================

    private Branch buscarSede(Long idBranch) {
        return bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una sede con el id: " + idBranch)
                );
    }

    private Tariff buscarTarifa(Long id) {
        return tS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe una tarifa con el id: " + id)
                );
    }

    // Reglas: fechas en orden y sin cruzarse con otra tarifa de la misma sede
    private void validarTarifa(TariffDTOInsert dto, Long idTariffActual) {
        if (dto.getInitialEffectiveDate().isAfter(dto.getEndEffectiveDate())) {
            throw new BusinessRuleException(
                    "La fecha de inicio de vigencia no puede ser posterior a la fecha de fin"
            );
        }

        boolean seCruza = tS.listByBranch(dto.getIdBranch())
                .stream()
                .filter(t -> idTariffActual == null || !t.getIdTariff().equals(idTariffActual))
                .anyMatch(t -> !t.getInitialEffectiveDate().isAfter(dto.getEndEffectiveDate())
                        && !t.getEndEffectiveDate().isBefore(dto.getInitialEffectiveDate()));

        if (seCruza) {
            throw new BusinessRuleException(
                    "Las fechas de vigencia se cruzan con otra tarifa de la misma sede"
            );
        }
    }

    private void copiarDatos(TariffDTOInsert dto, Tariff tariff, Branch branch) {
        tariff.setBranch(branch);
        tariff.setInitialEffectiveDate(dto.getInitialEffectiveDate());
        tariff.setEndEffectiveDate(dto.getEndEffectiveDate());
        tariff.setCostPerKwh(dto.getCostPerKwh());
        tariff.setSupplier(dto.getSupplier());
    }

    private TariffDTOList toDTO(Tariff tariff) {
        TariffDTOList dto = new TariffDTOList();
        dto.setIdTariff(tariff.getIdTariff());
        dto.setIdBranch(tariff.getBranch().getIdBranch());
        dto.setNameBranch(tariff.getBranch().getNameBranch());
        dto.setInitialEffectiveDate(tariff.getInitialEffectiveDate());
        dto.setEndEffectiveDate(tariff.getEndEffectiveDate());
        dto.setCostPerKwh(tariff.getCostPerKwh());
        dto.setSupplier(tariff.getSupplier());
        return dto;
    }
}