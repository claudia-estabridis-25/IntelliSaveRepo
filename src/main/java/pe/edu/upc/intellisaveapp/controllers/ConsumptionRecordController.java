package pe.edu.upc.intellisaveapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionRecordDTOInsert;
import pe.edu.upc.intellisaveapp.dtos.ConsumptionRecordDTOList;
import pe.edu.upc.intellisaveapp.entities.ConsumptionRecord;
import pe.edu.upc.intellisaveapp.entities.Equipment;
import pe.edu.upc.intellisaveapp.exceptions.ResourceNotFoundException;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IConsumptionRecordService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IEquipmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/consumption-records")
public class ConsumptionRecordController {
    private final IConsumptionRecordService crS;
    private final IEquipmentService eS;
    private final ModelMapper mP;

    private static final Double PRECIO_KWH_TEMPORAL = 0.75;

    public ConsumptionRecordController(IConsumptionRecordService crS, IEquipmentService eS, ModelMapper mP) {
        this.crS = crS;
        this.eS = eS;
        this.mP = mP;
    }

    @GetMapping
    public ResponseEntity<List<ConsumptionRecordDTOList>> listar() {
        List<ConsumptionRecordDTOList> lista = crS.list()
                .stream()
                .map(cr -> mP.map(cr, ConsumptionRecordDTOList.class))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<ConsumptionRecordDTOInsert> registrar(@Valid @RequestBody ConsumptionRecordDTOInsert dto) {
        Equipment equipment = eS.listById(dto.getIdEquipment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + dto.getIdEquipment()
                        )
                );

        ConsumptionRecord cr = mP.map(dto, ConsumptionRecord.class);
        cr.setEquipment(equipment);

        Double kwhConsumption = (equipment.getWattPowerEquipment() * dto.getHoursOfUse()) / 1000;
        cr.setKwhConsumption(kwhConsumption);
        cr.setCostTotal(kwhConsumption * PRECIO_KWH_TEMPORAL);

        crS.insert(cr);

        ConsumptionRecordDTOInsert responseDTO = mP.map(cr, ConsumptionRecordDTOInsert.class);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cr.getIdConsumptionRecord())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @PutMapping
    public ResponseEntity<ConsumptionRecordDTOInsert> actualizar(@Valid @RequestBody ConsumptionRecordDTOInsert dto) {
        ConsumptionRecord existente = crS.listById(dto.getIdConsumptionRecord())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de consumo con el id: " + dto.getIdConsumptionRecord()
                        )
                );

        Equipment equipment = eS.listById(dto.getIdEquipment())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un equipo con el id: " + dto.getIdEquipment()
                        )
                );

        ConsumptionRecord cr = mP.map(dto, ConsumptionRecord.class);
        cr.setIdConsumptionRecord(existente.getIdConsumptionRecord());
        cr.setEquipment(equipment);

        Double kwhConsumption = (equipment.getWattPowerEquipment() * dto.getHoursOfUse()) / 1000;
        cr.setKwhConsumption(kwhConsumption);
        cr.setCostTotal(kwhConsumption * PRECIO_KWH_TEMPORAL);

        crS.update(cr);

        ConsumptionRecordDTOInsert responseDTO = mP.map(cr, ConsumptionRecordDTOInsert.class);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsumptionRecordDTOList> listarPorId(@PathVariable Long id) {
        ConsumptionRecord cr = crS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de consumo con el id: " + id
                        )
                );

        ConsumptionRecordDTOList dto = mP.map(cr, ConsumptionRecordDTOList.class);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ConsumptionRecord cr = crS.listById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe un registro de consumo con el id: " + id
                        )
                );
        crS.delete(cr.getIdConsumptionRecord());
        return ResponseEntity.noContent().build();
    }
}