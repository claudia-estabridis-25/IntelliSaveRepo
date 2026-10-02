package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AlertDTOInsert {
    private Long idAlert;
    // El área se toma automáticamente del equipo
    @NotNull(message = "El id del equipo es obligatorio")
    private Long idEquipment;
    private LocalDateTime dateTimeAlert; // Opcional: vacío = fecha y hora actual
    @NotBlank(message = "El tipo de alerta es obligatorio")
    @Size(max = 20, message = "El tipo de alerta no puede superar 20 caracteres")
    private String typeAlert;
    @NotBlank(message = "La descripción de la alerta es obligatoria")
    @Size(max = 90, message = "La descripción no puede superar 90 caracteres")
    private String descriptionAlert;
    @NotBlank(message = "El nivel de prioridad es obligatorio")
    private String priorityLevelAlert; // Alta, Media, Baja
    @NotNull(message = "El consumo detectado (kWh) es obligatorio")
    @PositiveOrZero(message = "El consumo detectado no puede ser negativo")
    private Double kwhDetected;
    private String statusAlert; // Opcional: vacío = Pendiente

    public Long getIdAlert() {
        return idAlert;
    }

    public void setIdAlert(Long idAlert) {
        this.idAlert = idAlert;
    }

    public Long getIdEquipment() {
        return idEquipment;
    }

    public void setIdEquipment(Long idEquipment) {
        this.idEquipment = idEquipment;
    }

    public LocalDateTime getDateTimeAlert() {
        return dateTimeAlert;
    }

    public void setDateTimeAlert(LocalDateTime dateTimeAlert) {
        this.dateTimeAlert = dateTimeAlert;
    }

    public String getTypeAlert() {
        return typeAlert;
    }

    public void setTypeAlert(String typeAlert) {
        this.typeAlert = typeAlert;
    }

    public String getDescriptionAlert() {
        return descriptionAlert;
    }

    public void setDescriptionAlert(String descriptionAlert) {
        this.descriptionAlert = descriptionAlert;
    }

    public String getPriorityLevelAlert() {
        return priorityLevelAlert;
    }

    public void setPriorityLevelAlert(String priorityLevelAlert) {
        this.priorityLevelAlert = priorityLevelAlert;
    }

    public Double getKwhDetected() {
        return kwhDetected;
    }

    public void setKwhDetected(Double kwhDetected) {
        this.kwhDetected = kwhDetected;
    }

    public String getStatusAlert() {
        return statusAlert;
    }

    public void setStatusAlert(String statusAlert) {
        this.statusAlert = statusAlert;
    }
}