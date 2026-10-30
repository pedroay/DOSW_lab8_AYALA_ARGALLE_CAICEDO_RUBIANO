package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.SolicitudRequestDto;
import edu.eci.dosw.oficioya.dto.SolicitudResponseDto;
import edu.eci.dosw.oficioya.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponseDto crear(@Valid @RequestBody SolicitudRequestDto req, Authentication auth) {
        return solicitudService.crear(req, auth.getName());
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping
    public List<SolicitudResponseDto> listar(Authentication auth) {
        return solicitudService.listar(auth.getName());
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping("/{id}")
    public SolicitudResponseDto obtener(@PathVariable Long id, Authentication auth) {
        return solicitudService.obtener(id, auth.getName());
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/aceptar")
    public SolicitudResponseDto aceptar(@PathVariable Long id, Authentication auth) {
        return solicitudService.aceptar(id, auth.getName());
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/rechazar")
    public SolicitudResponseDto rechazar(@PathVariable Long id, Authentication auth) {
        return solicitudService.rechazar(id, auth.getName());
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PatchMapping("/{id}/cancelar")
    public SolicitudResponseDto cancelar(@PathVariable Long id, Authentication auth) {
        return solicitudService.cancelar(id, auth.getName());
    }
}

