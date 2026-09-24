package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "climate_records")
public class ClimateRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@Column(name = "climate_id")
    private Long idClimate; //PK

    @Column(name = "climate_date_time", nullable = false)
    private LocalDateTime climateDateTime;

    @Column(name = "temperature", precision = 10, scale = 2, nullable = false)
    private double temperature;

    @Column(name = "humidity", precision = 10, scale = 2, nullable = false)
    private double humidity;

    @Column(name = "climate_condition", length = 100, nullable = false)
    private String climateCondition;

    @Column(name = "wind_speed", precision = 10, scale = 2, nullable = false)
    private double windSpeed;

    @Column(name = "thermal_sensation", precision = 10, scale = 2, nullable = false)
    private double thermalSensation;

    @ManyToOne
    @JoinColumn(name = "idBranch", nullable = false)
    private Branch branch; //FK

    public ClimateRecord() {
    }

    public ClimateRecord(Long idClimate, LocalDateTime climateDateTime, double temperature, double humidity,
                         String climateCondition, double windSpeed, double thermalSensation, Branch branch) {
        this.idClimate = idClimate;
        this.climateDateTime = climateDateTime;
        this.temperature = temperature;
        this.humidity = humidity;
        this.climateCondition = climateCondition;
        this.windSpeed = windSpeed;
        this.thermalSensation = thermalSensation;
        this.branch = branch;
    }

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

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}
