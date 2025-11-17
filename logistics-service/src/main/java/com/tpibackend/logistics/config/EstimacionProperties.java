package com.tpibackend.logistics.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "logistics.estimaciones")
public class EstimacionProperties {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal costoKmBase;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal consumoLitrosKm;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal precioCombustible;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costoTiempoHora;

    private int diasEstadiaDeposito;

    public BigDecimal getCostoKmBase() {
        return costoKmBase;
    }

    public void setCostoKmBase(BigDecimal costoKmBase) {
        this.costoKmBase = costoKmBase;
    }

    public BigDecimal getConsumoLitrosKm() {
        return consumoLitrosKm;
    }

    public void setConsumoLitrosKm(BigDecimal consumoLitrosKm) {
        this.consumoLitrosKm = consumoLitrosKm;
    }

    public BigDecimal getPrecioCombustible() {
        return precioCombustible;
    }

    public void setPrecioCombustible(BigDecimal precioCombustible) {
        this.precioCombustible = precioCombustible;
    }

    public BigDecimal getCostoTiempoHora() {
        return costoTiempoHora;
    }

    public void setCostoTiempoHora(BigDecimal costoTiempoHora) {
        this.costoTiempoHora = costoTiempoHora;
    }

    public int getDiasEstadiaDeposito() {
        return diasEstadiaDeposito;
    }

    public void setDiasEstadiaDeposito(int diasEstadiaDeposito) {
        this.diasEstadiaDeposito = diasEstadiaDeposito;
    }
}
