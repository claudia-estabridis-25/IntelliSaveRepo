package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.ClimateRecordDTO;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.ClimateRecord;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IClimateRecordService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/climate-records")
public class ClimateRecordController {
    private final IClimateRecordService cS;
    private final IBranchService bS;
    private final ModelMapper modelMapper;

    public ClimateRecordController(IClimateRecordService cS, IBranchService bS, ModelMapper modelMapper) {
        this.cS = cS;
        this.bS = bS;
        this.modelMapper = modelMapper;
    }


    //Listar todos
    @GetMapping
    public ResponseEntity<List<ClimateRecordDTO>> list() {
        List<ClimateRecordDTO> lista = cS.list()
                .stream()
                .map(climateRecord -> modelMapper.map(climateRecord, ClimateRecordDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    //Listar por id
    @GetMapping("/{id}")
    public ResponseEntity<ClimateRecordDTO> listById(@PathVariable Long id) {
        ClimateRecord c = cS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de clima con el id: " + id
                        )
                );

        ClimateRecordDTO dto = modelMapper.map(c, ClimateRecordDTO.class);

        return ResponseEntity.ok(dto);
    }

    //Listar registros de clima (o historial climático) de una sede específica
    //Osea, por cada sede, voy a obtener sus registros climáticos (gracias a la API)
    @GetMapping("/branch/{idBranch}")
    public ResponseEntity<List<ClimateRecordDTO>> listByBranch(@PathVariable Long idBranch) {

        //La lista implementada en IClimaRecordService es de tipo ClimateRecord, por lo que el objeto cS me trae
        //ese tipo de dato (ClimateRecord); pero acá necesito una lista de tipo DTO, por eso se usa modelMapper, para
        //poder convertir de tipo entidad (ClimateRecord) a tipo DTO (ClimateRecordDTO)

        List<ClimateRecordDTO> lista = cS.listByBranch(idBranch)
                .stream()
                .map(climateRecord -> modelMapper.map(climateRecord, ClimateRecordDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    /* En la API, este metodo hace:
        GET /api/climate-records/branch/3
       Osea, trae solo el historial climático de la Sede (Branch) con idBranch = 3
    */


    /*
    //Registrar
    @PostMapping
    public ResponseEntity<ClimateRecordDTO> register(@Valid @RequestBody ClimateRecordDTO dto) {
        Branch branch = bS.listById(dto.getIdBranch())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la sede con el id: " + dto.getIdBranch()
                        )
                );


        ClimateRecord climate = modelMapper.map(dto, ClimateRecord.class);
        climate.setBranch(branch);
        cS.insert(climate);
        ClimateRecordDTO responseDTO = modelMapper.map(climate, ClimateRecordDTO.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(climate.getIdClimate())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    //Actualizar
    @PutMapping
    public ResponseEntity<ClimateRecordDTO> update(@Valid @RequestBody ClimateRecordDTO dto) {
        Optional<ClimateRecord> existente = cS.listById(dto.getIdClimate());

        if(existente.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe un registro de clima con el id: " + dto.getIdClimate()
            );
        }

        Optional<Branch> branch = bS.listById(dto.getIdBranch());
        if(branch.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una sede con el id: " + dto.getIdBranch()
            );
        }

        ClimateRecord climate = existente.get();

        climate.setClimateDateTime(dto.getClimateDateTime());
        climate.setTemperature(dto.getTemperature());
        climate.setHumidity(dto.getHumidity());
        climate.setClimateCondition(dto.getClimateCondition());
        climate.setWindSpeed(dto.getWindSpeed());
        climate.setThermalSensation(dto.getThermalSensation());

        climate.setBranch(branch.get());

        cS.update(climate);

        ClimateRecordDTO responseDTO = modelMapper.map(climate, ClimateRecordDTO.class);

        return ResponseEntity.ok(responseDTO);
    }


    //Eliminar por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ClimateRecord c = cS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de clima con el id: " + id
                        )
                );

        cS.delete(c.getIdClimate());

        return ResponseEntity.noContent().build();
    }
    */


}
