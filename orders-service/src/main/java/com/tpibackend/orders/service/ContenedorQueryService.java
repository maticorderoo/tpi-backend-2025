package com.tpibackend.orders.service;

import com.tpibackend.orders.client.LogisticsClient;
import com.tpibackend.orders.dto.response.DepositoResumenDto;
import com.tpibackend.orders.dto.response.PendingContainerResponseDto;
import com.tpibackend.orders.dto.response.TramoResumenDto;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import com.tpibackend.orders.repository.SolicitudRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ContenedorQueryService {

    private static final Set<String> ESTADOS_PENDIENTES = Set.of("ESTIMADO", "ASIGNADO", "INICIADO");

    private final SolicitudRepository solicitudRepository;
    private final LogisticsClient logisticsClient;

    public ContenedorQueryService(SolicitudRepository solicitudRepository, LogisticsClient logisticsClient) {
        this.solicitudRepository = solicitudRepository;
        this.logisticsClient = logisticsClient;
    }

    public List<PendingContainerResponseDto> obtenerContenedoresPendientes(String estadoFiltro, Long depositoId) {
        List<Solicitud> solicitudes = solicitudRepository.findAll();
        return solicitudes.stream()
                .filter(solicitud -> solicitud.getContenedor() != null)
                .flatMap(solicitud -> buildResponsesForSolicitud(solicitud, estadoFiltro, depositoId))
                .toList();
    }

    private Stream<PendingContainerResponseDto> buildResponsesForSolicitud(Solicitud solicitud,
            String estadoFiltro, Long depositoId) {
        return logisticsClient.obtenerRutaPorSolicitud(solicitud.getId())
                .stream()
                .flatMap(ruta -> ruta.getTramos() == null ? Stream.empty() : ruta.getTramos().stream()
                        .filter(tramo -> matchesEstado(tramo, estadoFiltro))
                        .filter(tramo -> matchesDeposito(tramo, depositoId))
                        .map(tramo -> mapToResponse(solicitud, ruta.getId(), tramo)));
    }

    private boolean matchesEstado(TramoResumenDto tramo, String estadoFiltro) {
        String estadoTramo = normalize(tramo.getEstado());
        if (!StringUtils.hasText(estadoFiltro)) {
            return estadoTramo != null && ESTADOS_PENDIENTES.contains(estadoTramo);
        }
        String filtro = normalize(estadoFiltro);
        return filtro != null && filtro.equals(estadoTramo);
    }

    private boolean matchesDeposito(TramoResumenDto tramo, Long depositoId) {
        if (depositoId == null) {
            return true;
        }
        if (!"DEPOSITO".equalsIgnoreCase(tramo.getDestinoTipo())) {
            return false;
        }
        return depositoId.equals(tramo.getDestinoId());
    }

    private PendingContainerResponseDto mapToResponse(Solicitud solicitud, Long rutaId, TramoResumenDto tramo) {
        DepositoResumenDto deposito = null;
        if ("DEPOSITO".equalsIgnoreCase(tramo.getDestinoTipo()) && tramo.getDestinoId() != null) {
            deposito = logisticsClient.obtenerDeposito(tramo.getDestinoId()).orElse(null);
        }
        String descripcionDestino = deposito != null ? deposito.getDireccion() : tramo.getDestinoTipo();
        ContenedorEstado estadoContenedor = solicitud.getContenedor() != null
                ? solicitud.getContenedor().getEstado()
                : null;
        return PendingContainerResponseDto.builder()
                .solicitudId(solicitud.getId())
                .contenedorId(solicitud.getContenedor().getId())
                .rutaId(rutaId)
                .tramoId(tramo.getId())
                .estadoTramo(tramo.getEstado())
                .depositoDestinoId(tramo.getDestinoId())
                .depositoDestinoNombre(deposito != null ? deposito.getNombre() : null)
                .destinoDescripcion(descripcionDestino)
                .estadoContenedor(estadoContenedor)
                .build();
    }

    private String normalize(String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT).trim();
    }
}
