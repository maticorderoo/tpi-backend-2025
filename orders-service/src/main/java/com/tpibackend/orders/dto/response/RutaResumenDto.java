package com.tpibackend.orders.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RutaResumenDto {
    Long id;
    Integer cantTramos;
    Integer cantDepositos;
    BigDecimal costoTotalAprox;
    BigDecimal costoTotalReal;
    BigDecimal pesoTotal;
    BigDecimal volumenTotal;
    List<TramoResumenDto> tramos;
}
