package pe.edu.upc.intellisaveapp.dtos;

import java.util.List;

public class PredictionResultDTO {
    private PredictionDTOList prediction;
    private Integer historicalDaysWithData;
    private List<DailyConsumptionPointDTO> historical;
    private List<DailyConsumptionPointDTO> estimated;

    public PredictionDTOList getPrediction() {
        return prediction;
    }

    public void setPrediction(PredictionDTOList prediction) {
        this.prediction = prediction;
    }

    public Integer getHistoricalDaysWithData() {
        return historicalDaysWithData;
    }

    public void setHistoricalDaysWithData(Integer historicalDaysWithData) {
        this.historicalDaysWithData = historicalDaysWithData;
    }

    public List<DailyConsumptionPointDTO> getHistorical() {
        return historical;
    }

    public void setHistorical(List<DailyConsumptionPointDTO> historical) {
        this.historical = historical;
    }

    public List<DailyConsumptionPointDTO> getEstimated() {
        return estimated;
    }

    public void setEstimated(List<DailyConsumptionPointDTO> estimated) {
        this.estimated = estimated;
    }
}