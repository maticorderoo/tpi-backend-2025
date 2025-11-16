package com.tpibackend.orders.dto.response;

import com.tpibackend.orders.model.enums.ContenedorEstado;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ContenedorResponseDto {
    Long id;
    String codigo;
    BigDecimal peso;
    BigDecimal volumen;
    ContenedorEstado estado;
}
