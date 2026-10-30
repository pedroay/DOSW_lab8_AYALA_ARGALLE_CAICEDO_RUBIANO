package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.SolicitudRequestDto;
import edu.eci.dosw.oficioya.dto.SolicitudResponseDto;
import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Solicitud;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.EstadoInvalidoException;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.repository.SolicitudRepository;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    public SolicitudService(SolicitudRepository solicitudRepository, UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public SolicitudResponseDto crear(SolicitudRequestDto req, String correoContratante) {
        Usuario contratante = usuarioRepository.findByCorreo(correoContratante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario contratante no encontrado"));

        Usuario trabajador = usuarioRepository.findByIdAndRol(req.getTrabajadorId(), Rol.TRABAJADOR)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));

        Solicitud solicitud = new Solicitud();
        solicitud.setContratante(contratante);
        solicitud.setTrabajador(trabajador);
        solicitud.setDescripcion(req.getDescripcion());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        Solicitud guardada = solicitudRepository.save(solicitud);
        return aDto(guardada);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDto> listar(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        List<Solicitud> solicitudes;
        if (usuario.getRol() == Rol.CONTRATANTE) {
            solicitudes = solicitudRepository.findByContratanteId(usuario.getId());
        } else if (usuario.getRol() == Rol.TRABAJADOR) {
            solicitudes = solicitudRepository.findByTrabajadorId(usuario.getId());
        } else {
            solicitudes = Collections.emptyList();
        }

        return solicitudes.stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponseDto obtener(Long id, String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        return solicitudRepository.findByIdAndParticipante(id, usuario.getId())
                .map(this::aDto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));
    }

    public SolicitudResponseDto aceptar(Long id, String correoTrabajador) {
        Usuario trabajador = usuarioRepository.findByCorreo(correoTrabajador)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario trabajador no encontrado"));

        Solicitud solicitud = solicitudRepository.findByIdAndTrabajadorId(id, trabajador.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se puede aceptar una solicitud en estado PENDIENTE");
        }

        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        Solicitud guardada = solicitudRepository.save(solicitud);
        return aDto(guardada);
    }

    public SolicitudResponseDto rechazar(Long id, String correoTrabajador) {
        Usuario trabajador = usuarioRepository.findByCorreo(correoTrabajador)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario trabajador no encontrado"));

        Solicitud solicitud = solicitudRepository.findByIdAndTrabajadorId(id, trabajador.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se puede rechazar una solicitud en estado PENDIENTE");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        Solicitud guardada = solicitudRepository.save(solicitud);
        return aDto(guardada);
    }

    public SolicitudResponseDto cancelar(Long id, String correoContratante) {
        Usuario contratante = usuarioRepository.findByCorreo(correoContratante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario contratante no encontrado"));

        Solicitud solicitud = solicitudRepository.findByIdAndContratanteId(id, contratante.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE && solicitud.getEstado() != EstadoSolicitud.ACEPTADA) {
            throw new EstadoInvalidoException("Solo se puede cancelar una solicitud en estado PENDIENTE o ACEPTADA");
        }

        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        Solicitud guardada = solicitudRepository.save(solicitud);
        return aDto(guardada);
    }

    private SolicitudResponseDto aDto(Solicitud s) {
        return new SolicitudResponseDto(
                s.getId(),
                s.getDescripcion(),
                s.getEstado(),
                s.getContratante() != null ? s.getContratante().getId() : null,
                s.getTrabajador() != null ? s.getTrabajador().getId() : null,
                s.getFechaCreacion());
    }
}

