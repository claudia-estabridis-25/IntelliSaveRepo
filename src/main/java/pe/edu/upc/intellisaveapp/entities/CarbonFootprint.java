package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "carbon_footprints")
public class CarbonFootprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFootprint; //PK

    @ManyToOne
    @JoinColumn(name = "idDepartment", nullable = false)
    private Department department; //FK

    @Column(name = "timePeriod", length = 12) //Puede ser nulo
    private String timePeriod; //Mensual, bimestral, trimestral, semestral, anual

    @Column(name = "calculationDate", nullable = false)
    private LocalDate calculationDate; //Fecha de cálculo de la huella de carbono

    //Emisiones CO₂ (kg) = Consumo (kWh) × Factor de emisión (kg CO₂/kWh)
    @Column(name = "emissionFactor", nullable = false) //Ejm: 0.52 kg CO₂/kWh, 0.6593 kg CO₂/kWh
    private double emissionFactor;

    @Column(name = "co2Emissions", nullable = false)
    private double co2Emissions; //en kilogramos

    @Column(name = "kwhTotalConsumption", nullable = false)
    private double kwhTotalConsumption; //en kWh

    public CarbonFootprint() {
    }

    public CarbonFootprint(Long idFootprint, Department department, String timePeriod, LocalDate calculationDate,
                           double emissionFactor, double co2Emissions, double kwhTotalConsumption) {
        this.idFootprint = idFootprint;
        this.department = department;
        this.timePeriod = timePeriod;
        this.calculationDate = calculationDate;
        this.emissionFactor = emissionFactor;
        this.co2Emissions = co2Emissions;
        this.kwhTotalConsumption = kwhTotalConsumption;
    }

    public Long getIdFootprint() {
        return idFootprint;
    }

    public void setIdFootprint(Long idFootprint) {
        this.idFootprint = idFootprint;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public double getEmissionFactor() {
        return emissionFactor;
    }

    public void setEmissionFactor(double emissionFactor) {
        this.emissionFactor = emissionFactor;
    }

    public double getCo2Emissions() {
        return co2Emissions;
    }

    public void setCo2Emissions(double co2Emissions) {
        this.co2Emissions = co2Emissions;
    }

    public double getKwhTotalConsumption() {
        return kwhTotalConsumption;
    }

    public void setKwhTotalConsumption(double kwhTotalConsumption) {
        this.kwhTotalConsumption = kwhTotalConsumption;
    }
}
