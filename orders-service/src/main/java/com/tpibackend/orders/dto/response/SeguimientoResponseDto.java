package com.tpibackend.orders.dto.response;

import com.tpibackend.orders.model.enums.ContenedorEstado;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeguimientoResponseDto {
    Long contenedorId;
    Long solicitudId;
    ContenedorEstado estadoContenedor;
    RutaResumenDto ruta;
}
