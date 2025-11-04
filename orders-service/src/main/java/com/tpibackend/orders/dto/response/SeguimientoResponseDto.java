package com.tpibackend.orders.dto.response;

import com.tpibackend.orders.model.enums.SolicitudEstado;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeguimientoResponseDto {
    Long contenedorId;
    Long solicitudId;
    SolicitudEstado estadoActual;
    List<SolicitudEventoResponseDto> eventos;
}
