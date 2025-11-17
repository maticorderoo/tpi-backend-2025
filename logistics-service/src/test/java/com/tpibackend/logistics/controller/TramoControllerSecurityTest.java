package com.tpibackend.logistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tpibackend.logistics.config.SecurityConfig;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.exception.SecurityExceptionHandler;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.model.enums.TramoTipo;
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

@WebMvcTest(TramoController.class)
@Import({SecurityConfig.class, SecurityExceptionHandler.class})
@ActiveProfiles("test")
@TestPropertySource(properties = "server.servlet.context-path=/api")
class TramoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TramoService tramoService;

    @Test
    void asignarCamionSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/logistics/tramos/2/asignaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"camionId\":1}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void asignarCamionConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/logistics/tramos/2/asignaciones")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"camionId\":1}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void asignarCamionConOperadorDevuelve200() throws Exception {
        when(tramoService.asignarCamion(any(), any())).thenReturn(tramoResponse());

        mockMvc.perform(post("/logistics/tramos/2/asignaciones")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"camionId\":1}"))
            .andExpect(status().isOk());
    }

    @Test
    void iniciarTramoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/logistics/tramos/2/inicios")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void iniciarTramoConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/logistics/tramos/2/inicios")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void iniciarTramoConTransportistaDevuelve200() throws Exception {
        when(tramoService.iniciarTramo(any())).thenReturn(tramoResponse());

        mockMvc.perform(post("/logistics/tramos/2/inicios")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void finalizarTramoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/logistics/tramos/2/finalizaciones")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void finalizarTramoConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/logistics/tramos/2/finalizaciones")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void finalizarTramoConTransportistaDevuelve200() throws Exception {
        when(tramoService.finalizarTramo(any())).thenReturn(tramoResponse());

        mockMvc.perform(post("/logistics/tramos/2/finalizaciones")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
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

    private TramoResponse tramoResponse() {
        return new TramoResponse(
            1L,
            1L,
            LocationType.ORIGEN,
            10L,
            LocationType.DESTINO,
            20L,
            TramoTipo.ORIGEN_DESTINO,
            TramoEstado.ESTIMADO,
            BigDecimal.ZERO,
            null,
            OffsetDateTime.now(),
            null,
            null,
            15d,
            null,
            90L,
            null,
            0,
            null,
            null
        );
    }
}
