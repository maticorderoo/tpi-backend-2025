package com.tpibackend.orders.service.impl;

import com.tpibackend.orders.client.DistanceClient;
import com.tpibackend.orders.client.FleetMetricsClient;
import com.tpibackend.orders.dto.request.EstimacionRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudEventoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.exception.OrdersNotFoundException;
import com.tpibackend.orders.exception.OrdersValidationException;
import com.tpibackend.orders.mapper.SolicitudMapper;
import com.tpibackend.orders.model.Cliente;
import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.SolicitudEstado;
import com.tpibackend.orders.model.history.SolicitudEvento;
import com.tpibackend.orders.repository.ClienteRepository;
import com.tpibackend.orders.repository.ContenedorRepository;
import com.tpibackend.orders.repository.SolicitudRepository;
import com.tpibackend.orders.service.SolicitudService;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudServiceImpl.class);
    private static final EnumSet<SolicitudEstado> ESTADOS_ACTIVOS = EnumSet.of(
        SolicitudEstado.BORRADOR, SolicitudEstado.PROGRAMADA, SolicitudEstado.EN_TRANSITO
    );

    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final SolicitudRepository solicitudRepository;
    private final SolicitudMapper solicitudMapper;
    private final FleetMetricsClient fleetMetricsClient;
    private final DistanceClient distanceClient;

    public SolicitudServiceImpl(
        ClienteRepository clienteRepository,
        ContenedorRepository contenedorRepository,
        SolicitudRepository solicitudRepository,
        SolicitudMapper solicitudMapper,
        FleetMetricsClient fleetMetricsClient,
        DistanceClient distanceClient
    ) {
        this.clienteRepository = clienteRepository;
        this.contenedorRepository = contenedorRepository;
        this.solicitudRepository = solicitudRepository;
        this.solicitudMapper = solicitudMapper;
        this.fleetMetricsClient = fleetMetricsClient;
        this.distanceClient = distanceClient;
    }

    @Override
    @Transactional
    public SolicitudResponseDto crearSolicitud(SolicitudCreateRequest request) {
        Cliente cliente = resolverCliente(request);
        Contenedor contenedor = resolverContenedor(request, cliente);

        if (solicitudRepository.existsByContenedorIdAndEstadoIn(contenedor.getId(), ESTADOS_ACTIVOS)) {
            throw new OrdersValidationException("El contenedor ya posee una solicitud activa");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setContenedor(contenedor);
        solicitud.setEstado(SolicitudEstado.BORRADOR);
        solicitud.setEstadiaEstimada(request.getEstadiaEstimada());
        solicitud.setOrigen(request.getOrigen());
        solicitud.setDestino(request.getDestino());
        solicitud.setFechaCreacion(OffsetDateTime.now());

        SolicitudEvento evento = new SolicitudEvento();
        evento.setEstado(SolicitudEstado.BORRADOR);
        evento.setFechaEvento(OffsetDateTime.now());
        evento.setDescripcion("Solicitud creada en estado BORRADOR");
        solicitud.agregarEvento(evento);

        Solicitud guardada = solicitudRepository.save(solicitud);
        contenedor.setSolicitudActiva(guardada);
        log.info("Solicitud {} creada para el contenedor {}", guardada.getId(), contenedor.getId());
        return solicitudMapper.toDto(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponseDto obtenerSolicitud(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new OrdersNotFoundException("Solicitud no encontrada"));
        return solicitudMapper.toDto(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SeguimientoResponseDto obtenerSeguimientoPorContenedor(Long contenedorId) {
        Solicitud solicitud = solicitudRepository.findByContenedorId(contenedorId)
            .orElseThrow(() -> new OrdersNotFoundException("No existe solicitud asociada al contenedor"));
        List<SolicitudEventoResponseDto> eventos = solicitudMapper.toDto(solicitud).getEventos();
        return SeguimientoResponseDto.builder()
            .contenedorId(contenedorId)
            .solicitudId(solicitud.getId())
            .estadoActual(solicitud.getEstado())
            .eventos(eventos)
            .build();
    }

    @Override
    @Transactional
    public SolicitudResponseDto calcularEstimacion(Long solicitudId, EstimacionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new OrdersNotFoundException("Solicitud no encontrada"));

        String origen = seleccionarValorNoVacio(request.getOrigen(), solicitud.getOrigen());
        String destino = seleccionarValorNoVacio(request.getDestino(), solicitud.getDestino());
        if (isAnyBlank(origen, destino)) {
            throw new OrdersValidationException("Es necesario indicar origen y destino para calcular la estimación");
        }

        solicitud.setOrigen(origen);
        solicitud.setDestino(destino);
        solicitud.setEstadiaEstimada(request.getEstadiaEstimada());

        var distanceResponse = distanceClient.calculateDistance(origen, destino);
        var fleetResponse = fleetMetricsClient.getFleetAverages();

        BigDecimal kilometros = distanceResponse.distanciaKilometros();
        BigDecimal costoKilometro = nvl(fleetResponse.costoKilometroPromedio());
        BigDecimal consumoPromedio = nvl(fleetResponse.consumoPromedio());
        BigDecimal precioCombustible = nvl(request.getPrecioCombustible());
        BigDecimal estadiaEstim = nvl(request.getEstadiaEstimada());

        BigDecimal costoEstimado = kilometros.multiply(costoKilometro)
            .add(kilometros.multiply(consumoPromedio).multiply(precioCombustible))
            .add(estadiaEstim)
            .setScale(2, RoundingMode.HALF_UP);

        solicitud.setCostoEstimado(costoEstimado);
        solicitud.setTiempoEstimadoMinutos(distanceResponse.duracionMinutos());

        SolicitudEvento evento = new SolicitudEvento();
        evento.setEstado(solicitud.getEstado());
        evento.setFechaEvento(OffsetDateTime.now());
        evento.setDescripcion("Estimación calculada para la solicitud");
        solicitud.agregarEvento(evento);

        Solicitud actualizada = solicitudRepository.save(solicitud);
        log.info("Estimación actualizada para la solicitud {}", solicitudId);
        return solicitudMapper.toDto(actualizada);
    }

    private Cliente resolverCliente(SolicitudCreateRequest request) {
        if (request.getCliente().getId() != null) {
            return clienteRepository.findById(request.getCliente().getId())
                .orElseThrow(() -> new OrdersNotFoundException("Cliente no encontrado"));
        }
        validarNuevoCliente(request);
        Cliente cliente = new Cliente();
        cliente.setNombre(request.getCliente().getNombre());
        cliente.setEmail(request.getCliente().getEmail());
        cliente.setTelefono(request.getCliente().getTelefono());
        return clienteRepository.save(cliente);
    }

    private Contenedor resolverContenedor(SolicitudCreateRequest request, Cliente cliente) {
        if (request.getContenedor().getId() != null) {
            Contenedor contenedor = contenedorRepository.findById(request.getContenedor().getId())
                .orElseThrow(() -> new OrdersNotFoundException("Contenedor no encontrado"));
            if (!contenedor.getCliente().getId().equals(cliente.getId())) {
                throw new OrdersValidationException("El contenedor no pertenece al cliente indicado");
            }
            return contenedor;
        }
        validarNuevoContenedor(request);
        Contenedor contenedor = new Contenedor();
        contenedor.setCliente(cliente);
        contenedor.setEstado(request.getContenedor().getEstado());
        contenedor.setPeso(request.getContenedor().getPeso());
        contenedor.setVolumen(request.getContenedor().getVolumen());
        return contenedorRepository.save(contenedor);
    }

    private void validarNuevoCliente(SolicitudCreateRequest request) {
        if (!StringUtils.hasText(request.getCliente().getNombre())) {
            throw new OrdersValidationException("El nombre del cliente es obligatorio");
        }
        if (!StringUtils.hasText(request.getCliente().getEmail())) {
            throw new OrdersValidationException("El email del cliente es obligatorio");
        }
    }

    private void validarNuevoContenedor(SolicitudCreateRequest request) {
        if (request.getContenedor().getPeso() == null || request.getContenedor().getPeso().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrdersValidationException("El peso del contenedor debe ser mayor a 0");
        }
        if (request.getContenedor().getVolumen() == null || request.getContenedor().getVolumen().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrdersValidationException("El volumen del contenedor debe ser mayor a 0");
        }
        if (!StringUtils.hasText(request.getContenedor().getEstado())) {
            throw new OrdersValidationException("El estado del contenedor es obligatorio");
        }
    }

    private boolean isAnyBlank(String... values) {
        if (values == null) {
            return true;
        }
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                return true;
            }
        }
        return false;
    }

    private String seleccionarValorNoVacio(String preferido, String alternativo) {
        return StringUtils.hasText(preferido) ? preferido : alternativo;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
