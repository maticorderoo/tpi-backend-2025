package com.tpibackend.orders.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpibackend.orders.config.SecurityConfig;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.response.ClienteResponseDto;
import com.tpibackend.orders.dto.response.ContenedorResponseDto;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudEventoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.model.enums.SolicitudEstado;
import com.tpibackend.orders.service.SolicitudService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

@WebMvcTest(controllers = SolicitudController.class)
@Import(SecurityConfig.class)
class SolicitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SolicitudService solicitudService;

    @Test
    void crearSolicitud_debeRetornar201() throws Exception {
        SolicitudResponseDto responseDto = SolicitudResponseDto.builder()
            .id(1L)
            .estado(SolicitudEstado.BORRADOR)
            .cliente(ClienteResponseDto.builder().id(1L).nombre("Juan").email("juan@test.com").build())
            .contenedor(ContenedorResponseDto.builder().id(2L).estado("Disponible").build())
            .eventos(List.of())
            .build();
        when(solicitudService.crearSolicitud(any())).thenReturn(responseDto);

        SolicitudCreateRequest request = new SolicitudCreateRequest();
        request.setCliente(new com.tpibackend.orders.dto.request.ClienteRequestDto());
        request.setContenedor(new com.tpibackend.orders.dto.request.ContenedorRequestDto());

        mockMvc.perform(post("/solicitudes")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("BORRADOR"));

        verify(solicitudService).crearSolicitud(any());
    }

    @Test
    void obtenerSeguimiento_debeRetornarSeguimiento() throws Exception {
        SeguimientoResponseDto seguimiento = SeguimientoResponseDto.builder()
            .contenedorId(2L)
            .solicitudId(10L)
            .estadoActual(SolicitudEstado.PROGRAMADA)
            .eventos(List.of(SolicitudEventoResponseDto.builder()
                .estado(SolicitudEstado.PROGRAMADA)
                .fechaEvento(OffsetDateTime.now())
                .descripcion("Programada")
                .build()))
            .build();
        when(solicitudService.obtenerSeguimientoPorContenedor(2L)).thenReturn(seguimiento);

        mockMvc.perform(get("/seguimiento/2")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estadoActual").value("PROGRAMADA"));
    }

    @Test
    void calcularEstimacion_requiereRolOperador() throws Exception {
        SolicitudResponseDto response = SolicitudResponseDto.builder()
            .id(10L)
            .estado(SolicitudEstado.BORRADOR)
            .costoEstimado(new BigDecimal("100.00"))
            .tiempoEstimadoMinutos(120L)
            .eventos(List.of())
            .build();
        when(solicitudService.calcularEstimacion(eq(10L), any())).thenReturn(response);

        var estimacionRequest = new com.tpibackend.orders.dto.request.EstimacionRequest();
        estimacionRequest.setPrecioCombustible(new BigDecimal("2"));
        estimacionRequest.setEstadiaEstimada(BigDecimal.ZERO);
        estimacionRequest.setOrigen("Buenos Aires");
        estimacionRequest.setDestino("Rosario");

        mockMvc.perform(post("/solicitudes/10/estimacion")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_OPERADOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estimacionRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.costoEstimado").value(100.00));

        mockMvc.perform(post("/solicitudes/10/estimacion")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estimacionRequest)))
            .andExpect(status().isForbidden());
    }
}
