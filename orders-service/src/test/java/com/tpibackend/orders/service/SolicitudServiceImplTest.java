package com.tpibackend.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.tpibackend.orders.client.LogisticsClient;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.request.ClienteRequestDto;
import com.tpibackend.orders.dto.request.ContenedorRequestDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.mapper.SolicitudMapper;
import com.tpibackend.orders.model.Cliente;
import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import com.tpibackend.orders.repository.ClienteRepository;
import com.tpibackend.orders.repository.ContenedorRepository;
import com.tpibackend.orders.repository.SolicitudRepository;
import com.tpibackend.orders.service.EstadoService;
import com.tpibackend.orders.service.impl.SolicitudServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SolicitudServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private LogisticsClient logisticsClient;

    @Mock
    private EstadoService estadoService;

    private SolicitudMapper solicitudMapper = Mappers.getMapper(SolicitudMapper.class);

    private SolicitudServiceImpl solicitudService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        solicitudService = new SolicitudServiceImpl(
            clienteRepository,
            contenedorRepository,
            solicitudRepository,
            solicitudMapper,
            logisticsClient,
            estadoService
        );
    }

    @Test
    void crearSolicitud_debePersistirClienteContenedorYNuevaSolicitud() {
        SolicitudCreateRequest request = new SolicitudCreateRequest();
        ClienteRequestDto clienteDto = new ClienteRequestDto();
        clienteDto.setNombre("Juan Perez");
        clienteDto.setEmail("juan@test.com");
        clienteDto.setTelefono("12345");
        clienteDto.setCuit("30-12345678-9");
        request.setCliente(clienteDto);

        ContenedorRequestDto contenedorDto = new ContenedorRequestDto();
        contenedorDto.setCodigo("CONT-0001");
        // El estado no se setea, se gestiona automáticamente
        contenedorDto.setPeso(new BigDecimal("2000"));
        contenedorDto.setVolumen(new BigDecimal("30"));
        request.setContenedor(contenedorDto);
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

        Cliente clientePersistido = new Cliente();
        clientePersistido.setId(1L);
        clientePersistido.setNombre("Juan Perez");
        clientePersistido.setEmail("juan@test.com");
        clientePersistido.setCuit("30-12345678-9");

        Contenedor contenedorPersistido = new Contenedor();
        contenedorPersistido.setId(2L);
        contenedorPersistido.setCliente(clientePersistido);
        contenedorPersistido.setEstado(ContenedorEstado.BORRADOR);
        contenedorPersistido.setPeso(new BigDecimal("2000"));
        contenedorPersistido.setVolumen(new BigDecimal("30"));
        contenedorPersistido.setCodigo("CONT-0001");

        when(clienteRepository.findByCuit("30-12345678-9")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clientePersistido);
        when(contenedorRepository.findFirstByCodigoAndEstadoNotIn(eq("CONT-0001"), anyCollection()))
            .thenReturn(Optional.empty());
        when(contenedorRepository.save(any(Contenedor.class))).thenReturn(contenedorPersistido);
        when(solicitudRepository.findByContenedorId(eq(2L))).thenReturn(Optional.empty());
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocation -> {
            Solicitud solicitud = invocation.getArgument(0);
            solicitud.setId(5L);
            return solicitud;
        });

        SolicitudResponseDto response = solicitudService.crearSolicitud(request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getCliente().getId()).isEqualTo(1L);
        assertThat(response.getContenedor().getId()).isEqualTo(2L);
        assertThat(response.getContenedor().getEstado()).isEqualTo(ContenedorEstado.BORRADOR);
        assertThat(response.getOrigen()).isEqualTo("Buenos Aires");
        assertThat(response.getDestino()).isEqualTo("Córdoba");
    }

}
