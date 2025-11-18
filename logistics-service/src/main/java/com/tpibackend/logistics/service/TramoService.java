package com.tpibackend.logistics.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpibackend.distance.DistanceClient;
import com.tpibackend.distance.model.DistanceResult;
import com.tpibackend.logistics.client.FleetClient;
import com.tpibackend.logistics.client.FleetClient.TruckInfo;
import com.tpibackend.logistics.client.FleetClient.TruckLookupResult;
import com.tpibackend.logistics.client.FleetClient.TarifaActiva;
import com.tpibackend.logistics.client.OrdersClient;
import com.tpibackend.logistics.dto.integration.SolicitudLogisticaResponse;
import com.tpibackend.logistics.dto.request.AsignarCamionRequest;
import com.tpibackend.logistics.dto.request.RegistrarFinTramoRequest;
import com.tpibackend.logistics.dto.request.RegistrarInicioTramoRequest;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.exception.BusinessException;
import com.tpibackend.logistics.exception.NotFoundException;
import com.tpibackend.logistics.integration.OrdersSyncGateway;
import com.tpibackend.logistics.mapper.LogisticsMapper;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.model.Ruta;
import com.tpibackend.logistics.model.Tramo;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.repository.DepositoRepository;
import com.tpibackend.logistics.repository.RutaRepository;
import com.tpibackend.logistics.repository.TramoRepository;

@Service
@Transactional
public class TramoService {

    private static final Logger log = LoggerFactory.getLogger(TramoService.class);

    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;
    private final FleetClient fleetClient;
    private final OrdersSyncGateway ordersSyncGateway;
    private final DistanceClient distanceClient;
    private final RutaRepository rutaRepository;
    private final OrdersClient ordersClient;
    private final TarifaService tarifaService;

    public TramoService(TramoRepository tramoRepository,
            DepositoRepository depositoRepository,
            FleetClient fleetClient,
            OrdersSyncGateway ordersSyncGateway,
            DistanceClient distanceClient,
            RutaRepository rutaRepository,
            OrdersClient ordersClient,
            TarifaService tarifaService) {
        this.tramoRepository = tramoRepository;
        this.depositoRepository = depositoRepository;
        this.fleetClient = fleetClient;
        this.ordersSyncGateway = ordersSyncGateway;
        this.distanceClient = distanceClient;
        this.rutaRepository = rutaRepository;
        this.ordersClient = ordersClient;
        this.tarifaService = tarifaService;
    }

    public TramoResponse asignarCamion(Long tramoId, AsignarCamionRequest request) {
        Tramo tramo = obtenerTramo(tramoId);
        if (tramo.getEstado() == TramoEstado.INICIADO || tramo.getEstado() == TramoEstado.FINALIZADO) {
            throw new BusinessException("No se puede asignar camión a un tramo " + tramo.getEstado());
        }

        Ruta ruta = tramo.getRuta();
        BigDecimal pesoCarga = resolverPesoCarga(ruta);
        BigDecimal volumenCarga = resolverVolumenCarga(ruta);
        if (pesoCarga == null || volumenCarga == null) {
            throw new BusinessException("No se pudo determinar el peso/volumen del contenedor asociado al tramo");
        }

        log.info("Validando camión {} en FleetService", request.camionId());
        TruckLookupResult camionLookup = fleetClient.obtenerCamion(request.camionId());
        log.info("FleetService respondió {} consultando camión {} en {}", camionLookup.statusCode().value(),
                request.camionId(), camionLookup.uri());
        TruckInfo camion = camionLookup.truck();
        if (camion.disponible() != null && !camion.disponible()) {
            throw new BusinessException("El camión " + request.camionId() + " no está disponible");
        }

        if (camion.capacidadPeso() != null && camion.capacidadPeso().compareTo(pesoCarga) < 0) {
            throw new BusinessException("El camión no soporta el peso requerido");
        }
        if (camion.capacidadVolumen() != null && camion.capacidadVolumen().compareTo(volumenCarga) < 0) {
            throw new BusinessException("El camión no soporta el volumen requerido");
        }

        if (camionTieneTramosIniciados(request.camionId(), tramo.getId())) {
            throw new BusinessException("El camión " + request.camionId() + " ya se encuentra realizando otro tramo");
        }

        Long camionAnterior = tramo.getCamionId();

        tramo.setCamionId(request.camionId());
        tramo.setEstado(TramoEstado.ASIGNADO);
        tramoRepository.save(tramo);

        if (camionAnterior != null && !Objects.equals(camionAnterior, request.camionId())) {
            sincronizarDisponibilidadCamion(camionAnterior);
        }

        log.info("Camión {} asignado al tramo {}", request.camionId(), tramoId);
        return LogisticsMapper.toTramoResponse(tramo);
    }

    public TramoResponse iniciarTramo(Long tramoId, RegistrarInicioTramoRequest request) {
        Tramo tramo = obtenerTramo(tramoId);
        if (tramo.getCamionId() == null) {
            throw new BusinessException("El tramo no tiene camión asignado");
        }
        if (tramo.getEstado() != TramoEstado.ASIGNADO) {
            throw new BusinessException("El tramo no puede iniciarse en estado " + tramo.getEstado());
        }

        if (camionTieneTramosIniciados(tramo.getCamionId(), tramo.getId())) {
            throw new BusinessException(
                    "El camión " + tramo.getCamionId() + " ya se encuentra realizando otro tramo en curso");
        }

        OffsetDateTime inicio = request != null && request.fechaHoraInicio() != null
                ? request.fechaHoraInicio()
                : OffsetDateTime.now();
        validarOrdenDeInicio(tramo, inicio);
        if (tramo.getFechaHoraFin() != null && inicio.isAfter(tramo.getFechaHoraFin())) {
            throw new BusinessException("fechaHoraInicio no puede ser posterior a la fecha de finalización registrada");
        }

        tramo.setEstado(TramoEstado.INICIADO);
        tramo.setFechaHoraInicio(inicio);
        tramoRepository.save(tramo);

        // Marcar camión como no disponible
        fleetClient.actualizarDisponibilidad(tramo.getCamionId(), false, "En tránsito - Tramo " + tramoId);

        Ruta ruta = tramo.getRuta();
        if (ruta.getSolicitudId() != null) {
            // TODO: reemplazar por evento; Logistics no debería realizar update directo en Orders
            ordersSyncGateway.notificarEstado(ruta.getSolicitudId(), "EN_TRANSITO", "EN_TRANSITO");
        }

        log.info("Tramo {} iniciado", tramoId);
        return LogisticsMapper.toTramoResponse(tramo);
    }

    private void validarOrdenDeInicio(Tramo tramo, OffsetDateTime fechaHoraInicio) {
        List<Tramo> tramosRuta = tramoRepository.findByRutaIdOrderByIdAsc(tramo.getRuta().getId());
        boolean tramoEncontrado = false;
        OffsetDateTime ultimaFinalizacionPrev = null;

        for (Tramo tramoRuta : tramosRuta) {
            if (tramoRuta.getId().equals(tramo.getId())) {
                tramoEncontrado = true;
                break;
            }

            if (tramoRuta.getEstado() != TramoEstado.FINALIZADO) {
                throw new BusinessException(
                        "No se puede iniciar este tramo porque existen tramos anteriores de la misma ruta que aún no están finalizados");
            }
            if (tramoRuta.getFechaHoraFin() != null && (ultimaFinalizacionPrev == null
                    || tramoRuta.getFechaHoraFin().isAfter(ultimaFinalizacionPrev))) {
                ultimaFinalizacionPrev = tramoRuta.getFechaHoraFin();
            }
        }

        if (!tramoEncontrado) {
            throw new BusinessException("El tramo no pertenece a la ruta configurada");
        }

        if (fechaHoraInicio != null && ultimaFinalizacionPrev != null
                && fechaHoraInicio.isBefore(ultimaFinalizacionPrev)) {
            throw new BusinessException("No se puede iniciar este tramo antes de la finalización del tramo anterior");
        }
    }

    private void validarOrdenDeFinalizacion(Tramo tramo, OffsetDateTime fechaHoraFin) {
        List<Tramo> tramosRuta = tramoRepository.findByRutaIdOrderByIdAsc(tramo.getRuta().getId());
        boolean tramoEncontrado = false;
        OffsetDateTime ultimaFinalizacionPrev = null;

        for (Tramo tramoRuta : tramosRuta) {
            if (tramoRuta.getId().equals(tramo.getId())) {
                tramoEncontrado = true;
                break;
            }
            if (tramoRuta.getFechaHoraFin() != null && (ultimaFinalizacionPrev == null
                    || tramoRuta.getFechaHoraFin().isAfter(ultimaFinalizacionPrev))) {
                ultimaFinalizacionPrev = tramoRuta.getFechaHoraFin();
            }
        }

        if (!tramoEncontrado) {
            throw new BusinessException("El tramo no pertenece a la ruta configurada");
        }

        if (ultimaFinalizacionPrev != null && fechaHoraFin.isBefore(ultimaFinalizacionPrev)) {
            throw new BusinessException("No se puede finalizar este tramo antes de la finalización del tramo anterior");
        }
    }

    public TramoResponse finalizarTramo(Long tramoId, RegistrarFinTramoRequest request) {
        Tramo tramo = obtenerTramo(tramoId);
        if (tramo.getEstado() != TramoEstado.INICIADO) {
            throw new BusinessException("El tramo no puede finalizarse en estado " + tramo.getEstado());
        }
        if (tramo.getFechaHoraInicio() == null) {
            throw new BusinessException("El tramo no tiene fecha de inicio registrada");
        }
        OffsetDateTime fin = request != null && request.fechaHoraFin() != null
                ? request.fechaHoraFin()
                : OffsetDateTime.now();
        if (!fin.isAfter(tramo.getFechaHoraInicio())) {
            throw new BusinessException("fechaHoraFin debe ser posterior a fechaHoraInicio del tramo");
        }
        validarOrdenDeFinalizacion(tramo, fin);
        tramo.setFechaHoraFin(fin);
        double distanciaReal = resolverDistanciaReal(tramo);
        tramo.setDistanciaKmReal(distanciaReal);
        long minutosReales = Duration.between(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin()).toMinutes();
        tramo.setTiempoRealMinutos(Math.max(0, minutosReales));

        int diasEstadia = calcularDiasEstadiaReal(tramo);
        tramo.setDiasEstadia(diasEstadia);
        BigDecimal costoEstadiaDia = obtenerCostoEstadiaDia(tramo);
        tramo.setCostoEstadiaDia(costoEstadiaDia);
        BigDecimal costoEstadia = tarifaService.calcularCostoEstadia(diasEstadia, costoEstadiaDia);
        tramo.setCostoEstadia(costoEstadia);

        TarifaActiva tarifa = tarifaService.obtenerTarifaActiva();
        BigDecimal costoDistancia = tarifaService.calcularCostoPorDistancia(distanciaReal, tarifa);
        BigDecimal costoTiempo = tarifaService.calcularCostoPorTiempo(tramo.getTiempoRealMinutos(), tarifa);
        BigDecimal costoReal = costoDistancia.add(costoTiempo).add(costoEstadia);
        tramo.setCostoReal(costoReal);
        tramo.setEstado(TramoEstado.FINALIZADO);
        tramoRepository.save(tramo);

        sincronizarDisponibilidadCamion(tramo.getCamionId());

        Ruta ruta = tramo.getRuta();
        List<Tramo> tramosRuta = tramoRepository.findByRutaIdOrderByIdAsc(ruta.getId());
        BigDecimal costoTotalReal = tramosRuta.stream()
                .map(Tramo::getCostoReal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ruta.setCostoTotalReal(costoTotalReal);
        long tiempoRealTotal = calcularVentanaLogistica(tramosRuta);
        ruta.setTiempoRealMinutos(tiempoRealTotal);
        rutaRepository.save(ruta);

        log.info("Tramo {} finalizado con costo {}", tramoId, costoReal);

        if (ruta.getSolicitudId() != null) {
            boolean rutaFinalizada = tramosRuta.stream()
                    .allMatch(t -> t.getEstado() == TramoEstado.FINALIZADO);
            if (rutaFinalizada) {
                ordersSyncGateway.notificarFinalizacion(ruta.getSolicitudId(), "ENTREGADO", "ENTREGADO",
                        ruta.getCostoTotalReal(), ruta.getTiempoRealMinutos());
            } else if (destinoEsDeposito(tramo)) {
                ordersSyncGateway.notificarEstado(ruta.getSolicitudId(), "EN_TRANSITO", "EN_DEPOSITO");
            }
        }

        return LogisticsMapper.toTramoResponse(tramo);
    }

    private long calcularVentanaLogistica(List<Tramo> tramosRuta) {
        Optional<OffsetDateTime> primerInicio = tramosRuta.stream()
                .map(Tramo::getFechaHoraInicio)
                .filter(Objects::nonNull)
                .min(OffsetDateTime::compareTo);
        Optional<OffsetDateTime> ultimoFin = tramosRuta.stream()
                .map(Tramo::getFechaHoraFin)
                .filter(Objects::nonNull)
                .max(OffsetDateTime::compareTo);

        if (primerInicio.isPresent() && ultimoFin.isPresent()) {
            long minutos = Duration.between(primerInicio.get(), ultimoFin.get()).toMinutes();
            return Math.max(0, minutos);
        }

        return tramosRuta.stream()
                .map(Tramo::getTiempoRealMinutos)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
    }

    private Tramo obtenerTramo(Long tramoId) {
        return tramoRepository.findById(tramoId)
                .orElseThrow(() -> new NotFoundException("Tramo " + tramoId + " no encontrado"));
    }

    private double resolverDistanciaReal(Tramo tramo) {
        double fallback = tramo.getDistanciaKmEstimada() != null ? tramo.getDistanciaKmEstimada() : 0d;

        if (tramo.getOrigenLat() == null || tramo.getOrigenLng() == null
                || tramo.getDestinoLat() == null || tramo.getDestinoLng() == null) {
            log.warn("Tramo {} sin coordenadas persistidas, usando distancia alternativa {} km", tramo.getId(), fallback);
            return fallback;
        }

        try {
            DistanceResult data = distanceClient.getDistanceAndDuration(
                    tramo.getOrigenLat(), tramo.getOrigenLng(),
                    tramo.getDestinoLat(), tramo.getDestinoLng());
            double distancia = data.distanceKm();
            if (distancia <= 0 && fallback > 0) {
                log.warn("Distancia calculada <= 0 para tramo {}, aplicando fallback {} km", tramo.getId(), fallback);
                return fallback;
            }
            log.info("Distancia real para tramo {} calculada en {} km", tramo.getId(), distancia);
            return distancia;
        } catch (Exception ex) {
            log.warn("No se pudo calcular distancia real con distance-client para tramo {}. Usando fallback {} km",
                    tramo.getId(), fallback, ex);
            return fallback;
        }
    }

    public java.util.List<TramoResponse> listarTramos(Long camionId) {
        if (camionId != null) {
            log.debug("Listando tramos del camión {}", camionId);
            return tramoRepository.findByCamionIdOrderByRutaIdAsc(camionId)
                    .stream()
                    .map(LogisticsMapper::toTramoResponse)
                    .collect(java.util.stream.Collectors.toList());
        }
        return tramoRepository.findAll().stream()
                .map(LogisticsMapper::toTramoResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<TramoResponse> listarTramosPorRuta(Long rutaId) {
        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByIdAsc(rutaId);
        if (tramos.isEmpty() && !rutaRepository.existsById(rutaId)) {
            throw new NotFoundException("Ruta " + rutaId + " no encontrada");
        }
        return tramos.stream()
                .map(LogisticsMapper::toTramoResponse)
                .toList();
    }

    private BigDecimal resolverPesoCarga(Ruta ruta) {
        if (ruta.getPesoTotal() != null && ruta.getPesoTotal().compareTo(BigDecimal.ZERO) > 0) {
            return ruta.getPesoTotal();
        }
        return obtenerDatosSolicitud(ruta).map(SolicitudLogisticaResponse::pesoContenedor).orElse(null);
    }

    private BigDecimal resolverVolumenCarga(Ruta ruta) {
        if (ruta.getVolumenTotal() != null && ruta.getVolumenTotal().compareTo(BigDecimal.ZERO) > 0) {
            return ruta.getVolumenTotal();
        }
        return obtenerDatosSolicitud(ruta).map(SolicitudLogisticaResponse::volumenContenedor).orElse(null);
    }

    private java.util.Optional<SolicitudLogisticaResponse> obtenerDatosSolicitud(Ruta ruta) {
        if (ruta.getSolicitudId() == null) {
            return java.util.Optional.empty();
        }
        java.util.Optional<SolicitudLogisticaResponse> solicitud = ordersClient.obtenerSolicitud(ruta.getSolicitudId());
        if (solicitud.isEmpty()) {
            log.warn("No se pudo recuperar información de la solicitud {} para validar camiones", ruta.getSolicitudId());
        }
        return solicitud;
    }

    public TramoResponse obtenerDetalle(Long tramoId) {
        return LogisticsMapper.toTramoResponse(obtenerTramo(tramoId));
    }

    private boolean destinoEsDeposito(Tramo tramo) {
        return tramo.getDestinoTipo() == LocationType.DEPOSITO;
    }

    private int calcularDiasEstadiaReal(Tramo tramo) {
        if (tramo.getDestinoTipo() != LocationType.DEPOSITO || tramo.getDestinoId() == null
                || tramo.getFechaHoraFin() == null) {
            return 0;
        }
        Ruta ruta = tramo.getRuta();
        if (ruta == null || ruta.getId() == null) {
            return 0;
        }
        List<Tramo> tramosRuta = tramoRepository.findByRutaIdOrderByIdAsc(ruta.getId());
        for (int i = 0; i < tramosRuta.size(); i++) {
            Tramo actual = tramosRuta.get(i);
            if (actual.getId().equals(tramo.getId()) && i + 1 < tramosRuta.size()) {
                Tramo siguiente = tramosRuta.get(i + 1);
                if (siguiente.getFechaHoraInicio() == null) {
                    return 0;
                }
                Duration espera = Duration.between(tramo.getFechaHoraFin(), siguiente.getFechaHoraInicio());
                if (espera.isNegative() || espera.isZero()) {
                    return 0;
                }
                long minutos = espera.toMinutes();
                double dias = minutos / (60d * 24d);
                return (int) Math.ceil(dias);
            }
        }
        return 0;
    }

    private BigDecimal obtenerCostoEstadiaDia(Tramo tramo) {
        if (tramo.getDestinoTipo() == LocationType.DEPOSITO && tramo.getDestinoId() != null) {
            return depositoRepository.findById(tramo.getDestinoId())
                    .map(Deposito::getCostoEstadiaDia)
                    .orElse(tramo.getCostoEstadiaDia() != null ? tramo.getCostoEstadiaDia() : BigDecimal.ZERO);
        }
        return tramo.getCostoEstadiaDia() != null ? tramo.getCostoEstadiaDia() : BigDecimal.ZERO;
    }

    private boolean camionTieneTramosIniciados(Long camionId, Long tramoAExcluir) {
        if (camionId == null) {
            return false;
        }
        return tramoRepository.findByCamionIdOrderByRutaIdAsc(camionId).stream()
                .filter(t -> tramoAExcluir == null || !t.getId().equals(tramoAExcluir))
                .anyMatch(t -> t.getEstado() == TramoEstado.INICIADO);
    }

    private boolean camionTieneTramosIniciados(Long camionId) {
        return camionTieneTramosIniciados(camionId, null);
    }

    private void sincronizarDisponibilidadCamion(Long camionId) {
        if (camionId == null) {
            return;
        }
        if (camionTieneTramosIniciados(camionId)) {
            fleetClient.actualizarDisponibilidad(camionId, false, "En tránsito");
        } else {
            fleetClient.actualizarDisponibilidad(camionId, true, null);
        }
    }

}
