package pe.edu.upc.intellisaveapp.dtos;

import java.time.LocalDate;

public class DailyConsumptionPointDTO {
    private LocalDate date;
    private Double kwh;

    public DailyConsumptionPointDTO() {
    }

    public DailyConsumptionPointDTO(LocalDate date, Double kwh) {
        this.date = date;
        this.kwh = kwh;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getKwh() {
        return kwh;
    }

    public void setKwh(Double kwh) {
        this.kwh = kwh;
    }
}