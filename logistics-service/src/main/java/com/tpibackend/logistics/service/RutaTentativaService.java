package com.tpibackend.logistics.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpibackend.distance.DistanceClient;
import com.tpibackend.distance.model.DistanceResult;
import com.tpibackend.logistics.client.FleetClient.TarifaActiva;
import com.tpibackend.logistics.client.OrdersClient;
import com.tpibackend.logistics.dto.integration.SolicitudLogisticaResponse;
import com.tpibackend.logistics.dto.integration.SolicitudLogisticaResponse.Punto;
import com.tpibackend.logistics.dto.response.LocationSummary;
import com.tpibackend.logistics.dto.response.RutaResponse;
import com.tpibackend.logistics.dto.response.RutaTentativaResponse;
import com.tpibackend.logistics.dto.response.TramoTentativoResponse;
import com.tpibackend.logistics.exception.NotFoundException;
import com.tpibackend.logistics.exception.TarifaNoConfiguradaException;
import com.tpibackend.logistics.exception.BusinessException;
import com.tpibackend.logistics.integration.OrdersSyncGateway;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.model.Ruta;
import com.tpibackend.logistics.model.RutaTentativa;
import com.tpibackend.logistics.model.Tramo;
import com.tpibackend.logistics.model.TramoTentativo;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.RutaTentativaEstado;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.model.enums.TramoTipo;
import com.tpibackend.logistics.repository.DepositoRepository;
import com.tpibackend.logistics.repository.RutaRepository;
import com.tpibackend.logistics.repository.RutaTentativaRepository;
import com.tpibackend.logistics.mapper.LogisticsMapper;

@Service
@Transactional
public class RutaTentativaService {

    private static final Logger log = LoggerFactory.getLogger(RutaTentativaService.class);

    private final OrdersClient ordersClient;
    private final DepositoRepository depositoRepository;
    private final DistanceClient distanceClient;
    private final TarifaService tarifaService;
    private final RutaTentativaRepository rutaTentativaRepository;
    private final RutaRepository rutaRepository;
    private final OrdersSyncGateway ordersSyncGateway;

    public RutaTentativaService(OrdersClient ordersClient,
            DepositoRepository depositoRepository,
            DistanceClient distanceClient,
            TarifaService tarifaService,
            RutaTentativaRepository rutaTentativaRepository,
            RutaRepository rutaRepository,
            OrdersSyncGateway ordersSyncGateway) {
        this.ordersClient = ordersClient;
        this.depositoRepository = depositoRepository;
        this.distanceClient = distanceClient;
        this.tarifaService = tarifaService;
        this.rutaTentativaRepository = rutaTentativaRepository;
        this.rutaRepository = rutaRepository;
        this.ordersSyncGateway = ordersSyncGateway;
    }

    public List<RutaTentativaResponse> generarTentativas(Long solicitudId) {
        SolicitudLogisticaResponse solicitud = ordersClient.obtenerSolicitud(solicitudId)
                .orElseThrow(() -> new NotFoundException("Solicitud " + solicitudId + " no encontrada"));

        LocationNode origen = LocationNode.fromSolicitud(LocationType.ORIGEN_SOLICITUD, solicitud.id(),
                solicitud.origen());
        LocationNode destino = LocationNode.fromSolicitud(LocationType.DESTINO_SOLICITUD, solicitud.id(),
                solicitud.destino());

        List<LocationNode> depositos = depositoRepository.findAll().stream()
                .map(LocationNode::fromDeposito)
                .toList();

        // borrar las tentativas previas para la solicitud
        rutaTentativaRepository.deleteBySolicitudId(solicitudId);

        TarifaActiva tarifa = obtenerTarifaActivaParaTentativas();

        List<RutaTentativa> rutas = new ArrayList<>();
        rutas.add(construirRutaTentativa(solicitudId, List.of(origen, destino), tarifa));

        for (LocationNode deposito : depositos) {
            rutas.add(construirRutaTentativa(solicitudId, List.of(origen, deposito, destino), tarifa));
        }

        for (int i = 0; i < depositos.size(); i++) {
            for (int j = i + 1; j < depositos.size(); j++) {
                LocationNode primero = depositos.get(i);
                LocationNode segundo = depositos.get(j);
                rutas.add(construirRutaTentativa(solicitudId, List.of(origen, primero, segundo, destino), tarifa));
                rutas.add(construirRutaTentativa(solicitudId, List.of(origen, segundo, primero, destino), tarifa));
            }
        }

        // aplicar orden y límite ANTES de persistir: primero por costoTotalAprox asc,
        // luego por tiempoEstimadoMinutos asc
        List<RutaTentativa> aPersistir = rutas.stream()
                .sorted(Comparator.comparing(RutaTentativa::getCostoTotalAprox)
                        .thenComparing(RutaTentativa::getTiempoEstimadoMinutos))
                .limit(5)
                .toList();

        if (!aPersistir.isEmpty()) {
            rutaTentativaRepository.saveAll(aPersistir);
        }

        // devolver las tentativas guardadas (como máximo 5) en la respuesta
        return rutaTentativaRepository.findBySolicitudIdOrderByCreatedAtAsc(solicitudId).stream()
                .map(this::toResponse)
                .toList();
    }

    private RutaTentativa construirRutaTentativa(Long solicitudId, List<LocationNode> nodos, TarifaActiva tarifa) {
        RutaTentativa ruta = new RutaTentativa();
        ruta.setSolicitudId(solicitudId);

        if (nodos.size() < 2) {
            ruta.setCantTramos(0);
            ruta.setCantDepositos(0);
            ruta.setCostoTotalAprox(BigDecimal.ZERO);
            ruta.setDistanciaTotalKm(0d);
            ruta.setTiempoEstimadoMinutos(0L);
            return ruta;
        }

        List<TramoTentativo> tramos = new ArrayList<>();
        BigDecimal costoTotal = BigDecimal.ZERO;
        double distanciaTotal = 0d;
        long tiempoTotal = 0L;

        for (int i = 0; i < nodos.size() - 1; i++) {
            LocationNode origen = nodos.get(i);
            LocationNode destino = nodos.get(i + 1);
            DistanceResult resultado = null;
            try {
                resultado = distanceClient.getDistanceAndDuration(origen.lat(), origen.lng(), destino.lat(),
                        destino.lng());
            } catch (Exception ex) {
                log.warn("No se pudo calcular distancia entre {} y {}: {}", origen.descripcion(), destino.descripcion(),
                        ex.getMessage());
            }

            double distancia = resultado != null ? resultado.distanceKm() : 0d;
            long tiempo = resultado != null ? Math.round(resultado.durationMinutes()) : 0L;
            CostoTentativo costo = calcularCosto(distancia, tiempo, destino, tarifa);

            TramoTentativo tramo = new TramoTentativo();
            tramo.setOrden(i + 1);
            tramo.setOrigenTipo(origen.tipo());
            tramo.setOrigenId(origen.referenciaId());
            tramo.setOrigenDescripcion(origen.descripcion());
            tramo.setOrigenLat(origen.lat());
            tramo.setOrigenLng(origen.lng());
            tramo.setDestinoTipo(destino.tipo());
            tramo.setDestinoId(destino.referenciaId());
            tramo.setDestinoDescripcion(destino.descripcion());
            tramo.setDestinoLat(destino.lat());
            tramo.setDestinoLng(destino.lng());
            tramo.setTipo(determinarTipo(origen.tipo(), destino.tipo()));
            tramo.setDistanciaKm(distancia);
            tramo.setTiempoEstimadoMinutos(tiempo);
            tramo.setCostoAproximado(costo.total());
            tramo.setDiasEstadia(costo.diasEstadia());
            tramo.setCostoEstadiaDia(costo.costoEstadiaDia());
            tramo.setCostoEstadia(costo.costoEstadia());
            tramos.add(tramo);
            costoTotal = costoTotal.add(costo.total());
            distanciaTotal += distancia;
            tiempoTotal += tiempo;
        }

        int depositosUtilizados = (int) nodos.stream().filter(node -> node.tipo() == LocationType.DEPOSITO).count();

        ruta.setCantTramos(tramos.size());
        ruta.setCantDepositos(depositosUtilizados);
        ruta.setDistanciaTotalKm(distanciaTotal);
        ruta.setCostoTotalAprox(costoTotal);
        ruta.setTiempoEstimadoMinutos(tiempoTotal);
        tramos.forEach(ruta::addTramo);
        return ruta;
    }

    private CostoTentativo calcularCosto(double distanciaKm, long tiempoMinutos, LocationNode destino, TarifaActiva tarifa) {
        BigDecimal costoBase = tarifaService.calcularCostoPorDistancia(distanciaKm, tarifa);
        BigDecimal costoTiempo = tarifaService.calcularCostoPorTiempo(tiempoMinutos, tarifa);

        BigDecimal total = costoBase.add(costoTiempo);

        int diasEstadia = 0;
        BigDecimal costoEstadiaDia = BigDecimal.ZERO;
        BigDecimal costoEstadia = BigDecimal.ZERO;
        if (destino.deposito() != null && tarifaService.diasEstadiaDeposito() > 0) {
            diasEstadia = tarifaService.diasEstadiaDeposito();
            costoEstadiaDia = destino.deposito().getCostoEstadiaDia();
            costoEstadia = tarifaService.calcularCostoEstadia(diasEstadia, costoEstadiaDia);
            total = total.add(costoEstadia);
        }
        return new CostoTentativo(total, diasEstadia, costoEstadiaDia, costoEstadia);
    }

    private TarifaActiva obtenerTarifaActivaParaTentativas() {
        try {
            return tarifaService.obtenerTarifaActiva();
        } catch (TarifaNoConfiguradaException ex) {
            throw new TarifaNoConfiguradaException(
                    "No se puede generar rutas tentativas porque no hay tarifa activa configurada en Flota");
        }
    }

    private TramoTipo determinarTipo(LocationType origenTipo, LocationType destinoTipo) {
        if (origenTipo == LocationType.DEPOSITO && destinoTipo == LocationType.DEPOSITO) {
            return TramoTipo.DEPOSITO_A_DEPOSITO;
        }
        if (origenTipo == LocationType.DEPOSITO) {
            return TramoTipo.DEPOSITO_A_DESTINO;
        }
        if (destinoTipo == LocationType.DEPOSITO) {
            return TramoTipo.ORIGEN_A_DEPOSITO;
        }
        return TramoTipo.ORIGEN_A_DESTINO;
    }

    public RutaResponse confirmarTentativa(Long solicitudId, Long rutaTentativaId) {
        RutaTentativa tentativa = rutaTentativaRepository.findByIdAndSolicitudId(rutaTentativaId, solicitudId)
                .orElseThrow(() -> new NotFoundException(
                        "Ruta tentativa " + rutaTentativaId + " no pertenece a la solicitud " + solicitudId));

        if (tentativa.getEstado() == RutaTentativaEstado.CONFIRMADA && tentativa.getRutaDefinitivaId() != null) {
            Ruta existente = rutaRepository.findById(tentativa.getRutaDefinitivaId())
                    .orElseThrow(() -> new NotFoundException(
                            "Ruta definitiva " + tentativa.getRutaDefinitivaId() + " no encontrada"));
            return LogisticsMapper.toRutaResponse(existente);
        }

        rutaRepository.findBySolicitudId(solicitudId).ifPresent(ruta -> {
            throw new BusinessException("La solicitud ya cuenta con una ruta confirmada");
        });

        SolicitudLogisticaResponse solicitud = ordersClient.obtenerSolicitud(solicitudId)
                .orElseThrow(() -> new NotFoundException("Solicitud " + solicitudId + " no encontrada"));

        Ruta ruta = new Ruta();
        ruta.setSolicitudId(solicitudId);
        ruta.setCantTramos(tentativa.getCantTramos());
        ruta.setCantDepositos(tentativa.getCantDepositos());
        ruta.setCostoTotalAprox(tentativa.getCostoTotalAprox());
        ruta.setTiempoEstimadoMinutos(tentativa.getTiempoEstimadoMinutos());
        ruta.setPesoTotal(defaultZero(solicitud.pesoContenedor()));
        ruta.setVolumenTotal(defaultZero(solicitud.volumenContenedor()));

        List<Tramo> tramos = tentativa.getTramos().stream()
                .sorted(Comparator.comparing(TramoTentativo::getOrden))
                .map(t -> mapearTramo(ruta, t))
                .toList();
        tramos.forEach(ruta::addTramo);

        rutaRepository.save(ruta);

        tentativa.setEstado(RutaTentativaEstado.CONFIRMADA);
        tentativa.setRutaDefinitivaId(ruta.getId());
        rutaTentativaRepository.save(tentativa);

        ordersSyncGateway.notificarPlanificacion(solicitudId, ruta.getCostoTotalAprox(),
                ruta.getTiempoEstimadoMinutos(), ruta.getId());
        ordersSyncGateway.notificarEstado(solicitudId, "PROGRAMADA", "PROGRAMADA");

        return LogisticsMapper.toRutaResponse(ruta);
    }

    private Tramo mapearTramo(Ruta ruta, TramoTentativo tentativo) {
        Tramo tramo = new Tramo();
        tramo.setRuta(ruta);
        tramo.setOrigenTipo(tentativo.getOrigenTipo());
        tramo.setOrigenId(tentativo.getOrigenId());
        tramo.setOrigenLat(tentativo.getOrigenLat());
        tramo.setOrigenLng(tentativo.getOrigenLng());
        tramo.setDestinoTipo(tentativo.getDestinoTipo());
        tramo.setDestinoId(tentativo.getDestinoId());
        tramo.setDestinoLat(tentativo.getDestinoLat());
        tramo.setDestinoLng(tentativo.getDestinoLng());
        tramo.setTipo(tentativo.getTipo());
        tramo.setEstado(TramoEstado.ESTIMADO);
        tramo.setDistanciaKmEstimada(tentativo.getDistanciaKm());
        tramo.setTiempoEstimadoMinutos(tentativo.getTiempoEstimadoMinutos());
        tramo.setCostoAprox(tentativo.getCostoAproximado());
        tramo.setDiasEstadia(tentativo.getDiasEstadia());
        tramo.setCostoEstadiaDia(tentativo.getCostoEstadiaDia());
        tramo.setCostoEstadia(tentativo.getCostoEstadia());
        return tramo;
    }

    private RutaTentativaResponse toResponse(RutaTentativa ruta) {
        List<TramoTentativoResponse> tramos = ruta.getTramos().stream()
                .sorted(Comparator.comparing(TramoTentativo::getOrden))
                .map(this::toResponse)
                .toList();
        return new RutaTentativaResponse(
                ruta.getId(),
                ruta.getSolicitudId(),
                ruta.getEstado(),
                ruta.getCantTramos(),
                ruta.getCantDepositos(),
                ruta.getDistanciaTotalKm(),
                ruta.getCostoTotalAprox(),
                ruta.getTiempoEstimadoMinutos(),
                tramos);
    }

    private TramoTentativoResponse toResponse(TramoTentativo tramo) {
        LocationSummary origen = new LocationSummary(tramo.getOrigenTipo(), tramo.getOrigenId(),
                tramo.getOrigenDescripcion(), tramo.getOrigenLat(), tramo.getOrigenLng());
        LocationSummary destino = new LocationSummary(tramo.getDestinoTipo(), tramo.getDestinoId(),
                tramo.getDestinoDescripcion(), tramo.getDestinoLat(), tramo.getDestinoLng());
        return new TramoTentativoResponse(tramo.getOrden(), origen, destino, tramo.getTipo(),
                tramo.getDistanciaKm(), tramo.getTiempoEstimadoMinutos(), tramo.getCostoAproximado());
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private record LocationNode(LocationType tipo, Long referenciaId, double lat, double lng,
            String descripcion, Deposito deposito) {

        static LocationNode fromSolicitud(LocationType tipo, Long solicitudId, Punto punto) {
            if (punto == null) {
                throw new NotFoundException("La solicitud no tiene coordenadas registradas");
            }
            if (punto.latitud() == null || punto.longitud() == null) {
                throw new NotFoundException("La solicitud carece de latitud/longitud para " + tipo);
            }
            return new LocationNode(tipo, solicitudId, punto.latitud(), punto.longitud(),
                    punto.descripcion(), null);
        }

        static LocationNode fromDeposito(Deposito deposito) {
            return new LocationNode(LocationType.DEPOSITO, deposito.getId(), deposito.getLat(), deposito.getLng(),
                    deposito.getNombre(), deposito);
        }

        LocationSummary toSummary() {
            return new LocationSummary(tipo, referenciaId, descripcion,
                    lat, lng);
        }
    }

    private record CostoTentativo(BigDecimal total, int diasEstadia,
            BigDecimal costoEstadiaDia, BigDecimal costoEstadia) {
    }
}
