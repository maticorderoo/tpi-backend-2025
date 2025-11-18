package com.tpibackend.logistics.dto.integration;

import java.math.BigDecimal;

public record SolicitudLogisticaResponse(
        Long id,
        String estadoSolicitud,
        Punto origen,
        Punto destino,
        BigDecimal pesoContenedor,
        BigDecimal volumenContenedor,
        Contenedor contenedor,
        String clienteEmail,
        String clienteIdentificador
) {
    public record Punto(String descripcion, Double latitud, Double longitud) {
    }

    public record Contenedor(Long id, String codigo, String estado) {
    }
}
