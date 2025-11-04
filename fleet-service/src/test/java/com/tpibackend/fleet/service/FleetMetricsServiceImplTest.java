package com.tpibackend.fleet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.tpibackend.fleet.model.dto.FleetMetricsResponse;
import com.tpibackend.fleet.model.entity.Camion;
import com.tpibackend.fleet.repository.CamionRepository;
import com.tpibackend.fleet.service.impl.FleetMetricsServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FleetMetricsServiceImplTest {

    @Mock
    private CamionRepository camionRepository;

    @InjectMocks
    private FleetMetricsServiceImpl fleetMetricsService;

    @Test
    void obtenerPromedios_whenNoCamionesDisponibles_shouldReturnCeros() {
        when(camionRepository.findByDisponibleTrue()).thenReturn(List.of());

        FleetMetricsResponse response = fleetMetricsService.obtenerPromedios();

        assertEquals(BigDecimal.ZERO.setScale(2), response.consumoPromedio());
        assertEquals(BigDecimal.ZERO.setScale(2), response.costoKmPromedio());
    }

    @Test
    void obtenerPromedios_shouldCalculateAverage() {
        Camion camion1 = buildCamion(BigDecimal.valueOf(100), BigDecimal.valueOf(5));
        Camion camion2 = buildCamion(BigDecimal.valueOf(140), BigDecimal.valueOf(7));
        when(camionRepository.findByDisponibleTrue()).thenReturn(List.of(camion1, camion2));

        FleetMetricsResponse response = fleetMetricsService.obtenerPromedios();

        assertEquals(BigDecimal.valueOf(6.00).setScale(2), response.consumoPromedio());
        assertEquals(BigDecimal.valueOf(120.00).setScale(2), response.costoKmPromedio());
    }

    private Camion buildCamion(BigDecimal costo, BigDecimal consumo) {
        Camion camion = new Camion();
        camion.setCostoKmBase(costo);
        camion.setConsumoLKm(consumo);
        return camion;
    }
}
