package edu.eci.dosw.oficioya.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SolicitudRequestDto {

    @NotNull(message = "El id del trabajador es obligatorio")
    private Long trabajadorId;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    public SolicitudRequestDto() {
    }

    public SolicitudRequestDto(Long trabajadorId, String descripcion) {
        this.trabajadorId = trabajadorId;
        this.descripcion = descripcion;
    }

    public Long getTrabajadorId() {
        return trabajadorId;
    }

    public void setTrabajadorId(Long trabajadorId) {
        this.trabajadorId = trabajadorId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

