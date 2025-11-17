package com.tpibackend.orders.service.impl;

import com.tpibackend.orders.client.FleetMetricsClient;
import com.tpibackend.orders.client.LogisticsClient;
import com.tpibackend.orders.dto.response.DistanceEstimationResponse;
import com.tpibackend.orders.dto.request.EstimacionRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.request.UbicacionRequestDto;
import com.tpibackend.orders.dto.request.SolicitudCostoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudPlanificacionUpdateRequest;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.dto.response.RutaResumenDto;
import com.tpibackend.orders.exception.OrdersNotFoundException;
import com.tpibackend.orders.exception.OrdersValidationException;
import com.tpibackend.orders.mapper.SolicitudMapper;
import com.tpibackend.orders.model.Cliente;
import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import com.tpibackend.orders.repository.ClienteRepository;
import com.tpibackend.orders.repository.ContenedorRepository;
import com.tpibackend.orders.repository.SolicitudRepository;
import com.tpibackend.orders.service.EstadoService;
import com.tpibackend.orders.service.SolicitudService;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Optional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudServiceImpl.class);
    private static final EnumSet<ContenedorEstado> CONTENEDOR_ESTADOS_CERRADOS = EnumSet.of(
            ContenedorEstado.ENTREGADO,
            ContenedorEstado.CANCELADO
    );

    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final SolicitudRepository solicitudRepository;
    private final SolicitudMapper solicitudMapper;
    private final FleetMetricsClient fleetMetricsClient;
    private final LogisticsClient logisticsClient;
    private final EstadoService estadoService;

    public SolicitudServiceImpl(
        ClienteRepository clienteRepository,
        ContenedorRepository contenedorRepository,
        SolicitudRepository solicitudRepository,
        SolicitudMapper solicitudMapper,
        FleetMetricsClient fleetMetricsClient,
        LogisticsClient logisticsClient,
        EstadoService estadoService
    ) {
        this.clienteRepository = clienteRepository;
        this.contenedorRepository = contenedorRepository;
        this.solicitudRepository = solicitudRepository;
        this.solicitudMapper = solicitudMapper;
        this.fleetMetricsClient = fleetMetricsClient;
        this.logisticsClient = logisticsClient;
        this.estadoService = estadoService;
    }

    @Override
    @Transactional
    public SolicitudResponseDto crearSolicitud(SolicitudCreateRequest request) {
        Cliente cliente = resolverCliente(request);
        validarClienteAutenticado(cliente);
        Contenedor contenedor = resolverContenedorPorNegocio(request, cliente);

        solicitudRepository.findByContenedorId(contenedor.getId())
            .filter(this::esSolicitudActiva)
            .ifPresent(existing -> {
                throw new OrdersValidationException("El contenedor ya posee una solicitud activa");
            });

        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setContenedor(contenedor);
        UbicacionRequestDto origen = request.getOrigen();
        UbicacionRequestDto destino = request.getDestino();
        solicitud.setOrigen(origen.getDireccion());
        solicitud.setOrigenLat(origen.getLatitud());
        solicitud.setOrigenLng(origen.getLongitud());
        solicitud.setDestino(destino.getDireccion());
        solicitud.setDestinoLat(destino.getLatitud());
        solicitud.setDestinoLng(destino.getLongitud());
        solicitud.setObservaciones(request.getObservaciones());
        solicitud.setFechaCreacion(OffsetDateTime.now());

        // Inicializar estados automáticamente
        String usuario = obtenerUsuarioActual();
        estadoService.inicializarEstados(contenedor, solicitud, usuario);

        Solicitud guardada = solicitudRepository.save(solicitud);
        contenedor.setSolicitudActiva(guardada);
        log.info("Solicitud {} creada para el contenedor {}", guardada.getId(), contenedor.getId());
        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponseDto obtenerSolicitud(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new OrdersNotFoundException("Solicitud no encontrada"));
        verificarAccesoASolicitud(solicitud);
        return mapToResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SeguimientoResponseDto obtenerSeguimientoPorContenedor(Long contenedorId) {
        Solicitud solicitud = solicitudRepository.findByContenedorId(contenedorId)
            .orElseThrow(() -> new OrdersNotFoundException("No existe solicitud asociada al contenedor"));
        verificarAccesoASolicitud(solicitud);
        ContenedorEstado estadoContenedor = Optional.ofNullable(solicitud.getContenedor())
            .map(Contenedor::getEstado)
            .orElse(null);
        Optional<RutaResumenDto> ruta = logisticsClient.obtenerRutaPorSolicitud(solicitud.getId());
        // TODO: ampliar seguimiento con informacion de tramos/logistica cuando se definan los eventos compartidos.

        return SeguimientoResponseDto.builder()
            .contenedorId(contenedorId)
            .solicitudId(solicitud.getId())
            .estadoContenedor(estadoContenedor)
            .ruta(ruta.orElse(null))
            .build();
    }

    @Override
    @Transactional
    public SolicitudResponseDto calcularEstimacion(Long solicitudId, EstimacionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new OrdersNotFoundException("Solicitud no encontrada"));

        String origen = request.getOrigen();
        String destino = request.getDestino();
        if (isAnyBlank(origen, destino)) {
            throw new OrdersValidationException("Es necesario indicar origen y destino para calcular la estimación");
        }

        solicitud.setEstadiaEstimada(request.getEstadiaEstimada());

        DistanceEstimationResponse distanceData = logisticsClient.estimarDistancia(origen, destino)
            .orElseThrow(() -> new OrdersValidationException(
                    "No fue posible calcular la distancia estimada con Logistics"));
        var fleetResponse = fleetMetricsClient.getFleetAverages();

        BigDecimal kilometros = BigDecimal.valueOf(distanceData.distanciaKm()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoKilometro = nvl(fleetResponse.costoKilometroPromedio());
        BigDecimal consumoPromedio = nvl(fleetResponse.consumoPromedio());
        BigDecimal precioCombustible = nvl(request.getPrecioCombustible());
        BigDecimal estadiaEstim = nvl(request.getEstadiaEstimada());

        BigDecimal costoEstimado = kilometros.multiply(costoKilometro)
            .add(kilometros.multiply(consumoPromedio).multiply(precioCombustible))
            .add(estadiaEstim)
            .setScale(2, RoundingMode.HALF_UP);

        solicitud.setCostoEstimado(costoEstimado);
        solicitud.setTiempoEstimadoMinutos(Math.round(distanceData.duracionMinutos()));

        Solicitud actualizada = solicitudRepository.save(solicitud);
        log.info("Estimación actualizada para la solicitud {}", solicitudId);
        return mapToResponse(actualizada);
    }

    @Override
    @Transactional
    public SolicitudResponseDto actualizarCosto(Long solicitudId, SolicitudCostoUpdateRequest request) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new OrdersNotFoundException("Solicitud no encontrada"));

        solicitud.setCostoFinal(request.costoFinal());
        if (request.tiempoRealMinutos() != null) {
            solicitud.setTiempoRealMinutos(request.tiempoRealMinutos());
        }

        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud {} actualizada con costo final {}", solicitudId, request.costoFinal());
        return mapToResponse(guardada);
    }

    @Override
    @Transactional
    public SolicitudResponseDto actualizarPlanificacion(Long solicitudId, SolicitudPlanificacionUpdateRequest request) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new OrdersNotFoundException("Solicitud no encontrada"));

        solicitud.setCostoEstimado(request.costoEstimado());
        solicitud.setTiempoEstimadoMinutos(request.tiempoEstimadoMinutos());
        solicitud.setRutaLogisticaId(request.rutaLogisticaId());

        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud {} actualizada con planificación logística {}", solicitudId, request.rutaLogisticaId());
        return mapToResponse(guardada);
    }

    private Cliente resolverCliente(SolicitudCreateRequest request) {
        var clienteRequest = request.getCliente();
        if (clienteRequest.getId() != null) {
            return clienteRepository.findById(clienteRequest.getId())
                .orElseThrow(() -> new OrdersNotFoundException("Cliente no encontrado"));
        }
        validarNuevoCliente(request);

        String cuit = clienteRequest.getCuit().trim();
        String email = clienteRequest.getEmail().trim();

        Optional<Cliente> clienteExistente = clienteRepository.findByCuit(cuit);
        if (clienteExistente.isEmpty()) {
            clienteExistente = clienteRepository.findByEmail(email);
        }
        if (clienteExistente.isPresent()) {
            return clienteExistente.get();
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(clienteRequest.getNombre());
        cliente.setEmail(email);
        cliente.setTelefono(clienteRequest.getTelefono());
        cliente.setCuit(cuit);
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
        // No seteamos el estado aquí, lo hará EstadoService
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
        if (!StringUtils.hasText(request.getCliente().getCuit())) {
            throw new OrdersValidationException("El CUIT del cliente es obligatorio");
        }
    }

    private SolicitudResponseDto mapToResponse(Solicitud solicitud) {
        SolicitudResponseDto base = solicitudMapper.toDto(solicitud);
        Optional<RutaResumenDto> ruta = logisticsClient.obtenerRutaPorSolicitud(solicitud.getId());
        return ruta.map(resumen -> base.toBuilder().rutaResumen(resumen).build()).orElse(base);
    }

    private boolean esSolicitudActiva(Solicitud solicitud) {
        Contenedor contenedor = solicitud.getContenedor();
        ContenedorEstado estado = contenedor != null ? contenedor.getEstado() : null;
        return estado == null || !CONTENEDOR_ESTADOS_CERRADOS.contains(estado);
    }

    private void validarNuevoContenedor(SolicitudCreateRequest request) {
        if (!StringUtils.hasText(request.getContenedor().getCodigo())) {
            throw new OrdersValidationException("El código del contenedor es obligatorio");
        }
        if (request.getContenedor().getPeso() == null || request.getContenedor().getPeso().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrdersValidationException("El peso del contenedor debe ser mayor a 0");
        }
        if (request.getContenedor().getVolumen() == null || request.getContenedor().getVolumen().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrdersValidationException("El volumen del contenedor debe ser mayor a 0");
        }
        // El estado se gestiona automáticamente, no es necesario validarlo
    }

    private void validarCodigoContenedorDisponible(String codigo) {
        contenedorRepository.findFirstByCodigoAndEstadoNotIn(codigo, CONTENEDOR_ESTADOS_CERRADOS)
            .ifPresent(existing -> {
                throw new OrdersValidationException(
                    "El código de contenedor ya está siendo utilizado por otro contenedor activo");
            });
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

    private Contenedor resolverContenedorPorNegocio(SolicitudCreateRequest request, Cliente cliente) {
        var contenedorRequest = request.getContenedor();

        if (contenedorRequest.getId() != null) {
            return resolverContenedor(request, cliente);
        }

        validarNuevoContenedor(request);
        String codigo = contenedorRequest.getCodigo().trim();
        validarCodigoContenedorDisponible(codigo);
        Contenedor contenedor = new Contenedor();
        contenedor.setCliente(cliente);
        contenedor.setCodigo(codigo);
        // No seteamos el estado aqui, lo hara EstadoService
        contenedor.setPeso(contenedorRequest.getPeso());
        contenedor.setVolumen(contenedorRequest.getVolumen());
        return contenedorRepository.save(contenedor);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String preferred = jwtAuth.getToken().getClaimAsString("preferred_username");
                if (StringUtils.hasText(preferred)) {
                    return preferred;
                }
                String email = jwtAuth.getToken().getClaimAsString("email");
                if (StringUtils.hasText(email)) {
                    return email;
                }
            }
            return authentication.getName();
        }
        return "system";
    }

    private void validarClienteAutenticado(Cliente cliente) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!requiereValidacionCliente(authentication, cliente)) {
            return;
        }
        String emailUsuario = obtenerEmailDesdeToken(authentication);
        if (emailUsuario == null || cliente.getEmail() == null
                || !cliente.getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new AccessDeniedException("Los clientes solo pueden operar con su propio perfil");
        }
    }

    private void verificarAccesoASolicitud(Solicitud solicitud) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || solicitud == null) {
            return;
        }
        if (tieneRol(authentication, "OPERADOR") || tieneRol(authentication, "ADMIN")) {
            return;
        }
        if (tieneRol(authentication, "CLIENTE")) {
            Cliente cliente = solicitud.getCliente();
            String emailUsuario = obtenerEmailDesdeToken(authentication);
            if (cliente != null && emailUsuario != null
                    && emailUsuario.equalsIgnoreCase(cliente.getEmail())) {
                return;
            }
            throw new AccessDeniedException("El cliente autenticado no puede acceder a esta solicitud");
        }
    }

    private boolean requiereValidacionCliente(Authentication authentication, Cliente cliente) {
        return authentication != null && authentication.isAuthenticated()
                && cliente != null && tieneRol(authentication, "CLIENTE")
                && !tieneRol(authentication, "ADMIN");
    }

    private boolean tieneRol(Authentication authentication, String roleName) {
        if (authentication == null || roleName == null) {
            return false;
        }
        String expected = "ROLE_" + roleName.toUpperCase();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> expected.equals(authority.getAuthority()));
    }

    private String obtenerEmailDesdeToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String email = jwtAuth.getToken().getClaimAsString("email");
            if (StringUtils.hasText(email)) {
                return email;
            }
            String preferred = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (StringUtils.hasText(preferred)) {
                return preferred;
            }
        }
        return null;
    }
}
