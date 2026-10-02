package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDate;

public class CarbonFootprintComparisonDTO {
    private String scope;
    private String timePeriod;
    private LocalDate dateA;
    private Double kwhA;
    private Double co2A;
    private LocalDate dateB;
    private Double kwhB;
    private Double co2B;
    private Double variationCo2;
    private Double variationPercent;
    private String trend;

    public CarbonFootprintComparisonDTO() {
    }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getTimePeriod() { return timePeriod; }
    public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }

    public LocalDate getDateA() { return dateA; }
    public void setDateA(LocalDate dateA) { this.dateA = dateA; }

    public Double getKwhA() { return kwhA; }
    public void setKwhA(Double kwhA) { this.kwhA = kwhA; }

    public Double getCo2A() { return co2A; }
    public void setCo2A(Double co2A) { this.co2A = co2A; }

    public LocalDate getDateB() { return dateB; }
    public void setDateB(LocalDate dateB) { this.dateB = dateB; }

    public Double getKwhB() { return kwhB; }
    public void setKwhB(Double kwhB) { this.kwhB = kwhB; }

    public Double getCo2B() { return co2B; }
    public void setCo2B(Double co2B) { this.co2B = co2B; }

    public Double getVariationCo2() { return variationCo2; }
    public void setVariationCo2(Double variationCo2) { this.variationCo2 = variationCo2; }

    public Double getVariationPercent() { return variationPercent; }
    public void setVariationPercent(Double variationPercent) { this.variationPercent = variationPercent; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
}