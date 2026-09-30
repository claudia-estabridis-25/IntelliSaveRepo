package pe.edu.upc.intellisaveapp.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UsersDTOInsert {
    private Long idUser;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 60, message = "El correo no puede superar 60 caracteres")
    private String emailUser;

    // Obligatoria al registrar; en actualizar es opcional (si viene vacía, no se cambia)
    @Size(min = 6, max = 60, message = "La contraseña debe tener entre 6 y 60 caracteres")
    private String passwordUser;
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    private String dniUser;

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(max = 20, message = "El primer nombre no puede superar 20 caracteres")
    private String firstName;

    @Size(max = 20, message = "El segundo nombre no puede superar 20 caracteres")
    private String secondName;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 25, message = "El apellido paterno no puede superar 25 caracteres")
    private String paternalSurname;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(max = 25, message = "El apellido materno no puede superar 25 caracteres")
    private String maternalSurname;

    @NotBlank(message = "El puesto es obligatorio")
    @Size(max = 25, message = "El puesto no puede superar 25 caracteres")
    private String positionUser;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener exactamente 9 dígitos")
    private String telephoneUser;

    @NotNull(message = "El id del área es obligatorio")
    private Long idDepartment;

    // Solo se usa en actualizar (HU026): ROLE_ADMIN, ROLE_SUPERVISOR, ROLE_EMPLOYEE
    private List<String> roles;

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getPasswordUser() {
        return passwordUser;
    }

    public void setPasswordUser(String passwordUser) {
        this.passwordUser = passwordUser;
    }

    public String getDniUser() {
        return dniUser;
    }

    public void setDniUser(String dniUser) {
        this.dniUser = dniUser;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getPaternalSurname() {
        return paternalSurname;
    }

    public void setPaternalSurname(String paternalSurname) {
        this.paternalSurname = paternalSurname;
    }

    public String getMaternalSurname() {
        return maternalSurname;
    }

    public void setMaternalSurname(String maternalSurname) {
        this.maternalSurname = maternalSurname;
    }

    public String getPositionUser() {
        return positionUser;
    }

    public void setPositionUser(String positionUser) {
        this.positionUser = positionUser;
    }

    public String getTelephoneUser() {
        return telephoneUser;
    }

    public void setTelephoneUser(String telephoneUser) {
        this.telephoneUser = telephoneUser;
    }

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
