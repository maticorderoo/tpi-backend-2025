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
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.exception.SecurityExceptionHandler;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.model.enums.TramoTipo;
import com.tpibackend.logistics.service.RutaService;
import com.tpibackend.logistics.service.RutaTentativaService;
import com.tpibackend.logistics.service.TramoService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(RutaController.class)
@Import({SecurityConfig.class, SecurityExceptionHandler.class})
@ActiveProfiles("test")
@TestPropertySource(properties = "server.servlet.context-path=/api")
class RutaControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RutaService rutaService;

    @MockBean
    private RutaTentativaService rutaTentativaService;

    @MockBean
    private TramoService tramoService;

    @Test
    void crearRutaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/logistics/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearRutaJson()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void crearRutaConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/logistics/routes")
                .with(jwtWithRoles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearRutaJson()))
            .andExpect(status().isForbidden());
    }

    @Test
    void crearRutaConRolOperadorDevuelve201() throws Exception {
        when(rutaService.crearRuta(any())).thenReturn(rutaResponse());

        mockMvc.perform(post("/logistics/routes")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearRutaJson()))
            .andExpect(status().isCreated());
    }

    @Test
    void asignarRutaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/logistics/routes/1/asignaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitudId\":1}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void asignarRutaConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/logistics/routes/1/asignaciones")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitudId\":1}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void asignarRutaConOperadorDevuelve200() throws Exception {
        when(rutaService.asignarRuta(any(), any())).thenReturn(rutaResponse());

        mockMvc.perform(post("/logistics/routes/1/asignaciones")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitudId\":1}"))
            .andExpect(status().isOk());
    }

    @Test
    void obtenerRutaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/logistics/routes/solicitudes/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerRutaConRolNoPermitidoDevuelve403() throws Exception {
        mockMvc.perform(get("/logistics/routes/solicitudes/1").with(jwtWithRoles("TRANSPORTISTA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void obtenerRutaConOperadorDevuelve200() throws Exception {
        when(rutaService.obtenerRutaPorSolicitud(any())).thenReturn(
            java.util.Optional.of(rutaResponse())
        );

        mockMvc.perform(get("/logistics/routes/solicitudes/1").with(jwtWithRoles("OPERADOR")))
            .andExpect(status().isOk());
    }

    private String crearRutaJson() throws Exception {
        Map<String, Object> payload = Map.of(
            "origen", Map.of("tipo", "SOLICITUD", "descripcion", "Origen"),
            "destino", Map.of("tipo", "SOLICITUD", "descripcion", "Destino"),
            "depositosIntermedios", List.of(),
            "pesoCarga", 1000,
            "volumenCarga", 20
        );
        return objectMapper.writeValueAsString(payload);
    }

    private RequestPostProcessor jwtWithRoles(String... roles) {
        java.util.List<String> roleList = java.util.Arrays.stream(roles).toList();
        java.util.List<SimpleGrantedAuthority> authorities = roleList.stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .toList();
        return jwt()
                .jwt(jwt -> jwt.claim("realm_access", Map.of("roles", roleList)))
                .authorities(authorities.toArray(SimpleGrantedAuthority[]::new));
    }

    private RutaResponse rutaResponse() {
        return new RutaResponse(
            1L,
            99L,
            0,
            0,
            BigDecimal.ZERO,
            null,
            0L,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            List.of(tramoResponse())
        );
    }

    private TramoResponse tramoResponse() {
        return new TramoResponse(
            1L,
            1L,
            LocationType.ORIGEN,
            1L,
            LocationType.DESTINO,
            2L,
            TramoTipo.ORIGEN_DESTINO,
            TramoEstado.ESTIMADO,
            BigDecimal.ZERO,
            null,
            OffsetDateTime.now(),
            null,
            null,
            10d,
            null,
            60L,
            null,
            0,
            null,
            null
        );
    }
}
