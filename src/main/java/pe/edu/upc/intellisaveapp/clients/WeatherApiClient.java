package pe.edu.upc.intellisaveapp.clients;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.edu.upc.intellisaveapp.dtos.WeatherApiResponse;

import java.net.http.HttpClient;
import java.time.Duration;

// Cliente de la API meteorológica Open-Meteo (no requiere clave)
@Component
public class WeatherApiClient {
    private static final String URL = "https://api.open-meteo.com/v1/forecast"
            + "?latitude={latitude}&longitude={longitude}&current={fields}&timezone=auto";

    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,weather_code";

    private final RestClient restClient;

    public WeatherApiClient() {
        // Tiempos máximos de espera, para que una API caída no deje colgada la petición
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    // Consulta el clima actual en las coordenadas indicadas
    public WeatherApiResponse getCurrentWeather(double latitude, double longitude) {
        return restClient.get()
                .uri(URL, latitude, longitude, CURRENT_FIELDS)
                .retrieve()
                .body(WeatherApiResponse.class);
    }
}