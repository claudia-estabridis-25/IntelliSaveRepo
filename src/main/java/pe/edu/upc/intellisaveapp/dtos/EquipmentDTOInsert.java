package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class EquipmentDTOInsert {
    private Long idEquipment;
    @NotNull(message = "El id del área relacionada es obligatorio")
    private Long idDepartment;
    @NotBlank(message = "El nombre del equipo es obligatorio")
    private String nameEquipment;
    @NotBlank(message = "La categoría del equipo es obligatoria")
    private String categoryEquipment;
    @NotBlank(message = "La marca del equipo es obligatoria")
    private String brandEquipment;
    @NotBlank(message = "El modelo del equipo es obligatorio")
    private String modelEquipment;
    @NotNull(message = "La potencia en watts del equipo es obligatoria")
    private Double wattPowerEquipment;
    @NotNull(message = "La fecha de adquisición del equipo es obligatoria")
    private LocalDate acquisitionDateEquipment;
    @NotBlank(message = "El estado del equipo es obligatorio")
    private String statusEquipment;

    public Long getIdEquipment() {
        return idEquipment;
    }

    public void setIdEquipment(Long idEquipment) {
        this.idEquipment = idEquipment;
    }

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public String getNameEquipment() {
        return nameEquipment;
    }

    public void setNameEquipment(String nameEquipment) {
        this.nameEquipment = nameEquipment;
    }

    public String getCategoryEquipment() {
        return categoryEquipment;
    }

    public void setCategoryEquipment(String categoryEquipment) {
        this.categoryEquipment = categoryEquipment;
    }

    public String getBrandEquipment() {
        return brandEquipment;
    }

    public void setBrandEquipment(String brandEquipment) {
        this.brandEquipment = brandEquipment;
    }

    public String getModelEquipment() {
        return modelEquipment;
    }

    public void setModelEquipment(String modelEquipment) {
        this.modelEquipment = modelEquipment;
    }

    public Double getWattPowerEquipment() {
        return wattPowerEquipment;
    }

    public void setWattPowerEquipment(Double wattPowerEquipment) {
        this.wattPowerEquipment = wattPowerEquipment;
    }

    public LocalDate getAcquisitionDateEquipment() {
        return acquisitionDateEquipment;
    }

    public void setAcquisitionDateEquipment(LocalDate acquisitionDateEquipment) {
        this.acquisitionDateEquipment = acquisitionDateEquipment;
    }

    public String getStatusEquipment() {
        return statusEquipment;
    }

    public void setStatusEquipment(String statusEquipment) {
        this.statusEquipment = statusEquipment;
    }
}