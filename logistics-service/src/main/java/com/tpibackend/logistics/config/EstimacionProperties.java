package com.tpibackend.logistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "logistics.estimaciones")
public class EstimacionProperties {

    private int diasEstadiaDeposito;

    public int getDiasEstadiaDeposito() {
        return diasEstadiaDeposito;
    }

    public void setDiasEstadiaDeposito(int diasEstadiaDeposito) {
        this.diasEstadiaDeposito = diasEstadiaDeposito;
    }
}
