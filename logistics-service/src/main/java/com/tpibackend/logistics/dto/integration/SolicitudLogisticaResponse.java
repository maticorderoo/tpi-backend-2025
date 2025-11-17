package com.tpibackend.logistics.dto.integration;

import java.math.BigDecimal;

public record SolicitudLogisticaResponse(
        Long id,
        Punto origen,
        Punto destino,
        BigDecimal pesoContenedor,
        BigDecimal volumenContenedor
) {
    public record Punto(String descripcion, Double latitud, Double longitud) {
    }
}
