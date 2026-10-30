package edu.eci.dosw.oficioya.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET_32_BYTES = "clave-super-secreta-de-prueba-de-al-menos-32-bytes";
    private static final String ISSUER = "oficioya";
    private static final long TTL_MINUTES = 30;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_32_BYTES, ISSUER, TTL_MINUTES);
    }

    @Test
    @DisplayName("Debe generar un token válido con los claims requeridos")
    void generarTokenValido() {
        String correo = "pedro@example.com";
        String rol = "TRABAJADOR";

        String token = jwtService.generar(correo, rol);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = jwtService.validar(token);
        assertEquals(correo, claims.getSubject());
        assertEquals(rol, claims.get("role", String.class));
        assertEquals(ISSUER, claims.getIssuer());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    @DisplayName("Debe rechazar un token alterado")
    void rechazarTokenAlterado() {
        String token = jwtService.generar("pedro@example.com", "TRABAJADOR");
        String tokenAlterado = alterarFirma(token);

        assertThrows(JwtException.class, () -> jwtService.validar(tokenAlterado));
    }

    @Test
    @DisplayName("Debe rechazar un token vencido")
    void rechazarTokenVencido() {
        JwtService servicioExpirado = new JwtService(SECRET_32_BYTES, ISSUER, -1);
        String tokenVencido = servicioExpirado.generar("pedro@example.com", "TRABAJADOR");

        assertThrows(ExpiredJwtException.class, () -> jwtService.validar(tokenVencido));
    }

    @Test
    @DisplayName("Debe rechazar un token con emisor diferente")
    void rechazarTokenEmisorInvalido() {
        JwtService otroEmisor = new JwtService(SECRET_32_BYTES, "otro-emisor", TTL_MINUTES);
        String token = otroEmisor.generar("pedro@example.com", "TRABAJADOR");

        assertThrows(JwtException.class, () -> jwtService.validar(token));
    }

    @Test
    @DisplayName("Debe calcular correctamente el TTL en minutos y segundos")
    void verificarTtl() {
        assertEquals(30, jwtService.getTtlMinutes());
        assertEquals(1800, jwtService.getTtlSeconds());
    }

    private String alterarFirma(String token) {
        int index = token.length() - 5;
        char c = token.charAt(index) == 'A' ? 'B' : 'A';
        return token.substring(0, index) + c + token.substring(index + 1);
    }
}

