package edu.eci.dosw.oficioya.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsarioResponseDTO{
    private Long id; 
    private String nombre;
    private String correo;
    private String telefono;
    private String rol;
    private String oficioPrincipal;
    private String zona;

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setRol(String rol) { this.rol = rol; }
    public void setOficioPrincipal(String oficioPrincipal) { this.oficioPrincipal = oficioPrincipal; }
    public void setZona(String zona) { this.zona = zona; }
}