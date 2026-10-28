package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.RegistroRequestDto;
import edu.eci.dosw.oficioya.dto.UsarioResponseDTO;
import edu.eci.dosw.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsarioResponseDTO registrar(@Valid @RequestBody RegistroRequestDto request) {
        return authService.registrar(request);
    }
}