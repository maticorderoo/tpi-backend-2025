package com.tpibackend.orders.dto.response;

import com.tpibackend.orders.model.enums.SolicitudEstado;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SolicitudEventoResponseDto {
    SolicitudEstado estado;
    OffsetDateTime fechaEvento;
    String descripcion;
}
