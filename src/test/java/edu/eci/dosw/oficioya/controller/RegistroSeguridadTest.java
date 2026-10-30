package edu.eci.dosw.oficioya.controller;

import tools.jackson.databind.ObjectMapper;
import edu.eci.dosw.oficioya.dto.RegistroRequestDto;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RegistroSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        // Limpia H2 antes de cada prueba para garantizar determinismo
        usuarioRepository.deleteAll();
    }

    @Test
    void pt01_registroValido_retorna201YNoExponePassword() throws Exception {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setNombre("Pedro Gomez");
        req.setCorreo("pedro@example.com");
        req.setTelefono("3001234567");
        req.setPassword("ContrasenaDemo123!");
        req.setRol("TRABAJADOR");

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void pt02_correoRepetido_retorna409() throws Exception {
        RegistroRequestDto req1 = new RegistroRequestDto();
        req1.setNombre("Usuario 1");
        req1.setCorreo("duplicado@example.com");
        req1.setTelefono("111");
        req1.setPassword("Valida1234");
        req1.setRol("CONTRATANTE");

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        RegistroRequestDto req2 = new RegistroRequestDto();
        req2.setNombre("Usuario 2");
        req2.setCorreo("duplicado@example.com");
        req2.setTelefono("222");
        req2.setPassword("OtraValida1234");
        req2.setRol("TRABAJADOR");

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }

    @Test
    void pt03_adminOClaveCorta_retorna400() throws Exception {
        // Intento con ADMIN
        RegistroRequestDto reqAdmin = new RegistroRequestDto();
        reqAdmin.setNombre("Admin Fake");
        reqAdmin.setCorreo("admin@example.com");
        reqAdmin.setTelefono("000");
        reqAdmin.setPassword("Valida1234");
        reqAdmin.setRol("ADMIN"); // Rol no permitido

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqAdmin)))
                .andExpect(status().isBadRequest());

        // Intento con clave corta
        RegistroRequestDto reqClaveCorta = new RegistroRequestDto();
        reqClaveCorta.setNombre("Clave Corta");
        reqClaveCorta.setCorreo("corta@example.com");
        reqClaveCorta.setTelefono("123");
        reqClaveCorta.setPassword("1234567"); 
        reqClaveCorta.setRol("TRABAJADOR");

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqClaveCorta)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pt04_persistenciaContrasena_hashBcryptDistintoDelOriginal() throws Exception {
        String rawPassword = "SuperSecretPassword123";
        String correo = "hash@example.com";

        RegistroRequestDto req = new RegistroRequestDto();
        req.setNombre("Tester Hash");
        req.setCorreo(correo);
        req.setTelefono("333");
        req.setPassword(rawPassword);
        req.setRol("TRABAJADOR");

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        Usuario usuarioGuardado = usuarioRepository.findByCorreo(correo).orElseThrow();
        assertNotNull(usuarioGuardado.getPasswordHash());
        assertNotEquals(rawPassword, usuarioGuardado.getPasswordHash());
        assertTrue(usuarioGuardado.getPasswordHash().startsWith("$2a$")); 
    }
}