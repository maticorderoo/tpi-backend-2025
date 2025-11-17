package com.tpibackend.logistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpibackend.logistics.config.SecurityConfig;
import com.tpibackend.logistics.dto.response.RutaResponse;
import com.tpibackend.logistics.exception.SecurityExceptionHandler;
import com.tpibackend.logistics.service.RutaService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(RutaController.class)
@Import({SecurityConfig.class, SecurityExceptionHandler.class})
@ActiveProfiles("test")
class RutaControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RutaService rutaService;

    @Test
    void crearRutaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/logistics/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearRutaJson()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void crearRutaConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/logistics/routes")
                .with(jwtWithRoles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearRutaJson()))
            .andExpect(status().isForbidden());
    }

    @Test
    void crearRutaConRolOperadorDevuelve201() throws Exception {
        when(rutaService.crearRuta(any())).thenReturn(
            new RutaResponse(1L, null, 0, 0, null, null, null, null, List.of())
        );

        mockMvc.perform(post("/api/logistics/routes")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearRutaJson()))
            .andExpect(status().isCreated());
    }

    @Test
    void asignarRutaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/logistics/routes/1/asignaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitudId\":1}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void asignarRutaConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/logistics/routes/1/asignaciones")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitudId\":1}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void asignarRutaConOperadorDevuelve200() throws Exception {
        when(rutaService.asignarRuta(any(), any())).thenReturn(
            new RutaResponse(1L, null, 0, 0, null, null, null, null, List.of())
        );

        mockMvc.perform(post("/api/logistics/routes/1/asignaciones")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitudId\":1}"))
            .andExpect(status().isOk());
    }

    @Test
    void obtenerRutaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/logistics/routes/solicitudes/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerRutaConRolNoPermitidoDevuelve403() throws Exception {
        mockMvc.perform(get("/api/logistics/routes/solicitudes/1").with(jwtWithRoles("TRANSPORTISTA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void obtenerRutaConOperadorDevuelve200() throws Exception {
        when(rutaService.obtenerRutaPorSolicitud(any())).thenReturn(
            java.util.Optional.of(new RutaResponse(1L, null, 0, 0, null, null, null, null, List.of()))
        );

        mockMvc.perform(get("/api/logistics/routes/solicitudes/1").with(jwtWithRoles("OPERADOR")))
            .andExpect(status().isOk());
    }

    private String crearRutaJson() throws Exception {
        Map<String, Object> payload = Map.of(
            "origen", Map.of("tipo", "CLIENTE", "descripcion", "Origen"),
            "destino", Map.of("tipo", "CLIENTE", "descripcion", "Destino"),
            "depositosIntermedios", List.of(),
            "costoKmBase", 1000,
            "consumoLitrosKm", 0.3,
            "precioCombustible", 700,
            "pesoCarga", 1000,
            "volumenCarga", 20
        );
        return objectMapper.writeValueAsString(payload);
    }

    private RequestPostProcessor jwtWithRoles(String... roles) {
        return jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of(roles))));
    }
}
