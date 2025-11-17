package com.tpibackend.orders.dto.response;

import com.tpibackend.orders.model.enums.ContenedorEstado;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PendingContainerResponseDto {
    Long solicitudId;
    Long contenedorId;
    Long rutaId;
    Long tramoId;
    String estadoTramo;
    Long depositoDestinoId;
    String depositoDestinoNombre;
    String destinoDescripcion;
    ContenedorEstado estadoContenedor;
}
