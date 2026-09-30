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


    @GetMapping("/branch/{idBranch}")
    public ResponseEntity<List<ClimateRecordDTO>> listByBranch(@PathVariable Long idBranch) {


        List<ClimateRecordDTO> lista = cS.listByBranch(idBranch)
                .stream()
                .map(climateRecord -> modelMapper.map(climateRecord, ClimateRecordDTO.class))
                .toList();

        return ResponseEntity.ok(lista);
    }


}
