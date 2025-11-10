package com.tpibackend.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.tpibackend.distance.DistanceClient;
import com.tpibackend.distance.model.DistanceData;
import com.tpibackend.orders.client.FleetMetricsClient;
import com.tpibackend.orders.client.LogisticsClient;
import com.tpibackend.orders.dto.request.EstimacionRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.request.ClienteRequestDto;
import com.tpibackend.orders.dto.request.ContenedorRequestDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.exception.OrdersValidationException;
import com.tpibackend.orders.mapper.SolicitudMapper;
import com.tpibackend.orders.model.Cliente;
import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import com.tpibackend.orders.model.enums.SolicitudEstado;
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
    private FleetMetricsClient fleetMetricsClient;

    @Mock
    private DistanceClient distanceClient;

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
            fleetMetricsClient,
            distanceClient,
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
        request.setCliente(clienteDto);

        ContenedorRequestDto contenedorDto = new ContenedorRequestDto();
        // El estado no se setea, se gestiona automáticamente
        contenedorDto.setPeso(new BigDecimal("2000"));
        contenedorDto.setVolumen(new BigDecimal("30"));
        request.setContenedor(contenedorDto);

        Cliente clientePersistido = new Cliente();
        clientePersistido.setId(1L);
        clientePersistido.setNombre("Juan Perez");
        clientePersistido.setEmail("juan@test.com");

        Contenedor contenedorPersistido = new Contenedor();
        contenedorPersistido.setId(2L);
        contenedorPersistido.setCliente(clientePersistido);
        contenedorPersistido.setEstado(ContenedorEstado.BORRADOR);
        contenedorPersistido.setPeso(new BigDecimal("2000"));
        contenedorPersistido.setVolumen(new BigDecimal("30"));

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clientePersistido);
        when(contenedorRepository.save(any(Contenedor.class))).thenReturn(contenedorPersistido);
        when(solicitudRepository.existsByContenedorIdAndEstadoIn(eq(2L), anyCollection())).thenReturn(false);
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocation -> {
            Solicitud solicitud = invocation.getArgument(0);
            solicitud.setId(5L);
            return solicitud;
        });

        SolicitudResponseDto response = solicitudService.crearSolicitud(request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getEstado()).isEqualTo(SolicitudEstado.BORRADOR);
        assertThat(response.getCliente().getId()).isEqualTo(1L);
        assertThat(response.getContenedor().getId()).isEqualTo(2L);
        assertThat(response.getEventos()).hasSize(1);
    }

    @Test
    void calcularEstimacion_debeActualizarCostoYTiempo() {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(10L);
        solicitud.setEstado(SolicitudEstado.BORRADOR);
        solicitud.setOrigen("Buenos Aires");
        solicitud.setDestino("Rosario");
        solicitud.setCliente(new Cliente());
        solicitud.setContenedor(new Contenedor());

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(distanceClient.getDistance("Buenos Aires", "Rosario"))
            .thenReturn(new DistanceData(300.0, 360.0));
        when(fleetMetricsClient.getFleetAverages())
            .thenReturn(new FleetMetricsClient.FleetAveragesResponse(new BigDecimal("15"), new BigDecimal("0.3")));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EstimacionRequest request = new EstimacionRequest();
        request.setPrecioCombustible(new BigDecimal("2"));
        request.setEstadiaEstimada(new BigDecimal("100"));

        SolicitudResponseDto response = solicitudService.calcularEstimacion(10L, request);

        BigDecimal costoEsperado = new BigDecimal("300").multiply(new BigDecimal("15"))
            .add(new BigDecimal("300").multiply(new BigDecimal("0.3")).multiply(new BigDecimal("2")))
            .add(new BigDecimal("100"));
        assertThat(response.getCostoEstimado()).isEqualByComparingTo(costoEsperado.setScale(2));
        assertThat(response.getTiempoEstimadoMinutos()).isEqualTo(360L);
        assertThat(response.getEventos()).isNotEmpty();
    }

    @Test
    void calcularEstimacion_sinOrigenODestino_debeFallar() {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(10L);
        solicitud.setEstado(SolicitudEstado.BORRADOR);
        solicitud.setCliente(new Cliente());
        solicitud.setContenedor(new Contenedor());
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));

        EstimacionRequest request = new EstimacionRequest();
        request.setPrecioCombustible(new BigDecimal("2"));
        request.setEstadiaEstimada(new BigDecimal("100"));

        assertThatThrownBy(() -> solicitudService.calcularEstimacion(10L, request))
            .isInstanceOf(OrdersValidationException.class)
            .hasMessageContaining("origen y destino");
    }
}
