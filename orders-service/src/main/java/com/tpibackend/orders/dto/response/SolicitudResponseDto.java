package com.tpibackend.orders.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;
import com.tpibackend.orders.model.enums.ContenedorEstado;

@Value
@Builder(toBuilder = true)
public class SolicitudResponseDto {
    Long id;
    ContenedorEstado estado;
    BigDecimal costoEstimado;
    Long tiempoEstimadoMinutos;
    BigDecimal costoFinal;
    Long tiempoRealMinutos;
    BigDecimal estadiaEstimada;
    String observaciones;
    String origen;
    Double origenLat;
    Double origenLng;
    String destino;
    Double destinoLat;
    Double destinoLng;
    OffsetDateTime fechaCreacion;
    ClienteResponseDto cliente;
    ContenedorResponseDto contenedor;
    RutaResumenDto rutaResumen;
}
