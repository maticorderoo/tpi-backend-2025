package com.tpibackend.logistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tpibackend.logistics.config.SecurityConfig;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.exception.SecurityExceptionHandler;
import com.tpibackend.logistics.service.TramoService;
import java.time.OffsetDateTime;
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

@WebMvcTest(TramoController.class)
@Import({SecurityConfig.class, SecurityExceptionHandler.class})
@ActiveProfiles("test")
class TramoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TramoService tramoService;

    @Test
    void asignarCamionSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/logistics/tramos/2/asignaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"camionId\":1,\"pesoCarga\":100,\"volumenCarga\":10}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void asignarCamionConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/logistics/tramos/2/asignaciones")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"camionId\":1,\"pesoCarga\":100,\"volumenCarga\":10}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void asignarCamionConOperadorDevuelve200() throws Exception {
        when(tramoService.asignarCamion(any(), any())).thenReturn(
            new TramoResponse(1L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        );

        mockMvc.perform(post("/api/logistics/tramos/2/asignaciones")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"camionId\":1,\"pesoCarga\":100,\"volumenCarga\":10}"))
            .andExpect(status().isOk());
    }

    @Test
    void iniciarTramoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/logistics/tramos/2/inicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fechaHoraInicio\":\"" + OffsetDateTime.now().toString() + "\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void iniciarTramoConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/logistics/tramos/2/inicios")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fechaHoraInicio\":\"" + OffsetDateTime.now().toString() + "\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void iniciarTramoConTransportistaDevuelve200() throws Exception {
        when(tramoService.iniciarTramo(any(), any())).thenReturn(
            new TramoResponse(1L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        );

        mockMvc.perform(post("/api/logistics/tramos/2/inicios")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fechaHoraInicio\":\"" + OffsetDateTime.now().toString() + "\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void finalizarTramoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/logistics/tramos/2/finalizaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(finTramoJson()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void finalizarTramoConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/logistics/tramos/2/finalizaciones")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(finTramoJson()))
            .andExpect(status().isForbidden());
    }

    @Test
    void finalizarTramoConTransportistaDevuelve200() throws Exception {
        when(tramoService.finalizarTramo(any(), any())).thenReturn(
            new TramoResponse(1L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        );

        mockMvc.perform(post("/api/logistics/tramos/2/finalizaciones")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(finTramoJson()))
            .andExpect(status().isOk());
    }

    private String finTramoJson() {
        return "{\"fechaHoraFin\":\"" + OffsetDateTime.now().toString() + "\",\"kmReal\":100,\"consumoLitrosKm\":0.3,\"precioCombustible\":700,\"costoKmBase\":900}";
    }

    private RequestPostProcessor jwtWithRoles(String... roles) {
        return jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of(roles))));
    }
}
