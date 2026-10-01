package pe.edu.upc.intellisaveapp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Mapea la respuesta de Open-Meteo (solo los campos de "current" que se usan)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {
    private Current current;

    public Current getCurrent() { return current; }
    public void setCurrent(Current current) { this.current = current; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        @JsonProperty("temperature_2m")
        private Double temperature;

        @JsonProperty("relative_humidity_2m")
        private Double humidity;

        @JsonProperty("apparent_temperature")
        private Double thermalSensation;

        @JsonProperty("wind_speed_10m")
        private Double windSpeed;

        @JsonProperty("weather_code")
        private Integer weatherCode; //climateCondition

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Double getHumidity() { return humidity; }
        public void setHumidity(Double humidity) { this.humidity = humidity; }

        public Double getThermalSensation() { return thermalSensation; }
        public void setThermalSensation(Double thermalSensation) { this.thermalSensation = thermalSensation; }

        public Double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

        public Integer getWeatherCode() { return weatherCode; }
        public void setWeatherCode(Integer weatherCode) { this.weatherCode = weatherCode; }
    }
}