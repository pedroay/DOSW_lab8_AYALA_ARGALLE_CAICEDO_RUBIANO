package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.LoginRequestDto;
import edu.eci.dosw.oficioya.dto.LoginResponseDto;
import edu.eci.dosw.oficioya.dto.RegistroRequestDto;
import edu.eci.dosw.oficioya.dto.UsarioResponseDTO;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.CredencialesInvalidasException;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import edu.eci.dosw.oficioya.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsarioResponseDTO registrar(RegistroRequestDto req) {
        String correoNormalizado = req.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            throw new IllegalArgumentException("El correo ya está en uso"); 
        }

        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setCorreo(correoNormalizado);
        u.setTelefono(req.getTelefono());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
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

    public LoginResponseDto login(LoginRequestDto req) {
        String correoNormalizado = req.getCorreo() != null ? req.getCorreo().trim().toLowerCase() : "";

        Usuario u = usuarioRepository.findByCorreo(correoNormalizado)
                .filter(x -> passwordEncoder.matches(req.getPassword(), x.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        String token = jwtService.generar(u.getCorreo(), u.getRol().name());
        return new LoginResponseDto("Bearer", token, jwtService.getTtlSeconds());
    }
}
