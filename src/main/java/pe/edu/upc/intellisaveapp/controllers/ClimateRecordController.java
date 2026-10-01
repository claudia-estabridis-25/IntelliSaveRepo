package pe.edu.upc.intellisaveapp.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.ClimateRecordDTO;
import pe.edu.upc.intellisaveapp.dtos.ClimateRefreshResultDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.ClimateRecord;
import pe.edu.upc.intellisaveapp.entities.Department;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IClimateRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IDepartmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/climate-records")
public class ClimateRecordController {
    private final IClimateRecordService cS;
    private final IBranchService bS;
    private final IDepartmentService dS;
    private final ModelMapper modelMapper;

    public ClimateRecordController(IClimateRecordService cS, IBranchService bS,
                                   IDepartmentService dS, ModelMapper modelMapper) {
        this.cS = cS;
        this.bS = bS;
        this.dS = dS;
        this.modelMapper = modelMapper;
    }

    //Listar todos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ClimateRecordDTO>> list() {
        List<ClimateRecordDTO> lista = cS.list()
                .stream()
                .map(climateRecord -> modelMapper.map(climateRecord, ClimateRecordDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Listar por id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ClimateRecordDTO> listById(@PathVariable Long id) {
        ClimateRecord c = cS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe un registro de clima con el id: " + id)
                );

        return ResponseEntity.ok(modelMapper.map(c, ClimateRecordDTO.class));
    }

    //HU18: historial climático de una sede (se actualiza solo si el último registro tiene más de 1 hora)
    @GetMapping("/branch/{idBranch}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ClimateRecordDTO>> listByBranch(@PathVariable Long idBranch) {
        Branch branch = buscarSede(idBranch);
        cS.refreshIfOutdated(branch);

        return ResponseEntity.ok(convertir(cS.listByBranch(idBranch)));
    }

    //HU18: historial climático de un área (es el de la sede a la que pertenece el área)
    @GetMapping("/department/{idDepartment}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ClimateRecordDTO>> listByDepartment(@PathVariable Long idDepartment) {
        Department department = dS.listById(idDepartment)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe un área con el id: " + idDepartment)
                );

        Branch branch = department.getBranch();
        cS.refreshIfOutdated(branch);

        return ResponseEntity.ok(convertir(cS.listByBranch(branch.getIdBranch())));
    }

    //HU17: consultar la API y guardar el clima actual de una sede
    @PostMapping("/branch/{idBranch}/fetch")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ClimateRecordDTO> fetchBranch(@PathVariable Long idBranch) {
        Branch branch = buscarSede(idBranch);
        ClimateRecord record = cS.fetchAndSave(branch);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/climate-records/{id}")
                .buildAndExpand(record.getIdClimate())
                .toUri();

        return ResponseEntity.created(location).body(modelMapper.map(record, ClimateRecordDTO.class));
    }

    //HU17 (T11): consultar la API para todas las sedes; si una falla, continúa con las demás
    @PostMapping("/refresh")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ClimateRefreshResultDTO> refreshAll() {
        return ResponseEntity.ok(cS.refreshAllBranches());
    }

    // ===================== MÉTODOS DE APOYO =====================

    private Branch buscarSede(Long idBranch) {
        return bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe la sede con el id: " + idBranch)
                );
    }

    private List<ClimateRecordDTO> convertir(List<ClimateRecord> registros) {
        return registros.stream()
                .map(climateRecord -> modelMapper.map(climateRecord, ClimateRecordDTO.class))
                .toList();
    }
}