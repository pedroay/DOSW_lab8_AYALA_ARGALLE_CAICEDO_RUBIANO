package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.PerfilAdmin;
import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.dto.UsarioResponseDTO;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsarioResponseDTO obtenerMiPerfil(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        UsarioResponseDTO dto = new UsarioResponseDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setCorreo(u.getCorreo());
        dto.setTelefono(u.getTelefono());
        dto.setRol(u.getRol().name());
        dto.setOficioPrincipal(u.getOficioPrincipal());
        dto.setZona(u.getZona());
        return dto;
    }

    public List<PerfilPublico> listarTrabajadores() {
        return usuarioRepository.findByRol(Rol.TRABAJADOR).stream()
                .map(this::aPublico)
                .toList();
    }

    public PerfilPublico obtenerTrabajador(Long id) {
        return usuarioRepository.findByIdAndRol(id, Rol.TRABAJADOR)
                .map(this::aPublico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));
    }

    public List<PerfilAdmin> listarParaAdmin() {
        return usuarioRepository.findAll().stream()
                .map(u -> new PerfilAdmin(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(),
                        u.getRol().name(), u.getOficioPrincipal(), u.getZona()))
                .toList();
    }

    private PerfilPublico aPublico(Usuario u) {
        return new PerfilPublico(u.getId(), u.getNombre(), u.getOficioPrincipal(), u.getZona());
    }
}