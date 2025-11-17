package com.tpibackend.orders.dto.response;

import java.math.BigDecimal;

public record SolicitudLogisticsView(
        Long id,
        String estadoSolicitud,
        Punto origen,
        Punto destino,
        BigDecimal pesoContenedor,
        BigDecimal volumenContenedor,
        Contenedor contenedor
) {
    public record Punto(String descripcion, Double latitud, Double longitud) {
    }

    public record Contenedor(Long id, String codigo, String estado) {
    }
}
