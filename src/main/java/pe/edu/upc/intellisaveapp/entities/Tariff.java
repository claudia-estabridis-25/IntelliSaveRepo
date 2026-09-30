package pe.edu.upc.intellisaveapp.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "tariffs")
public class Tariff { //FALTA CRUD Y QUERIES
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTariff; //PK

    @ManyToOne
    @JoinColumn(name = "idBranch", nullable = false)
    private Branch branch; //FK

    @Column(name = "initialEffectiveDate", nullable = false)
    private LocalDate initialEffectiveDate;

    @Column(name = "endEffectiveDate", nullable = false)
    private LocalDate endEffectiveDate;

    @Column(name = "costPerKwh", nullable = false)
    private double costPerKwh; //Costo por kwh

    @Column(name = "supplier", length = 60)
    private String supplier; //proveedor

    public Tariff() {
    }

    public Tariff(Long idTariff, Branch branch, LocalDate initialEffectiveDate, LocalDate endEffectiveDate,
                  double costPerKwh, String supplier) {
        this.idTariff = idTariff;
        this.branch = branch;
        this.initialEffectiveDate = initialEffectiveDate;
        this.endEffectiveDate = endEffectiveDate;
        this.costPerKwh = costPerKwh;
        this.supplier = supplier;
    }

    public Long getIdTariff() {
        return idTariff;
    }

    public void setIdTariff(Long idTariff) {
        this.idTariff = idTariff;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public LocalDate getInitialEffectiveDate() {
        return initialEffectiveDate;
    }

    public void setInitialEffectiveDate(LocalDate initialEffectiveDate) {
        this.initialEffectiveDate = initialEffectiveDate;
    }

    public LocalDate getEndEffectiveDate() {
        return endEffectiveDate;
    }

    public void setEndEffectiveDate(LocalDate endEffectiveDate) {
        this.endEffectiveDate = endEffectiveDate;
    }

    public double getCostPerKwh() {
        return costPerKwh;
    }

    public void setCostPerKwh(double costPerKwh) {
        this.costPerKwh = costPerKwh;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
}
