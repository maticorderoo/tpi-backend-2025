package com.tpibackend.logistics.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpibackend.distance.DistanceClient;
import com.tpibackend.distance.model.DistanceData;
import com.tpibackend.logistics.client.FleetClient;
import com.tpibackend.logistics.client.FleetClient.TruckInfo;
import com.tpibackend.logistics.client.OrdersClient;
import com.tpibackend.logistics.dto.request.AsignarCamionRequest;
import com.tpibackend.logistics.dto.request.FinTramoRequest;
import com.tpibackend.logistics.dto.request.InicioTramoRequest;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.exception.BusinessException;
import com.tpibackend.logistics.exception.NotFoundException;
import com.tpibackend.logistics.mapper.LogisticsMapper;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.model.Ruta;
import com.tpibackend.logistics.model.Tramo;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.repository.DepositoRepository;
import com.tpibackend.logistics.repository.TramoRepository;

@Service
@Transactional
public class TramoService {

    private static final Logger log = LoggerFactory.getLogger(TramoService.class);

    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;
    private final FleetClient fleetClient;
    private final OrdersClient ordersClient;
    private final DistanceClient distanceClient;

    public TramoService(TramoRepository tramoRepository,
            DepositoRepository depositoRepository,
            FleetClient fleetClient,
            OrdersClient ordersClient,
            DistanceClient distanceClient) {
        this.tramoRepository = tramoRepository;
        this.depositoRepository = depositoRepository;
        this.fleetClient = fleetClient;
        this.ordersClient = ordersClient;
        this.distanceClient = distanceClient;
    }

    public TramoResponse asignarCamion(Long tramoId, AsignarCamionRequest request) {
        Tramo tramo = obtenerTramo(tramoId);
        if (tramo.getEstado() == TramoEstado.FINALIZADO) {
            throw new BusinessException("El tramo ya fue finalizado");
        }
        TruckInfo camion = fleetClient.obtenerCamion(request.camionId());
        if (!camion.disponible()) {
            throw new BusinessException("El camión " + request.camionId() + " no está disponible");
        }

        Ruta ruta = tramo.getRuta();
        BigDecimal pesoCarga = request.pesoCarga() != null ? request.pesoCarga() : ruta.getPesoTotal();
        BigDecimal volumenCarga = request.volumenCarga() != null ? request.volumenCarga() : ruta.getVolumenTotal();

        if (camion.capacidadPeso() != null && pesoCarga != null
                && camion.capacidadPeso().compareTo(pesoCarga) < 0) {
            throw new BusinessException("El camión no soporta el peso requerido");
        }
        if (camion.capacidadVolumen() != null && volumenCarga != null
                && camion.capacidadVolumen().compareTo(volumenCarga) < 0) {
            throw new BusinessException("El camión no soporta el volumen requerido");
        }

        tramo.setCamionId(request.camionId());
        tramo.setEstado(TramoEstado.ASIGNADO);
        tramoRepository.save(tramo);

        log.info("Camión {} asignado al tramo {}", request.camionId(), tramoId);
        return LogisticsMapper.toTramoResponse(tramo);
    }

    public TramoResponse iniciarTramo(Long tramoId, InicioTramoRequest request) {
        Tramo tramo = obtenerTramo(tramoId);
        if (tramo.getCamionId() == null) {
            throw new BusinessException("El tramo no tiene camión asignado");
        }
        if (tramo.getEstado() != TramoEstado.ASIGNADO) {
            throw new BusinessException("El tramo no puede iniciarse en estado " + tramo.getEstado());
        }

        OffsetDateTime inicio = request != null && request.fechaHoraInicio() != null
                ? request.fechaHoraInicio()
                : OffsetDateTime.now();
        tramo.setEstado(TramoEstado.INICIADO);
        tramo.setFechaHoraInicio(inicio);
        tramoRepository.save(tramo);

        Ruta ruta = tramo.getRuta();
        if (ruta.getSolicitudId() != null) {
            ordersClient.actualizarEstado(ruta.getSolicitudId(), "EN_TRANSITO");
        }

        log.info("Tramo {} iniciado", tramoId);
        return LogisticsMapper.toTramoResponse(tramo);
    }

    public TramoResponse finalizarTramo(Long tramoId, FinTramoRequest request) {
        Tramo tramo = obtenerTramo(tramoId);
        if (tramo.getEstado() != TramoEstado.INICIADO) {
            throw new BusinessException("El tramo no puede finalizarse en estado " + tramo.getEstado());
        }
        if (tramo.getFechaHoraInicio() == null) {
            throw new BusinessException("El tramo no tiene fecha de inicio registrada");
        }

        tramo.setFechaHoraFin(request.fechaHoraFin() != null ? request.fechaHoraFin() : OffsetDateTime.now());
        double distanciaReal = resolverDistanciaReal(tramo, request);
        tramo.setDistanciaKmReal(distanciaReal);
        tramo.setDiasEstadia(request.diasEstadia());

        BigDecimal costoEstadiaDia = tramo.getCostoEstadiaDia();
        if (tramo.getDestinoTipo() == LocationType.DEPOSITO && tramo.getDestinoId() != null) {
            Deposito deposito = depositoRepository.findById(tramo.getDestinoId()).orElse(null);
            if (deposito != null) {
                costoEstadiaDia = deposito.getCostoEstadiaDia();
                tramo.setCostoEstadiaDia(costoEstadiaDia);
            }
        }

        BigDecimal distancia = BigDecimal.valueOf(distanciaReal);
        BigDecimal costoEstadia = costoEstadiaDia.multiply(BigDecimal.valueOf(request.diasEstadia()));
        tramo.setCostoEstadia(costoEstadia);

        BigDecimal costoReal = request.costoKmBase().multiply(distancia)
                .add(request.consumoLitrosKm().multiply(distancia).multiply(request.precioCombustible()))
                .add(costoEstadia);
        tramo.setCostoReal(costoReal);
        tramo.setEstado(TramoEstado.FINALIZADO);
        tramoRepository.save(tramo);

        Ruta ruta = tramo.getRuta();
        ruta.setCostoTotalReal(ruta.calcularCostoTotalReal());

        log.info("Tramo {} finalizado con costo {}", tramoId, costoReal);

        if (ruta.getSolicitudId() != null &&
                ruta.getTramos().stream().allMatch(t -> t.getEstado() == TramoEstado.FINALIZADO)) {
            ordersClient.actualizarEstado(ruta.getSolicitudId(), "ENTREGADA");
            ordersClient.actualizarCosto(ruta.getSolicitudId(), ruta.getCostoTotalReal());
        }

        return LogisticsMapper.toTramoResponse(tramo);
    }

    private Tramo obtenerTramo(Long tramoId) {
        return tramoRepository.findById(tramoId)
                .orElseThrow(() -> new NotFoundException("Tramo " + tramoId + " no encontrado"));
    }

    private double resolverDistanciaReal(Tramo tramo, FinTramoRequest request) {
        double fallback = request.kmReal() != null ? request.kmReal()
                : tramo.getDistanciaKmEstimada() != null ? tramo.getDistanciaKmEstimada() : 0d;

        if (tramo.getOrigenLat() == null || tramo.getOrigenLng() == null
                || tramo.getDestinoLat() == null || tramo.getDestinoLng() == null) {
            log.warn("Tramo {} sin coordenadas persistidas, usando distancia alternativa {} km", tramo.getId(), fallback);
            return fallback;
        }

        try {
            DistanceData data = distanceClient.getDistance(
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
}
