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
import pe.edu.upc.intellisaveapp.securities.CompanyAccess;
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
    private final CompanyAccess acceso;

    public ClimateRecordController(IClimateRecordService cS, IBranchService bS,
                                   IDepartmentService dS, ModelMapper modelMapper, CompanyAccess acceso) {
        this.cS = cS;
        this.bS = bS;
        this.dS = dS;
        this.modelMapper = modelMapper;
        this.acceso = acceso;
    }

    //Listar todos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ClimateRecordDTO>> list() {
        Long idCompany = acceso.idEmpresaUsuario();

        List<ClimateRecordDTO> lista = cS.list()
                .stream()
                .filter(climateRecord -> idCompany == null ||
                        climateRecord.getBranch().getCompany().getIdCompany().equals(idCompany))
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
        acceso.validarEmpresa(c.getBranch().getCompany().getIdCompany());

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
        acceso.validarEmpresa(department.getBranch().getCompany().getIdCompany());

        Branch branch = department.getBranch();
        cS.refreshIfOutdated(branch);

        return ResponseEntity.ok(convertir(cS.listByBranch(branch.getIdBranch())));
    }

    //HU17: consultar la API y guardar el clima actual de una sede
    @PostMapping("/branch/{idBranch}/fetch")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ClimateRecordDTO> fetchBranch(@PathVariable Long idBranch) {
        // Validando si existe la sede y si pertenece a la empresa del usuario logueado
        Branch branch = buscarSede(idBranch);

        // Pide a la API Open-Meteo el clima actual con la latitud y longitud de la sede,
        // y lo guarda en el objeto record (de ClimateRecord).
        ClimateRecord record = cS.fetchAndSave(branch);

        // Armando la URL del registro nuevo, por ejemplo: http://localhost:8080/api/climate-records/15
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath() //Toma la base del servidor (http://localhost:8080)
                .path("/api/climate-records/{id}") //Le agrega la ruta
                .buildAndExpand(record.getIdClimate()) //Cambia {id} por el id real
                .toUri(); //Lo convierte en URI

        // Responde 201 Created. Pone la URL en el header Location y en el cuerpo
        // devuelve el registro como ClimateRecordDTO.
        return ResponseEntity.created(location).body(modelMapper.map(record, ClimateRecordDTO.class));
    }

    //HU17 (T11): consultar la API para todas las sedes; si ocurre una falla, continúa con las demás.
    //Este endpoint actualiza el clima de todas las sedes del sistema, de forma automática cada cada hora.
    //Se deja esta opción manual únicamente para el admin de la plataforma.
    @PostMapping("/refresh")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ClimateRefreshResultDTO> refreshAll() {
        acceso.validarAdminDePlataforma();

        return ResponseEntity.ok(cS.refreshAllBranches());
    }

    // ================================ MÉTODOS DE APOYO ===================================

    private Branch buscarSede(Long idBranch) {
        Branch branch = bS.listById(idBranch)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No existe la sede con el id: " + idBranch)
                );
        acceso.validarEmpresa(branch.getCompany().getIdCompany());

        return branch;
    }

    private List<ClimateRecordDTO> convertir(List<ClimateRecord> registros) {
        return registros.stream()
                .map(climateRecord -> modelMapper.map(climateRecord, ClimateRecordDTO.class))
                .toList();
    }
}