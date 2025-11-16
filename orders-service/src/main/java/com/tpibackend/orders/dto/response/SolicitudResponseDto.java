package com.tpibackend.orders.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SolicitudResponseDto {
    Long id;
    BigDecimal costoEstimado;
    Long tiempoEstimadoMinutos;
    BigDecimal costoFinal;
    Long tiempoRealMinutos;
    BigDecimal estadiaEstimada;
    OffsetDateTime fechaCreacion;
    ClienteResponseDto cliente;
    ContenedorResponseDto contenedor;
    List<SolicitudEventoResponseDto> eventos;
    RutaResumenDto rutaResumen;
}
