package com.tpibackend.logistics.service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tpibackend.logistics.client.OrdersClient;
import com.tpibackend.logistics.dto.integration.SolicitudLogisticaResponse;
import com.tpibackend.logistics.dto.response.ContenedorPendienteResponse;
import com.tpibackend.logistics.dto.response.SeguimientoContenedorResponse;
import com.tpibackend.logistics.dto.response.SeguimientoUbicacion;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.exception.BusinessException;
import com.tpibackend.logistics.exception.NotFoundException;
import com.tpibackend.logistics.mapper.LogisticsMapper;
import com.tpibackend.logistics.model.Ruta;
import com.tpibackend.logistics.model.Tramo;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.repository.RutaRepository;
import com.tpibackend.logistics.repository.TramoRepository;

@Service
@Transactional(readOnly = true)
public class SeguimientoService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final OrdersClient ordersClient;

    public SeguimientoService(RutaRepository rutaRepository, TramoRepository tramoRepository,
            OrdersClient ordersClient) {
        this.rutaRepository = rutaRepository;
        this.tramoRepository = tramoRepository;
        this.ordersClient = ordersClient;
    }

    public List<ContenedorPendienteResponse> obtenerContenedoresPendientes(Long solicitudId, Long depositoId,
            TramoEstado estadoTramo) {
        Stream<Ruta> rutaStream = solicitudId != null
                ? rutaRepository.findBySolicitudId(solicitudId).stream()
                : rutaRepository.findAll().stream();

        return rutaStream
                .map(ruta -> buildPendiente(ruta, depositoId, estadoTramo))
                .flatMap(Optional::stream)
                .toList();
    }

    public SeguimientoContenedorResponse obtenerEstadoContenedor(Long contenedorId, Long solicitudId) {
        Ruta ruta = rutaRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró ruta logística asociada a la solicitud " + solicitudId));

        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByIdAsc(ruta.getId());
        if (tramos.isEmpty()) {
            throw new NotFoundException("La ruta logística " + ruta.getId() + " no tiene tramos configurados");
        }

        Tramo tramoActual = tramos.stream()
                .filter(t -> t.getEstado() != TramoEstado.FINALIZADO)
                .findFirst()
                .orElse(tramos.get(tramos.size() - 1));

        SolicitudLogisticaResponse solicitud = ruta.getSolicitudId() != null
                ? ordersClient.obtenerSolicitud(ruta.getSolicitudId()).orElse(null)
                : null;
        validarAccesoCliente(solicitud);

        Long contenedorDesdeOrders = solicitud != null && solicitud.contenedor() != null
                ? solicitud.contenedor().id()
                : null;
        if (contenedorDesdeOrders != null && contenedorId != null
                && !Objects.equals(contenedorId, contenedorDesdeOrders)) {
            throw new BusinessException("El contenedor solicitado no pertenece a la solicitud " + solicitudId);
        }

        List<TramoResponse> tramoResponses = tramos.stream()
                .map(LogisticsMapper::toTramoResponse)
                .toList();

        String estadoSolicitud = solicitud != null ? solicitud.estadoSolicitud() : null;
        String contenedorCodigo = solicitud != null && solicitud.contenedor() != null
                ? solicitud.contenedor().codigo()
                : null;
        String estadoLogistico = derivarEstadoLogistico(tramos);
        SeguimientoUbicacion ubicacion = buildUbicacion(tramoActual);
        OffsetDateTime ultimoMovimiento = resolverUltimoMovimiento(tramos);

        return new SeguimientoContenedorResponse(
                ruta.getSolicitudId(),
                contenedorDesdeOrders != null ? contenedorDesdeOrders : contenedorId,
                contenedorCodigo,
                estadoSolicitud,
                estadoLogistico,
                ubicacion,
                LogisticsMapper.toTramoResponse(tramoActual),
                tramoResponses,
                ultimoMovimiento);
    }

    private Optional<ContenedorPendienteResponse> buildPendiente(Ruta ruta, Long depositoId, TramoEstado estadoTramo) {
        if (ruta.getSolicitudId() == null || ruta.getTramos() == null || ruta.getTramos().isEmpty()) {
            return Optional.empty();
        }
        List<Tramo> tramos = ruta.getTramos().stream()
                .sorted(Comparator.comparing(Tramo::getId))
                .toList();
        Optional<Tramo> tramoActualOpt = tramos.stream()
                .filter(t -> t.getEstado() != TramoEstado.FINALIZADO)
                .findFirst();
        if (tramoActualOpt.isEmpty()) {
            return Optional.empty();
        }
        Tramo tramoActual = tramoActualOpt.get();
        if (estadoTramo != null && tramoActual.getEstado() != estadoTramo) {
            return Optional.empty();
        }
        if (depositoId != null && !cumpleFiltroDeposito(tramoActual, depositoId)) {
            return Optional.empty();
        }

        SolicitudLogisticaResponse solicitud = ruta.getSolicitudId() != null
                ? ordersClient.obtenerSolicitud(ruta.getSolicitudId()).orElse(null)
                : null;
        Long contenedorId = solicitud != null && solicitud.contenedor() != null
                ? solicitud.contenedor().id()
                : null;
        String contenedorCodigo = solicitud != null && solicitud.contenedor() != null
                ? solicitud.contenedor().codigo()
                : null;
        String estadoSolicitud = solicitud != null ? solicitud.estadoSolicitud() : null;

        return Optional.of(new ContenedorPendienteResponse(
                ruta.getSolicitudId(),
                contenedorId,
                contenedorCodigo,
                estadoSolicitud,
                ruta.getId(),
                tramoActual.getId(),
                derivarEstadoLogistico(tramos),
                tramoActual.getEstado(),
                buildUbicacion(tramoActual),
                resolverUltimoMovimiento(tramos)));
    }

    private boolean cumpleFiltroDeposito(Tramo tramoActual, Long depositoId) {
        if (tramoActual == null || depositoId == null) {
            return true;
        }
        if ((tramoActual.getEstado() == TramoEstado.ESTIMADO || tramoActual.getEstado() == TramoEstado.ASIGNADO)
                && tramoActual.getOrigenTipo() == LocationType.DEPOSITO
                && depositoId.equals(tramoActual.getOrigenId())) {
            return true;
        }
        if (tramoActual.getDestinoTipo() == LocationType.DEPOSITO
                && depositoId.equals(tramoActual.getDestinoId())) {
            return true;
        }
        return false;
    }

    private String derivarEstadoLogistico(List<Tramo> tramos) {
        if (tramos.stream().allMatch(t -> t.getEstado() == TramoEstado.FINALIZADO)) {
            return "ENTREGADO";
        }
        if (tramos.stream().anyMatch(t -> t.getEstado() == TramoEstado.INICIADO)) {
            return "EN_TRANSITO";
        }
        if (tramos.stream().anyMatch(t -> t.getEstado() == TramoEstado.ASIGNADO)) {
            return "ASIGNADO";
        }
        return "PROGRAMADO";
    }

    private SeguimientoUbicacion buildUbicacion(Tramo tramo) {
        if (tramo == null) {
            return null;
        }
        LocationType tipo;
        Long referencia;
        Double lat;
        Double lng;
        String descripcion;
        if (tramo.getEstado() == TramoEstado.INICIADO) {
            tipo = LocationType.PUNTO_INTERMEDIO;
            referencia = tramo.getCamionId();
            lat = tramo.getDestinoLat();
            lng = tramo.getDestinoLng();
            descripcion = "En tránsito hacia " + tramo.getDestinoTipo();
        } else if (tramo.getEstado() == TramoEstado.ESTIMADO || tramo.getEstado() == TramoEstado.ASIGNADO) {
            tipo = tramo.getOrigenTipo();
            referencia = tramo.getOrigenId();
            lat = tramo.getOrigenLat();
            lng = tramo.getOrigenLng();
            descripcion = "Listo para salir desde " + tramo.getOrigenTipo();
        } else {
            tipo = tramo.getDestinoTipo();
            referencia = tramo.getDestinoId();
            lat = tramo.getDestinoLat();
            lng = tramo.getDestinoLng();
            descripcion = "Arribado a " + tramo.getDestinoTipo();
        }
        return new SeguimientoUbicacion(tipo, referencia, lat, lng, descripcion);
    }

    private OffsetDateTime resolverUltimoMovimiento(List<Tramo> tramos) {
        return tramos.stream()
                .map(tramo -> tramo.getFechaHoraFin() != null ? tramo.getFechaHoraFin() : tramo.getFechaHoraInicio())
                .filter(Objects::nonNull)
                .max(OffsetDateTime::compareTo)
                .orElse(null);
    }

    private void validarAccesoCliente(SolicitudLogisticaResponse solicitud) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        if (tieneRol(authentication, "OPERADOR")) {
            return;
        }
        if (tieneRol(authentication, "CLIENTE")) {
            String clienteId = obtenerClienteIdDesdeToken(authentication);
            String clienteSolicitud = solicitud != null ? solicitud.clienteIdentificador() : null;
            if (StringUtils.hasText(clienteId) && StringUtils.hasText(clienteSolicitud)
                    && clienteSolicitud.equalsIgnoreCase(clienteId)) {
                return;
            }
            throw new AccessDeniedException("El cliente autenticado no puede acceder al seguimiento solicitado");
        }
    }

    private boolean tieneRol(Authentication authentication, String roleName) {
        if (authentication == null || !StringUtils.hasText(roleName)) {
            return false;
        }
        String expected = "ROLE_" + roleName.toUpperCase();
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> expected.equals(granted.getAuthority()));
    }

    private String obtenerClienteIdDesdeToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String clienteId = jwtAuth.getToken().getClaimAsString("clienteId");
            if (StringUtils.hasText(clienteId)) {
                return clienteId;
            }
        }
        return null;
    }
}
