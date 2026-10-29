package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TokenSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    private static final String RUTA_PROTEGIDA = "/api/v1/usuarios/me";
    private static final String MENSAJE_TOKEN_INVALIDO = "Token inválido o expirado";

    @Test
    @DisplayName("PT-07: Recurso protegido sin token responde 401 con formato ApiError")
    void pt07_recursoProtegidoSinToken_retorna401ConApiError() throws Exception {
        mockMvc.perform(get(RUTA_PROTEGIDA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_TOKEN_INVALIDO))
                .andExpect(jsonPath("$.ruta").value(RUTA_PROTEGIDA))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("PT-08: Token con la firma alterada responde 401 con formato ApiError")
    void pt08_tokenAlterado_retorna401() throws Exception {
        String token = jwtService.generar("pedro@example.com", "TRABAJADOR");
        String tokenAlterado = alterarFirma(token);

        mockMvc.perform(get(RUTA_PROTEGIDA)
                        .header("Authorization", "Bearer " + tokenAlterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_TOKEN_INVALIDO))
                .andExpect(jsonPath("$.ruta").value(RUTA_PROTEGIDA));
    }

    @Test
    @DisplayName("PT-09: Token vencido responde 401 con formato ApiError")
    void pt09_tokenVencido_retorna401() throws Exception {
        JwtService servicioExpirado = new JwtService(secret, issuer, -1);
        String tokenVencido = servicioExpirado.generar("pedro@example.com", "TRABAJADOR");

        mockMvc.perform(get(RUTA_PROTEGIDA)
                        .header("Authorization", "Bearer " + tokenVencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_TOKEN_INVALIDO))
                .andExpect(jsonPath("$.ruta").value(RUTA_PROTEGIDA));
    }

    @Test
    @DisplayName("Acceso denegado: Usuario sin rol ADMIN accediendo a /admin/** responde 403 con ApiError")
    void accesoDenegado_retorna403ConApiError() throws Exception {
        String tokenTrabajador = jwtService.generar("trabajador@example.com", "TRABAJADOR");
        String rutaAdmin = "/api/v1/admin/usuarios";

        mockMvc.perform(get(rutaAdmin)
                        .header("Authorization", "Bearer " + tokenTrabajador))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.ruta").value(rutaAdmin));
    }

    private String alterarFirma(String token) {
        int index = token.length() - 10;
        char c = token.charAt(index) == 'A' ? 'B' : 'A';
        return token.substring(0, index) + c + token.substring(index + 1);
    }
}
