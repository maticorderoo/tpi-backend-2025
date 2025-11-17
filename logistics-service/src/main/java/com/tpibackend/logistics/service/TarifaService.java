package com.tpibackend.logistics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.tpibackend.logistics.client.FleetClient;
import com.tpibackend.logistics.client.FleetClient.TarifaActiva;
import com.tpibackend.logistics.config.EstimacionProperties;

@Service
public class TarifaService {

    private final FleetClient fleetClient;
    private final EstimacionProperties estimacionProperties;

    public TarifaService(FleetClient fleetClient, EstimacionProperties estimacionProperties) {
        this.fleetClient = fleetClient;
        this.estimacionProperties = estimacionProperties;
    }

    public TarifaActiva obtenerTarifaActiva() {
        return fleetClient.obtenerTarifaActiva();
    }

    public BigDecimal calcularCostoPorDistancia(double distanciaKm, TarifaActiva tarifa) {
        TarifaActiva datos = requireTarifa(tarifa);
        return defaultZero(datos.costoKm()).multiply(distancia(distanciaKm));
    }

    public BigDecimal calcularCostoPorTiempo(long minutos, TarifaActiva tarifa) {
        if (minutos <= 0) {
            return BigDecimal.ZERO;
        }
        TarifaActiva datos = requireTarifa(tarifa);
        BigDecimal horas = BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return defaultZero(datos.costoHora()).multiply(horas);
    }

    public int diasEstadiaDeposito() {
        return estimacionProperties.getDiasEstadiaDeposito();
    }

    public BigDecimal calcularCostoEstadia(int dias, BigDecimal costoEstadiaDia) {
        if (dias <= 0 || costoEstadiaDia == null) {
            return BigDecimal.ZERO;
        }
        return costoEstadiaDia.multiply(BigDecimal.valueOf(dias));
    }

    private TarifaActiva requireTarifa(TarifaActiva tarifa) {
        if (tarifa == null) {
            throw new IllegalArgumentException("Se requiere una tarifa activa para calcular costos");
        }
        return tarifa;
    }

    private BigDecimal distancia(double distanciaKm) {
        double value = distanciaKm < 0 ? 0d : distanciaKm;
        return BigDecimal.valueOf(value);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
