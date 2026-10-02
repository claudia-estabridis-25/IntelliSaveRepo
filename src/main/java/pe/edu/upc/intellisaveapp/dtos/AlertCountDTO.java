package pe.edu.upc.intellisaveapp.dtos;

//Cantidad de alertas por estado o por nivel de prioridad
public class AlertCountDTO {
    private String category;
    private Long totalAlerts;

    public AlertCountDTO() {
    }

    public AlertCountDTO(String category, Long totalAlerts) {
        this.category = category;
        this.totalAlerts = totalAlerts;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(Long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }
}