package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ClimateRecordDTO {
    private Long idClimate;
    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime climateDateTime;
    @NotNull(message = "La temperatura es obligatoria")
    private double temperature;
    @NotNull(message = "La humedad es obligatoria")
    private double humidity;
    @NotBlank(message = "La condición climática es obligatoria")
    private String climateCondition;
    @NotNull(message = "La velocidad del viento es obligatoria")
    private double windSpeed;
    @NotNull(message = "La sensación térmica es obligatoria")
    private double thermalSensation;
    @NotNull(message = "El id de la sede relacionada es obligatorio")
    private Long idBranch;


    public Long getIdClimate() {
        return idClimate;
    }

    public void setIdClimate(Long idClimate) {
        this.idClimate = idClimate;
    }

    public LocalDateTime getClimateDateTime() {
        return climateDateTime;
    }

    public void setClimateDateTime(LocalDateTime climateDateTime) {
        this.climateDateTime = climateDateTime;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public String getClimateCondition() {
        return climateCondition;
    }

    public void setClimateCondition(String climateCondition) {
        this.climateCondition = climateCondition;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getThermalSensation() {
        return thermalSensation;
    }

    public void setThermalSensation(double thermalSensation) {
        this.thermalSensation = thermalSensation;
    }

    public Long getIdBranch() {
        return idBranch;
    }

    public void setIdBranch(Long idBranch) {
        this.idBranch = idBranch;
    }
}
