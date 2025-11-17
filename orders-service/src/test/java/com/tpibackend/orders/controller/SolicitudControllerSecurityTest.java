package com.tpibackend.orders.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpibackend.orders.config.SecurityConfig;
import com.tpibackend.orders.dto.request.ClienteRequestDto;
import com.tpibackend.orders.dto.request.ContenedorRequestDto;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.exception.SecurityExceptionHandler;
import com.tpibackend.orders.service.SolicitudService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(SolicitudController.class)
@Import({SecurityConfig.class, SecurityExceptionHandler.class})
@ActiveProfiles("test")
class SolicitudControllerSecurityTest {

    private static final String BASE_URL = "/orders";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SolicitudService solicitudService;

    @Test
    void crearSolicitudSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearSolicitudRequest())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void crearSolicitudConRolIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .with(jwtWithRoles("OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearSolicitudRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    void crearSolicitudConRolClienteDevuelve201() throws Exception {
        when(solicitudService.crearSolicitud(any())).thenReturn(SolicitudResponseDto.builder().build());

        mockMvc.perform(post(BASE_URL)
                .with(jwtWithRoles("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearSolicitudRequest())))
            .andExpect(status().isCreated());
    }

    @Test
    void obtenerSolicitudSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get(BASE_URL + "/10"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerSolicitudConRolNoPermitidoDevuelve403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/10").with(jwtWithRoles("TRANSPORTISTA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void obtenerSolicitudConRolValidoDevuelve200() throws Exception {
        when(solicitudService.obtenerSolicitud(10L)).thenReturn(SolicitudResponseDto.builder().build());

        mockMvc.perform(get(BASE_URL + "/10").with(jwtWithRoles("OPERADOR")))
            .andExpect(status().isOk());
    }

    @Test
    void seguimientoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get(BASE_URL + "/5/tracking"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void seguimientoConRolNoPermitidoDevuelve403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/5/tracking").with(jwtWithRoles("TRANSPORTISTA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void seguimientoConRolValidoDevuelve200() throws Exception {
        when(solicitudService.obtenerSeguimientoPorContenedor(5L)).thenReturn(SeguimientoResponseDto.builder().build());

        mockMvc.perform(get(BASE_URL + "/5/tracking").with(jwtWithRoles("OPERADOR")))
            .andExpect(status().isOk());
    }

    private SolicitudCreateRequest crearSolicitudRequest() {
        SolicitudCreateRequest request = new SolicitudCreateRequest();
        ClienteRequestDto cliente = new ClienteRequestDto();
        cliente.setNombre("Cliente Prueba");
        cliente.setEmail("cliente@correo.com");
        cliente.setTelefono("+54 11 5555-2222");
        cliente.setCuit("30-00000000-0");
        request.setCliente(cliente);
        ContenedorRequestDto contenedor = new ContenedorRequestDto();
        contenedor.setCodigo("CONT-SEC-0001");
        contenedor.setPeso(BigDecimal.TEN);
        contenedor.setVolumen(BigDecimal.ONE);
        // El estado se gestiona automáticamente, no se debe setear
        request.setContenedor(contenedor);
        com.tpibackend.orders.dto.request.UbicacionRequestDto origen = new com.tpibackend.orders.dto.request.UbicacionRequestDto();
        origen.setDireccion("Buenos Aires");
        origen.setLatitud(-34.6037);
        origen.setLongitud(-58.3816);
        request.setOrigen(origen);
        com.tpibackend.orders.dto.request.UbicacionRequestDto destino = new com.tpibackend.orders.dto.request.UbicacionRequestDto();
        destino.setDireccion("Cordoba");
        destino.setLatitud(-31.4201);
        destino.setLongitud(-64.1888);
        request.setDestino(destino);
        return request;
    }

    private RequestPostProcessor jwtWithRoles(String... roles) {
        return jwt().authorities(Arrays.stream(roles)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList()));
    }
}
