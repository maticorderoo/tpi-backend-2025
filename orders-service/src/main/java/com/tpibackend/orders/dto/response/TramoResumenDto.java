package com.tpibackend.orders.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TramoResumenDto {
    Long id;
    Long rutaId;
    String origenTipo;
    Long origenId;
    String destinoTipo;
    Long destinoId;
    String tipo;
    String estado;
    BigDecimal costoAprox;
    BigDecimal costoReal;
    OffsetDateTime fechaHoraInicio;
    OffsetDateTime fechaHoraFin;
    Long camionId;
    Double distanciaKmEstimada;
    Double distanciaKmReal;
    Long tiempoEstimadoMinutos;
    Long tiempoRealMinutos;
    Integer diasEstadia;
    BigDecimal costoEstadiaDia;
    BigDecimal costoEstadia;
}
