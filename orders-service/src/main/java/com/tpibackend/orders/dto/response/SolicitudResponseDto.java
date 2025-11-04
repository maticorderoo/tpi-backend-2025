package com.tpibackend.orders.dto.response;

import com.tpibackend.orders.model.enums.SolicitudEstado;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SolicitudResponseDto {
    Long id;
    SolicitudEstado estado;
    BigDecimal costoEstimado;
    Long tiempoEstimadoMinutos;
    BigDecimal costoFinal;
    Long tiempoRealMinutos;
    BigDecimal estadiaEstimada;
    String origen;
    String destino;
    OffsetDateTime fechaCreacion;
    ClienteResponseDto cliente;
    ContenedorResponseDto contenedor;
    List<SolicitudEventoResponseDto> eventos;
}
