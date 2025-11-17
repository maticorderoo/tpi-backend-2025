package com.tpibackend.orders.dto.response;

import java.math.BigDecimal;

public record SolicitudLogisticsView(
        Long id,
        Punto origen,
        Punto destino,
        BigDecimal pesoContenedor,
        BigDecimal volumenContenedor
) {
    public record Punto(String descripcion, Double latitud, Double longitud) {
    }
}
