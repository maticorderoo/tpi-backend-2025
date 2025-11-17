package com.tpibackend.orders.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tpibackend.orders.config.SecurityConfig;
import com.tpibackend.orders.dto.response.PendingContainerResponseDto;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import com.tpibackend.orders.service.ContenedorQueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = ContenedorQueryController.class)
@Import(SecurityConfig.class)
class ContenedorQueryControllerTest {

    private static final String BASE_URL = "/orders/containers/pendientes";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContenedorQueryService contenedorQueryService;

    @Test
    void obtenerPendientes_debeRetornarListado() throws Exception {
        var response = PendingContainerResponseDto.builder()
                .solicitudId(10L)
                .contenedorId(5L)
                .rutaId(3L)
                .tramoId(7L)
                .estadoTramo("ASIGNADO")
                .estadoContenedor(ContenedorEstado.PROGRAMADA)
                .build();
        when(contenedorQueryService.obtenerContenedoresPendientes(null, null))
                .thenReturn(List.of(response));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERADOR")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].solicitudId").value(10L))
                .andExpect(jsonPath("$[0].estadoTramo").value("ASIGNADO"));
    }
}
