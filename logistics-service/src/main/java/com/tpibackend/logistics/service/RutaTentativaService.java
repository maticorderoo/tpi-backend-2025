package com.tpibackend.logistics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tpibackend.distance.DistanceClient;
import com.tpibackend.distance.model.DistanceResult;
import com.tpibackend.logistics.client.OrdersClient;
import com.tpibackend.logistics.config.EstimacionProperties;
import com.tpibackend.logistics.dto.integration.SolicitudLogisticaResponse;
import com.tpibackend.logistics.dto.integration.SolicitudLogisticaResponse.Punto;
import com.tpibackend.logistics.dto.response.LocationSummary;
import com.tpibackend.logistics.dto.response.RutaTentativaResponse;
import com.tpibackend.logistics.dto.response.TramoTentativoResponse;
import com.tpibackend.logistics.exception.NotFoundException;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoTipo;
import com.tpibackend.logistics.repository.DepositoRepository;

@Service
public class RutaTentativaService {

    private static final Logger log = LoggerFactory.getLogger(RutaTentativaService.class);

    private final OrdersClient ordersClient;
    private final DepositoRepository depositoRepository;
    private final DistanceClient distanceClient;
    private final EstimacionProperties estimacionProperties;

    public RutaTentativaService(OrdersClient ordersClient,
            DepositoRepository depositoRepository,
            DistanceClient distanceClient,
            EstimacionProperties estimacionProperties) {
        this.ordersClient = ordersClient;
        this.depositoRepository = depositoRepository;
        this.distanceClient = distanceClient;
        this.estimacionProperties = estimacionProperties;
    }

    public List<RutaTentativaResponse> generarTentativas(Long solicitudId) {
        SolicitudLogisticaResponse solicitud = ordersClient.obtenerSolicitud(solicitudId)
                .orElseThrow(() -> new NotFoundException("Solicitud " + solicitudId + " no encontrada"));

        LocationNode origen = LocationNode.fromSolicitud(LocationType.ORIGEN_SOLICITUD, solicitud.id(), solicitud.origen());
        LocationNode destino = LocationNode.fromSolicitud(LocationType.DESTINO_SOLICITUD, solicitud.id(), solicitud.destino());

        List<Deposito> depositos = depositoRepository.findAll();
        List<RutaTentativaResponse> rutas = new ArrayList<>();

        rutas.add(construirRuta(solicitudId, List.of(origen, destino)));

        for (Deposito deposito : depositos) {
            rutas.add(construirRuta(solicitudId, List.of(origen, LocationNode.fromDeposito(deposito), destino)));
        }

        for (int i = 0; i < depositos.size(); i++) {
            for (int j = i + 1; j < depositos.size(); j++) {
                LocationNode primero = LocationNode.fromDeposito(depositos.get(i));
                LocationNode segundo = LocationNode.fromDeposito(depositos.get(j));
                rutas.add(construirRuta(solicitudId, List.of(origen, primero, segundo, destino)));
                rutas.add(construirRuta(solicitudId, List.of(origen, segundo, primero, destino)));
            }
        }

        return rutas;
    }

    private RutaTentativaResponse construirRuta(Long solicitudId, List<LocationNode> nodos) {
        if (nodos.size() < 2) {
            return new RutaTentativaResponse(solicitudId, 0, 0, 0d, BigDecimal.ZERO, 0L, Collections.emptyList());
        }

        List<TramoTentativoResponse> tramos = new ArrayList<>();
        BigDecimal costoTotal = BigDecimal.ZERO;
        double distanciaTotal = 0d;
        long tiempoTotal = 0L;

        for (int i = 0; i < nodos.size() - 1; i++) {
            LocationNode origen = nodos.get(i);
            LocationNode destino = nodos.get(i + 1);
            DistanceResult resultado = null;
            try {
                resultado = distanceClient.getDistanceAndDuration(origen.lat(), origen.lng(), destino.lat(), destino.lng());
            } catch (Exception ex) {
                log.warn("No se pudo calcular distancia entre {} y {}: {}", origen.descripcion(), destino.descripcion(), ex.getMessage());
            }

            double distancia = resultado != null ? resultado.distanceKm() : 0d;
            long tiempo = resultado != null ? Math.round(resultado.durationMinutes()) : 0L;
            BigDecimal costo = calcularCosto(distancia, tiempo, destino);

            TramoTentativoResponse tramo = new TramoTentativoResponse(
                    origen.toSummary(),
                    destino.toSummary(),
                    determinarTipo(origen.tipo(), destino.tipo()),
                    distancia,
                    tiempo,
                    costo);
            tramos.add(tramo);
            costoTotal = costoTotal.add(costo);
            distanciaTotal += distancia;
            tiempoTotal += tiempo;
        }

        int depositosUtilizados = (int) nodos.stream().filter(node -> node.tipo() == LocationType.DEPOSITO).count();

        return new RutaTentativaResponse(
                solicitudId,
                tramos.size(),
                depositosUtilizados,
                distanciaTotal,
                costoTotal,
                tiempoTotal,
                tramos);
    }

    private BigDecimal calcularCosto(double distanciaKm, long tiempoMinutos, LocationNode destino) {
        BigDecimal distancia = BigDecimal.valueOf(distanciaKm);
        BigDecimal costoBase = estimacionProperties.getCostoKmBase().multiply(distancia);
        BigDecimal costoCombustible = estimacionProperties.getConsumoLitrosKm()
                .multiply(distancia)
                .multiply(estimacionProperties.getPrecioCombustible());

        BigDecimal horas = BigDecimal.valueOf(tiempoMinutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal costoTiempo = estimacionProperties.getCostoTiempoHora().multiply(horas);

        BigDecimal total = costoBase.add(costoCombustible).add(costoTiempo);

        if (destino.deposito() != null && estimacionProperties.getDiasEstadiaDeposito() > 0) {
            BigDecimal estadia = destino.deposito().getCostoEstadiaDia()
                    .multiply(BigDecimal.valueOf(estimacionProperties.getDiasEstadiaDeposito()));
            total = total.add(estadia);
        }
        return total;
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
}
