package edu.eci.dosw.service;

import edu.eci.dosw.oficioya.dto.RegistroRequestDto;
import edu.eci.dosw.oficioya.dto.UsarioResponseDTO;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsarioResponseDTO registrar(RegistroRequestDto req) {
        String correoNormalizado = req.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            // Deberás capturar esta excepción en tu ApiExceptionHandler para retornar 409
            throw new IllegalArgumentException("El correo ya está en uso"); 
        }

        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setCorreo(correoNormalizado);
        u.setTelefono(req.getTelefono());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword())); // Genera el hash BCrypt
        u.setRol(Rol.valueOf(req.getRol())); 
        u.setOficioPrincipal(req.getOficioPrincipal());
        u.setZona(req.getZona());

        Usuario guardado = usuarioRepository.save(u);

        UsarioResponseDTO response = new UsarioResponseDTO();
        response.setId(guardado.getId());
        response.setNombre(guardado.getNombre());
        response.setCorreo(guardado.getCorreo());
        response.setTelefono(guardado.getTelefono());
        response.setRol(guardado.getRol().name());
        response.setOficioPrincipal(guardado.getOficioPrincipal());
        response.setZona(guardado.getZona());
        
        return response;
    }
}
