package com.tpibackend.fleet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpibackend.fleet.config.SecurityConfig;
import com.tpibackend.fleet.exception.SecurityExceptionHandler;
import com.tpibackend.fleet.model.dto.CamionRequest;
import com.tpibackend.fleet.model.dto.CamionResponse;
import com.tpibackend.fleet.service.CamionService;
import java.math.BigDecimal;
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

@WebMvcTest(CamionController.class)
@Import({SecurityConfig.class, SecurityExceptionHandler.class})
@ActiveProfiles("test")
class CamionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CamionService camionService;

    @Test
    void listarDisponiblesSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/trucks/available"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listarDisponiblesConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(get("/api/trucks/available").with(jwtWithRoles("TRANSPORTISTA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void listarDisponiblesConOperadorDevuelve200() throws Exception {
        when(camionService.findAll(Boolean.TRUE)).thenReturn(
            List.of(new CamionResponse(1L, null, null, null, null, null, null, null, null))
        );

        mockMvc.perform(get("/api/trucks/available").with(jwtWithRoles("OPERADOR")))
            .andExpect(status().isOk());
    }

    @Test
    void crearCamionSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/trucks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequest())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void crearCamionConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/trucks")
                .with(jwtWithRoles("TRANSPORTISTA"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    void crearCamionConOperadorDevuelve201() throws Exception {
        when(camionService.create(any())).thenReturn(
            new CamionResponse(1L, null, null, null, null, null, null, null, null)
        );

        mockMvc.perform(post("/api/trucks")
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearRequest())))
            .andExpect(status().isCreated());
    }

    private CamionRequest crearRequest() {
        return new CamionRequest(
            "AA123BB",
            "Transportista Test",
            "+54911234567",
            BigDecimal.valueOf(2000),
            BigDecimal.valueOf(30),
            true,
            BigDecimal.valueOf(900),
            BigDecimal.valueOf(0.3)
        );
    }

    private RequestPostProcessor jwtWithRoles(String... roles) {
        return jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of(roles))));
    }
}
