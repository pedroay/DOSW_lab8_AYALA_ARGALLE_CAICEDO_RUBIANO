package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.LoginRequestDto;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import edu.eci.dosw.oficioya.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private static final String CORREO_TEST = "pedro@example.com";
    private static final String PASSWORD_TEST = "ContrasenaDemo123!";
    private static final String MENSAJE_ERROR_GENERICO = "Credenciales inválidas";

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setNombre("Pedro Gomez");
        usuario.setCorreo(CORREO_TEST);
        usuario.setTelefono("3001234567");
        usuario.setPasswordHash(passwordEncoder.encode(PASSWORD_TEST));
        usuario.setRol(Rol.TRABAJADOR);
        usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("PT-05: Login válido retorna 200 con JWT firmado y tipo Bearer")
    void pt05_loginValido_retorna200YToken() throws Exception {
        LoginRequestDto req = new LoginRequestDto(CORREO_TEST, PASSWORD_TEST);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiraEn").value(1800))
                .andReturn();

        JsonNode jsonResponse = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = jsonResponse.get("accessToken").asText();

        Claims claims = jwtService.validar(token);
        assertEquals(CORREO_TEST, claims.getSubject());
        assertEquals("TRABAJADOR", claims.get("role", String.class));
        assertEquals("oficioya", claims.getIssuer());
    }

    @Test
    @DisplayName("PT-06: Login con contraseña incorrecta retorna 401 con mensaje genérico")
    void pt06_loginPasswordIncorrecto_retorna401MismoMensaje() throws Exception {
        LoginRequestDto req = new LoginRequestDto(CORREO_TEST, "ClaveEquivocada123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_ERROR_GENERICO))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("PT-06: Login con correo inexistente retorna 401 con el mismo mensaje genérico")
    void pt06_loginCorreoInexistente_retorna401MismoMensaje() throws Exception {
        LoginRequestDto req = new LoginRequestDto("noexiste@example.com", PASSWORD_TEST);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value(MENSAJE_ERROR_GENERICO))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("Login con cuerpo inválido o datos faltantes retorna 400 Bad Request")
    void loginDatosInvalidos_retorna400() throws Exception {
        LoginRequestDto req = new LoginRequestDto("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

