package pe.edu.upc.intellisaveapp.dtos;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateDTO {
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener exactamente 9 dígitos")
    private String telephoneUser;

    @Size(max = 20, message = "El segundo nombre no puede superar 20 caracteres")
    private String secondName;

    @Size(min = 6, max = 60, message = "La nueva contraseña debe tener entre 6 y 60 caracteres")
    private String newPassword;

    public String getTelephoneUser() {
        return telephoneUser;
    }

    public void setTelephoneUser(String telephoneUser) {
        this.telephoneUser = telephoneUser;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
