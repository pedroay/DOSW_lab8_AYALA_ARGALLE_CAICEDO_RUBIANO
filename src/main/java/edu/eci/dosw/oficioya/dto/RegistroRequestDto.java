package edu.eci.dosw.oficioya.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroRequestDto {
    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String correo;

    @NotBlank
    private String telefono;

    @NotBlank
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres") 
    private String password;

    @NotBlank
    @Pattern(regexp = "^(TRABAJADOR|CONTRATANTE)$", message = "Rol inválido o no permitido") 
    private String rol;

    private String oficioPrincipal;
    private String zona;

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setPassword(String password) { this.password = password; }
    public void setRol(String rol) { this.rol = rol; }
    public void setOficioPrincipal(String oficioPrincipal) { this.oficioPrincipal = oficioPrincipal; }
    public void setZona(String zona) { this.zona = zona; }

    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
    public String getOficioPrincipal() { return oficioPrincipal; }
    public String getZona() { return zona; }


}
