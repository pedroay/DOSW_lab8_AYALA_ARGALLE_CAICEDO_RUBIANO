package edu.eci.dosw.oficioya.repository;

import edu.eci.dosw.oficioya.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import edu.eci.dosw.oficioya.entity.Rol;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRol(Rol rol);

    Optional<Usuario> findByIdAndRol(Long id, Rol rol);
}