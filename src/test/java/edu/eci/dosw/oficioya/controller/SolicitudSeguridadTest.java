package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.SolicitudRequestDto;
import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Solicitud;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.SolicitudRepository;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SolicitudSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario contratante1;
    private Usuario contratante2;
    private Usuario trabajador1;
    private Usuario trabajador2;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        solicitudRepository.deleteAll();
        usuarioRepository.deleteAll();

        contratante1 = crearUsuario("Contratante Uno", "contratante1@example.com", Rol.CONTRATANTE);
        contratante2 = crearUsuario("Contratante Dos", "contratante2@example.com", Rol.CONTRATANTE);
        trabajador1 = crearUsuario("Trabajador Uno", "trabajador1@example.com", Rol.TRABAJADOR);
        trabajador2 = crearUsuario("Trabajador Dos", "trabajador2@example.com", Rol.TRABAJADOR);
        admin = crearUsuario("Admin", "admin@example.com", Rol.ADMIN);
    }

    private Usuario crearUsuario(String nombre, String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setTelefono("3001234567");
        u.setPasswordHash("$2a$10$dummyHashBcryptForTestingOnly12345678901234567890");
        u.setRol(rol);
        return usuarioRepository.save(u);
    }

    private Solicitud crearSolicitud(Usuario c, Usuario t, String descripcion, EstadoSolicitud estado) {
        Solicitud s = new Solicitud();
        s.setContratante(c);
        s.setTrabajador(t);
        s.setDescripcion(descripcion);
        s.setEstado(estado);
        return solicitudRepository.save(s);
    }

    @Test
    @DisplayName("Crear solicitud válida como CONTRATANTE retorna 201 y estado PENDIENTE")
    @WithMockUser(username = "contratante1@example.com", roles = "CONTRATANTE")
    void crearSolicitudValida_retorna201() throws Exception {
        SolicitudRequestDto req = new SolicitudRequestDto(trabajador1.getId(), "Arreglo de tubería");

        mockMvc.perform(post("/api/v1/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.descripcion").value("Arreglo de tubería"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.contratanteId").value(contratante1.getId()))
                .andExpect(jsonPath("$.trabajadorId").value(trabajador1.getId()));
    }

    @Test
    @DisplayName("PT-13: TRABAJADOR intenta crear una solicitud -> 403 Forbidden")
    @WithMockUser(username = "trabajador1@example.com", roles = "TRABAJADOR")
    void pt13_trabajadorIntentaCrearSolicitud_retorna403() throws Exception {
        SolicitudRequestDto req = new SolicitudRequestDto(trabajador2.getId(), "Intento inválido");

        mockMvc.perform(post("/api/v1/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PT-14: Contratante lista solicitudes -> Solo las propias")
    @WithMockUser(username = "contratante1@example.com", roles = "CONTRATANTE")
    void pt14_contratanteListaSolicitudes_soloLasPropias() throws Exception {
        crearSolicitud(contratante1, trabajador1, "Solicitud 1 de Contratante 1", EstadoSolicitud.PENDIENTE);
        crearSolicitud(contratante1, trabajador2, "Solicitud 2 de Contratante 1", EstadoSolicitud.ACEPTADA);
        crearSolicitud(contratante2, trabajador1, "Solicitud de Contratante 2", EstadoSolicitud.PENDIENTE);

        mockMvc.perform(get("/api/v1/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].contratanteId").value(contratante1.getId()))
                .andExpect(jsonPath("$[1].contratanteId").value(contratante1.getId()));
    }

    @Test
    @DisplayName("Trabajador lista solicitudes -> Solo las asignadas a él")
    @WithMockUser(username = "trabajador1@example.com", roles = "TRABAJADOR")
    void trabajadorListaSolicitudes_soloLasAsignadas() throws Exception {
        crearSolicitud(contratante1, trabajador1, "Para T1", EstadoSolicitud.PENDIENTE);
        crearSolicitud(contratante2, trabajador1, "También para T1", EstadoSolicitud.PENDIENTE);
        crearSolicitud(contratante1, trabajador2, "Para T2", EstadoSolicitud.PENDIENTE);

        mockMvc.perform(get("/api/v1/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].trabajadorId").value(trabajador1.getId()))
                .andExpect(jsonPath("$[1].trabajadorId").value(trabajador1.getId()));
    }

    @Test
    @DisplayName("PT-15: Consultar o cancelar una solicitud ajena -> 404, sin cambios en base de datos")
    @WithMockUser(username = "contratante2@example.com", roles = "CONTRATANTE")
    void pt15_consultarOCancelarSolicitudAjena_retorna404SinCambios() throws Exception {
        Solicitud ajena = crearSolicitud(contratante1, trabajador1, "Solicitud de Contratante 1", EstadoSolicitud.PENDIENTE);

        // Consultar solicitud ajena -> 404
        mockMvc.perform(get("/api/v1/solicitudes/" + ajena.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Cancelar solicitud ajena -> 404
        mockMvc.perform(patch("/api/v1/solicitudes/" + ajena.getId() + "/cancelar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Verificar que en base de datos el estado NO cambió
        Solicitud noModificada = solicitudRepository.findById(ajena.getId()).orElseThrow();
        assertEquals(EstadoSolicitud.PENDIENTE, noModificada.getEstado());
    }

    @Test
    @DisplayName("PT-16: Aceptar con trabajador no asignado -> 404; con contratante -> 403")
    void pt16_aceptarConTrabajadorNoAsignado_404_conContratante_403() throws Exception {
        Solicitud s = crearSolicitud(contratante1, trabajador1, "Solicitud para T1", EstadoSolicitud.PENDIENTE);

        // Con trabajador 2 (no asignado) -> 404
        mockMvc.perform(patch("/api/v1/solicitudes/" + s.getId() + "/aceptar")
                        .with(user("trabajador2@example.com").roles("TRABAJADOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Con contratante 1 -> 403 (rol incorrecto)
        mockMvc.perform(patch("/api/v1/solicitudes/" + s.getId() + "/aceptar")
                        .with(user("contratante1@example.com").roles("CONTRATANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PT-17: ADMIN consulta /solicitudes -> 403 Forbidden")
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void pt17_adminConsultaSolicitudes_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/solicitudes/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Transiciones válidas: PENDIENTE -> ACEPTADA y PENDIENTE -> RECHAZADA por trabajador asignado")
    void transicionesValidasTrabajador() throws Exception {
        Solicitud s1 = crearSolicitud(contratante1, trabajador1, "S1", EstadoSolicitud.PENDIENTE);
        Solicitud s2 = crearSolicitud(contratante1, trabajador1, "S2", EstadoSolicitud.PENDIENTE);

        // Aceptar s1
        mockMvc.perform(patch("/api/v1/solicitudes/" + s1.getId() + "/aceptar")
                        .with(user("trabajador1@example.com").roles("TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        // Rechazar s2
        mockMvc.perform(patch("/api/v1/solicitudes/" + s2.getId() + "/rechazar")
                        .with(user("trabajador1@example.com").roles("TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
    }

    @Test
    @DisplayName("Transiciones válidas: PENDIENTE -> CANCELADA y ACEPTADA -> CANCELADA por contratante autor")
    void transicionesValidasContratante() throws Exception {
        Solicitud sPendiente = crearSolicitud(contratante1, trabajador1, "S Pendiente", EstadoSolicitud.PENDIENTE);
        Solicitud sAceptada = crearSolicitud(contratante1, trabajador1, "S Aceptada", EstadoSolicitud.ACEPTADA);

        // Cancelar pendiente
        mockMvc.perform(patch("/api/v1/solicitudes/" + sPendiente.getId() + "/cancelar")
                        .with(user("contratante1@example.com").roles("CONTRATANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        // Cancelar aceptada
        mockMvc.perform(patch("/api/v1/solicitudes/" + sAceptada.getId() + "/cancelar")
                        .with(user("contratante1@example.com").roles("CONTRATANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("Transición inválida: Intentar cancelar solicitud ya RECHAZADA o CANCELADA responde 409 Conflict")
    void transicionInvalida_retorna409() throws Exception {
        Solicitud rechazada = crearSolicitud(contratante1, trabajador1, "S Rechazada", EstadoSolicitud.RECHAZADA);

        mockMvc.perform(patch("/api/v1/solicitudes/" + rechazada.getId() + "/cancelar")
                        .with(user("contratante1@example.com").roles("CONTRATANTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}

