package pe.edu.upc.intellisaveapp.servicesinterfaces;

import pe.edu.upc.intellisaveapp.entities.Equipment;

import java.util.List;
import java.util.Optional;

public interface IEquipmentService {
    public List<Equipment> list();
    public void insert(Equipment e);
    public void update(Equipment e);
    public Optional<Equipment> listById(Long id);
    public void delete(Long id);
}