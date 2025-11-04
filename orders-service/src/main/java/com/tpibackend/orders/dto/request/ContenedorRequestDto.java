package com.tpibackend.orders.dto.request;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ContenedorRequestDto {

    private Long id;

    private BigDecimal peso;

    private BigDecimal volumen;

    private String estado;
}
