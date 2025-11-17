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
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import com.tpibackend.orders.service.SolicitudService;
import java.math.BigDecimal;
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

    private static final String BASE_URL = "/orders";

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
            .cliente(ClienteResponseDto.builder().id(1L).nombre("Juan").email("juan@test.com").build())
            .contenedor(ContenedorResponseDto.builder().id(2L).estado(ContenedorEstado.BORRADOR).build())
            .build();
        when(solicitudService.crearSolicitud(any())).thenReturn(responseDto);

        SolicitudCreateRequest request = new SolicitudCreateRequest();
        com.tpibackend.orders.dto.request.ClienteRequestDto cliente = new com.tpibackend.orders.dto.request.ClienteRequestDto();
        cliente.setNombre("ACME Corp");
        cliente.setEmail("contacto@acme.com");
        cliente.setTelefono("+54 11 5555-1111");
        request.setCliente(cliente);

        com.tpibackend.orders.dto.request.ContenedorRequestDto contenedor = new com.tpibackend.orders.dto.request.ContenedorRequestDto();
        contenedor.setPeso(new BigDecimal("1200.5"));
        contenedor.setVolumen(new BigDecimal("28.4"));
        request.setContenedor(contenedor);

        com.tpibackend.orders.dto.request.UbicacionRequestDto origen = new com.tpibackend.orders.dto.request.UbicacionRequestDto();
        origen.setDireccion("Buenos Aires");
        origen.setLatitud(-34.6037);
        origen.setLongitud(-58.3816);
        request.setOrigen(origen);

        com.tpibackend.orders.dto.request.UbicacionRequestDto destino = new com.tpibackend.orders.dto.request.UbicacionRequestDto();
        destino.setDireccion("Córdoba");
        destino.setLatitud(-31.4201);
        destino.setLongitud(-64.1888);
        request.setDestino(destino);

        mockMvc.perform(post(BASE_URL)
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.contenedor.estado").value("BORRADOR"));

        verify(solicitudService).crearSolicitud(any());
    }

    @Test
    void obtenerSeguimiento_debeRetornarSeguimiento() throws Exception {
        SeguimientoResponseDto seguimiento = SeguimientoResponseDto.builder()
            .contenedorId(2L)
            .solicitudId(10L)
            .estadoContenedor(ContenedorEstado.PROGRAMADA)
            .build();
        when(solicitudService.obtenerSeguimientoPorContenedor(2L)).thenReturn(seguimiento);

    mockMvc.perform(get(BASE_URL + "/2/tracking")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estadoContenedor").value("PROGRAMADA"));
    }

    @Test
    void calcularEstimacion_requiereRolOperador() throws Exception {
        SolicitudResponseDto response = SolicitudResponseDto.builder()
            .id(10L)
            .costoEstimado(new BigDecimal("100.00"))
            .tiempoEstimadoMinutos(120L)
            .build();
        when(solicitudService.calcularEstimacion(eq(10L), any())).thenReturn(response);

        var estimacionRequest = new com.tpibackend.orders.dto.request.EstimacionRequest();
        estimacionRequest.setPrecioCombustible(new BigDecimal("2"));
        estimacionRequest.setEstadiaEstimada(BigDecimal.ZERO);
        estimacionRequest.setOrigen("Buenos Aires");
        estimacionRequest.setDestino("Rosario");

        mockMvc.perform(post(BASE_URL + "/10/estimacion")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_OPERADOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estimacionRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.costoEstimado").value(100.00));

        mockMvc.perform(post(BASE_URL + "/10/estimacion")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(estimacionRequest)))
            .andExpect(status().isForbidden());
    }
}
