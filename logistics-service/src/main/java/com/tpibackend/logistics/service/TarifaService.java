package com.tpibackend.logistics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.tpibackend.logistics.config.EstimacionProperties;

@Service
public class TarifaService {

    private final EstimacionProperties estimacionProperties;

    public TarifaService(EstimacionProperties estimacionProperties) {
        this.estimacionProperties = estimacionProperties;
    }

    public BigDecimal costoKmBase() {
        return estimacionProperties.getCostoKmBase();
    }

    public BigDecimal consumoLitrosKm() {
        return estimacionProperties.getConsumoLitrosKm();
    }

    public BigDecimal precioCombustible() {
        return estimacionProperties.getPrecioCombustible();
    }

    public BigDecimal costoTiempoHora() {
        return estimacionProperties.getCostoTiempoHora();
    }

    public int diasEstadiaDeposito() {
        return estimacionProperties.getDiasEstadiaDeposito();
    }

    public BigDecimal calcularCostoBasePorDistancia(double distanciaKm) {
        return costoKmBase().multiply(distancia(distanciaKm));
    }

    public BigDecimal calcularCostoCombustible(double distanciaKm) {
        return consumoLitrosKm()
                .multiply(distancia(distanciaKm))
                .multiply(precioCombustible());
    }

    public BigDecimal calcularCostoTiempo(long minutos) {
        if (minutos <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal horas = BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return costoTiempoHora().multiply(horas);
    }

    public BigDecimal calcularCostoEstadia(int dias, BigDecimal costoEstadiaDia) {
        if (dias <= 0 || costoEstadiaDia == null) {
            return BigDecimal.ZERO;
        }
        return costoEstadiaDia.multiply(BigDecimal.valueOf(dias));
    }

    private BigDecimal distancia(double distanciaKm) {
        double value = distanciaKm < 0 ? 0d : distanciaKm;
        return BigDecimal.valueOf(value);
    }
}
