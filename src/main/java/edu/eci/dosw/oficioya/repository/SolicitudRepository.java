package edu.eci.dosw.oficioya.repository;

import edu.eci.dosw.oficioya.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByContratanteId(Long contratanteId);

    List<Solicitud> findByTrabajadorId(Long trabajadorId);

    Optional<Solicitud> findByIdAndContratanteId(Long id, Long contratanteId);

    Optional<Solicitud> findByIdAndTrabajadorId(Long id, Long trabajadorId);

    // Para GET /solicitudes/{id}: cualquiera de las dos partes
    @Query("select s from Solicitud s where s.id = :id "
            + "and (s.contratante.id = :usuarioId or s.trabajador.id = :usuarioId)")
    Optional<Solicitud> findByIdAndParticipante(@Param("id") Long id,
            @Param("usuarioId") Long usuarioId);
}