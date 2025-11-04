package com.tpibackend.fleet.service.impl;

import com.tpibackend.fleet.model.dto.FleetMetricsResponse;
import com.tpibackend.fleet.model.entity.Camion;
import com.tpibackend.fleet.repository.CamionRepository;
import com.tpibackend.fleet.service.FleetMetricsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FleetMetricsServiceImpl implements FleetMetricsService {

    private static final BigDecimal DEFAULT_VALUE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final CamionRepository camionRepository;

    public FleetMetricsServiceImpl(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    @Override
    public FleetMetricsResponse obtenerPromedios() {
        List<Camion> disponibles = camionRepository.findByDisponibleTrue();
        if (disponibles.isEmpty()) {
            return new FleetMetricsResponse(DEFAULT_VALUE, DEFAULT_VALUE);
        }
        BigDecimal consumoPromedio = promedio(disponibles.stream().map(Camion::getConsumoLKm).toList());
        BigDecimal costoPromedio = promedio(disponibles.stream().map(Camion::getCostoKmBase).toList());
        return new FleetMetricsResponse(consumoPromedio, costoPromedio);
    }

    private BigDecimal promedio(List<BigDecimal> valores) {
        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
    }
}
