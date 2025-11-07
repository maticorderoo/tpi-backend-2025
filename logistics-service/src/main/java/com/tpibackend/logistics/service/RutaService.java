package com.tpibackend.logistics.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpibackend.distance.DistanceClient;
import com.tpibackend.distance.model.DistanceData;
import com.tpibackend.logistics.client.OrdersClient;
import com.tpibackend.logistics.dto.request.AsignarRutaRequest;
import com.tpibackend.logistics.dto.request.CrearRutaRequest;
import com.tpibackend.logistics.dto.request.DepositStopRequest;
import com.tpibackend.logistics.dto.request.LocationPointRequest;
import com.tpibackend.logistics.dto.response.RutaResponse;
import com.tpibackend.logistics.exception.BusinessException;
import com.tpibackend.logistics.exception.NotFoundException;
import com.tpibackend.logistics.mapper.LogisticsMapper;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.model.Ruta;
import com.tpibackend.logistics.model.Tramo;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.model.enums.TramoTipo;
import com.tpibackend.logistics.repository.DepositoRepository;
import com.tpibackend.logistics.repository.RutaRepository;

@Service
@Transactional
public class RutaService {

    private static final Logger log = LoggerFactory.getLogger(RutaService.class);

    private final RutaRepository rutaRepository;
    private final DepositoRepository depositoRepository;
    private final DistanceClient distanceClient;
    private final OrdersClient ordersClient;

    public RutaService(RutaRepository rutaRepository,
            DepositoRepository depositoRepository,
            DistanceClient distanceClient,
            OrdersClient ordersClient) {
        this.rutaRepository = rutaRepository;
        this.depositoRepository = depositoRepository;
        this.distanceClient = distanceClient;
        this.ordersClient = ordersClient;
    }

    public RutaResponse crearRuta(CrearRutaRequest request) {
        Ruta ruta = new Ruta();
        ruta.setCantDepositos(request.depositosIntermedios() == null ? 0 : request.depositosIntermedios().size());
        ruta.setPesoTotal(defaultZero(request.pesoCarga()));
        ruta.setVolumenTotal(defaultZero(request.volumenCarga()));

        List<DepositStopRequest> depositStops = request.depositosIntermedios() == null
                ? Collections.emptyList()
                : request.depositosIntermedios();
        Map<Long, Integer> diasPorDeposito = new HashMap<>();
        depositStops.forEach(stop -> diasPorDeposito.put(stop.depositoId(), stop.diasEstadia()));

        List<ResolvedLocation> resolvedPoints = new ArrayList<>();
        resolvedPoints.add(resolveLocation(request.origen(), null));
        for (DepositStopRequest stop : depositStops) {
            resolvedPoints.add(resolveLocation(null, stop));
        }
        resolvedPoints.add(resolveLocation(request.destino(), null));

        if (resolvedPoints.size() < 2) {
            throw new BusinessException("La ruta debe tener origen y destino válidos");
        }

        List<Tramo> tramos = new ArrayList<>();
        for (int i = 0; i < resolvedPoints.size() - 1; i++) {
            ResolvedLocation origen = resolvedPoints.get(i);
            ResolvedLocation destino = resolvedPoints.get(i + 1);

            Tramo tramo = new Tramo();
            tramo.setOrigenTipo(origen.tipo());
            tramo.setOrigenId(origen.referenciaId());
            tramo.setOrigenLat(origen.lat());
            tramo.setOrigenLng(origen.lng());
            tramo.setDestinoTipo(destino.tipo());
            tramo.setDestinoId(destino.referenciaId());
            tramo.setDestinoLat(destino.lat());
            tramo.setDestinoLng(destino.lng());
            tramo.setTipo(TramoTipo.TRASLADO);
            tramo.setEstado(TramoEstado.ESTIMADO);

            DistanceData distanceData = null;
            try {
                distanceData = distanceClient.getDistance(origen.lat(), origen.lng(), destino.lat(), destino.lng());
            } catch (Exception ex) {
                log.warn("No fue posible calcular distancia estimada entre {} y {}: {}",
                        origen.descripcion(), destino.descripcion(), ex.getMessage());
            }

            double distancia = distanceData != null ? distanceData.distanceKm() : 0d;
            tramo.setDistanciaKmEstimada(distancia);

            BigDecimal costoBase = request.costoKmBase().multiply(BigDecimal.valueOf(distancia));
            BigDecimal costoCombustible = request.consumoLitrosKm()
                    .multiply(BigDecimal.valueOf(distancia))
                    .multiply(request.precioCombustible());

            int diasEstadia = 0;
            BigDecimal costoEstadiaDia = BigDecimal.ZERO;
            BigDecimal costoEstadia = BigDecimal.ZERO;
            if (destino.deposito() != null) {
                diasEstadia = diasPorDeposito.getOrDefault(destino.deposito().getId(), 0);
                costoEstadiaDia = destino.deposito().getCostoEstadiaDia();
                costoEstadia = costoEstadiaDia.multiply(BigDecimal.valueOf(diasEstadia));
            }
            tramo.setDiasEstadia(diasEstadia);
            tramo.setCostoEstadiaDia(costoEstadiaDia);
            tramo.setCostoEstadia(costoEstadia);

            BigDecimal costoAprox = costoBase.add(costoCombustible).add(costoEstadia);
            tramo.setCostoAprox(costoAprox);

            tramos.add(tramo);
        }

        tramos.forEach(ruta::addTramo);
        ruta.setCantTramos(tramos.size());
        ruta.setCostoTotalAprox(ruta.calcularCostoTotalAprox());
        rutaRepository.save(ruta);

        log.info("Ruta {} generada con {} tramos", ruta.getId(), ruta.getCantTramos());

        return LogisticsMapper.toRutaResponse(ruta);
    }

    public RutaResponse asignarRuta(Long rutaId, AsignarRutaRequest request) {
        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new NotFoundException("Ruta " + rutaId + " no encontrada"));

        if (ruta.getSolicitudId() != null && !ruta.getSolicitudId().equals(request.solicitudId())) {
            throw new BusinessException("La ruta ya está asignada a la solicitud " + ruta.getSolicitudId());
        }

        ruta.setSolicitudId(request.solicitudId());
        rutaRepository.save(ruta);

        log.info("Ruta {} asignada a la solicitud {}", ruta.getId(), request.solicitudId());
        ordersClient.actualizarEstado(request.solicitudId(), "PROGRAMADA");

        return LogisticsMapper.toRutaResponse(ruta);
    }

    public Ruta obtenerRuta(Long rutaId) {
        return rutaRepository.findById(rutaId)
                .orElseThrow(() -> new NotFoundException("Ruta " + rutaId + " no encontrada"));
    }

    public Optional<RutaResponse> obtenerRutaPorSolicitud(Long solicitudId) {
        return rutaRepository.findBySolicitudId(solicitudId)
                .map(LogisticsMapper::toRutaResponse);
    }

    private ResolvedLocation resolveLocation(LocationPointRequest location, DepositStopRequest depositStop) {
        if (depositStop != null) {
            Deposito deposito = depositoRepository.findById(depositStop.depositoId())
                    .orElseThrow(() -> new NotFoundException(
                            "Depósito " + depositStop.depositoId() + " no encontrado"));
            return new ResolvedLocation(LocationType.DEPOSITO, deposito.getId(), deposito.getLat(), deposito.getLng(),
                    deposito.getNombre(), deposito);
        }

        if (location == null) {
            throw new BusinessException("Ubicación inválida");
        }

        if (location.tipo() == LocationType.DEPOSITO) {
            if (location.referenciaId() == null) {
                throw new BusinessException("Se requiere referencia de depósito");
            }
            Deposito deposito = depositoRepository.findById(location.referenciaId())
                    .orElseThrow(() -> new NotFoundException(
                            "Depósito " + location.referenciaId() + " no encontrado"));
            return new ResolvedLocation(LocationType.DEPOSITO, deposito.getId(), deposito.getLat(), deposito.getLng(),
                    deposito.getNombre(), deposito);
        }

        if (location.lat() == null || location.lng() == null) {
            throw new BusinessException("Se requieren coordenadas para la ubicación " + location.tipo());
        }

        String descripcion = location.descripcion() != null ? location.descripcion() : location.tipo().name();
        return new ResolvedLocation(location.tipo(), location.referenciaId(), location.lat(), location.lng(),
                descripcion, null);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private record ResolvedLocation(LocationType tipo, Long referenciaId, double lat, double lng,
            String descripcion, Deposito deposito) {
    }
}
