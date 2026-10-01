package pe.edu.upc.intellisaveapp.servicesimplements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import pe.edu.upc.intellisaveapp.clients.WeatherApiClient;
import pe.edu.upc.intellisaveapp.dtos.ClimateRefreshResultDTO;
import pe.edu.upc.intellisaveapp.dtos.WeatherApiResponse;
import pe.edu.upc.intellisaveapp.entities.Branch;
import pe.edu.upc.intellisaveapp.entities.ClimateRecord;
import pe.edu.upc.intellisaveapp.exceptions.BusinessRuleException;
import pe.edu.upc.intellisaveapp.repositories.IClimateRecordRepository;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IBranchService;
import pe.edu.upc.intellisaveapp.servicesinterfaces.IClimateRecordService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ClimateRecordServiceImplement implements IClimateRecordService {
    private static final Logger log = LoggerFactory.getLogger(ClimateRecordServiceImplement.class);

    // HU17 CA01: no se consulta la API más de una vez por hora para la misma sede
    private static final long MINUTOS_ENTRE_CONSULTAS = 60;

    private final IClimateRecordRepository cR;
    private final IBranchService bS;
    private final WeatherApiClient weatherApiClient;

    public ClimateRecordServiceImplement(IClimateRecordRepository cR, IBranchService bS,
                                         WeatherApiClient weatherApiClient) {
        this.cR = cR;
        this.bS = bS;
        this.weatherApiClient = weatherApiClient;
    }

    @Override
    public List<ClimateRecord> list() {
        return cR.findAll();
    }

    @Override
    public Optional<ClimateRecord> listById(Long id) {
        return cR.findById(id);
    }

    @Override
    public List<ClimateRecord> listByBranch(Long idBranch) {
        return cR.findByBranch_IdBranch(idBranch)
                .stream()
                .sorted(Comparator.comparing(ClimateRecord::getClimateDateTime).reversed())
                .toList();
    }

    // HU17: consulta la API con las coordenadas de la sede (CA02) y guarda el registro (CA03)
    @Override
    public ClimateRecord fetchAndSave(Branch branch) {
        WeatherApiResponse response;
        try {
            response = weatherApiClient.getCurrentWeather(branch.getLatitudeBranch(), branch.getLongitudeBranch());
        } catch (RestClientException e) {
            // CA04: si la API no responde, se registra el error
            log.error("No se pudo consultar el clima de la sede {}: {}", branch.getNameBranch(), e.getMessage());
            throw new BusinessRuleException(
                    "No se pudo consultar el servicio meteorológico para la sede " + branch.getNameBranch()
            );
        }

        if (response == null || response.getCurrent() == null) {
            throw new BusinessRuleException(
                    "El servicio meteorológico no devolvió datos para la sede " + branch.getNameBranch()
            );
        }

        WeatherApiResponse.Current actual = response.getCurrent();

        ClimateRecord record = new ClimateRecord();
        record.setBranch(branch);
        record.setClimateDateTime(LocalDateTime.now());
        record.setTemperature(valor(actual.getTemperature()));
        record.setHumidity(valor(actual.getHumidity()));
        record.setWindSpeed(valor(actual.getWindSpeed()));
        record.setThermalSensation(valor(actual.getThermalSensation()));
        record.setClimateCondition(translateWeatherCode(actual.getWeatherCode()));

        return cR.save(record);
    }

    // HU17 CA01 y CA05: se actualiza solo, sin que el usuario lo pida, cuando el último registro tiene más de una hora
    @Override
    public void refreshIfOutdated(Branch branch) {
        Optional<ClimateRecord> ultimo = cR.findFirstByBranch_IdBranchOrderByClimateDateTimeDesc(branch.getIdBranch());

        boolean desactualizado = ultimo.isEmpty()
                || ultimo.get().getClimateDateTime().isBefore(LocalDateTime.now().minusMinutes(MINUTOS_ENTRE_CONSULTAS));

        if (desactualizado) {
            try {
                fetchAndSave(branch);
            } catch (BusinessRuleException e) {
                // Si la API falla, se sigue mostrando el historial que ya existe
                log.warn("No se pudo actualizar el clima de la sede {}", branch.getNameBranch());
            }
        }
    }

    // T06 y T10: recorre todas las sedes; si una falla, continúa con las demás
    @Override
    public ClimateRefreshResultDTO refreshAllBranches() {
        List<Branch> sedes = bS.list();
        int guardados = 0;
        List<String> errores = new ArrayList<>();

        for (Branch sede : sedes) {
            try {
                fetchAndSave(sede);
                guardados++;
            } catch (RuntimeException e) {
                errores.add(sede.getNameBranch() + ": " + e.getMessage());
            }
        }

        return new ClimateRefreshResultDTO(sedes.size(), guardados, errores);
    }

    // T07: traduce el weather_code numérico (WMO) de la API a texto
    private String translateWeatherCode(Integer code) {
        if (code == null) {
            return "Sin información";
        }
        return switch (code) {
            case 0 -> "Despejado";
            case 1 -> "Mayormente despejado";
            case 2 -> "Parcialmente nublado";
            case 3 -> "Nublado";
            case 45, 48 -> "Niebla";
            case 51, 53, 55 -> "Llovizna";
            case 56, 57 -> "Llovizna helada";
            case 61, 63, 65 -> "Lluvia";
            case 66, 67 -> "Lluvia helada";
            case 71, 73, 75, 77 -> "Nieve";
            case 80, 81, 82 -> "Chubascos";
            case 85, 86 -> "Chubascos de nieve";
            case 95 -> "Tormenta";
            case 96, 99 -> "Tormenta con granizo";
            default -> "Condición desconocida (código " + code + ")";
        };
    }

    private double valor(Double numero) {
        return numero != null ? numero : 0.0;
    }
}