package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import java.time.LocalDateTime;

public record SolicitudResponseDto(
        Long id,
        String descripcion,
        EstadoSolicitud estado,
        Long contratanteId,
        Long trabajadorId,
        LocalDateTime fechaCreacion) {
}

