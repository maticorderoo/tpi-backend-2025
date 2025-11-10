package com.tpibackend.fleet.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tpibackend.fleet.config.SecurityConfig;
import com.tpibackend.fleet.exception.SecurityExceptionHandler;
import com.tpibackend.fleet.service.FleetMetricsService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(MetricsController.class)
@Import({SecurityConfig.class, SecurityExceptionHandler.class})
@ActiveProfiles("test")
class MetricsControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FleetMetricsService fleetMetricsService;

    @Test
    void obtenerPromediosSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/metrics/promedios"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerPromediosConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(get("/metrics/promedios").with(jwtWithRoles("TRANSPORTISTA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPromediosConRolPermitidoDevuelve200() throws Exception {
        mockMvc.perform(get("/metrics/promedios").with(jwtWithRoles("OPERADOR")))
            .andExpect(status().isOk());
    }

    private RequestPostProcessor jwtWithRoles(String... roles) {
        return jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of(roles))));
    }
}
