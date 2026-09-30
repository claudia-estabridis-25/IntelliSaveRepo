package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "equipments")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipment; //PK

    @ManyToOne
    @JoinColumn(name = "idDepartment", nullable = false)
    private Department department; //FK

    @Column(name = "nameEquipment", length = 40, nullable = false)
    private String nameEquipment;

    @Column(name = "categoryEquipment", length = 40, nullable = false)
    private String categoryEquipment;

    @Column(name = "brandEquipment", length = 30, nullable = false)
    private String brandEquipment;

    @Column(name = "modelEquipment", length = 30, nullable = false)
    private String modelEquipment;

    @Column(name = "wattPowerEquipment", nullable = false)
    private Double wattPowerEquipment;

    @Column(name = "acquisitionDateEquipment", nullable = false)
    private LocalDate acquisitionDateEquipment;

    @Column(name = "statusEquipment", length = 20, nullable = false)
    private String statusEquipment;

    public Equipment() {
    }

    public Equipment(Long idEquipment, Department department, String nameEquipment, String categoryEquipment,
                     String brandEquipment, String modelEquipment, Double wattPowerEquipment,
                     LocalDate acquisitionDateEquipment, String statusEquipment) {
        this.idEquipment = idEquipment;
        this.department = department;
        this.nameEquipment = nameEquipment;
        this.categoryEquipment = categoryEquipment;
        this.brandEquipment = brandEquipment;
        this.modelEquipment = modelEquipment;
        this.wattPowerEquipment = wattPowerEquipment;
        this.acquisitionDateEquipment = acquisitionDateEquipment;
        this.statusEquipment = statusEquipment;
    }

    public Long getIdEquipment() {
        return idEquipment;
    }

    public void setIdEquipment(Long idEquipment) {
        this.idEquipment = idEquipment;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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