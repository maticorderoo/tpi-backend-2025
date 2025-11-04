package com.tpibackend.logistics.mapper;

import java.util.List;

import com.tpibackend.logistics.dto.response.DepositoResponse;
import com.tpibackend.logistics.dto.response.RutaResponse;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.model.Ruta;
import com.tpibackend.logistics.model.Tramo;

public final class LogisticsMapper {

    private LogisticsMapper() {
    }

    public static RutaResponse toRutaResponse(Ruta ruta) {
        List<TramoResponse> tramos = ruta.getTramos().stream()
                .map(LogisticsMapper::toTramoResponse)
                .toList();
        return new RutaResponse(
                ruta.getId(),
                ruta.getSolicitudId(),
                ruta.getCantTramos(),
                ruta.getCantDepositos(),
                ruta.getCostoTotalAprox(),
                ruta.getCostoTotalReal(),
                ruta.getPesoTotal(),
                ruta.getVolumenTotal(),
                tramos);
    }

    public static TramoResponse toTramoResponse(Tramo tramo) {
        return new TramoResponse(
                tramo.getId(),
                tramo.getRuta() != null ? tramo.getRuta().getId() : null,
                tramo.getOrigenTipo(),
                tramo.getOrigenId(),
                tramo.getDestinoTipo(),
                tramo.getDestinoId(),
                tramo.getTipo(),
                tramo.getEstado(),
                tramo.getCostoAprox(),
                tramo.getCostoReal(),
                tramo.getFechaHoraInicio(),
                tramo.getFechaHoraFin(),
                tramo.getCamionId(),
                tramo.getDistanciaKmEstimada(),
                tramo.getDistanciaKmReal(),
                tramo.getDiasEstadia(),
                tramo.getCostoEstadiaDia(),
                tramo.getCostoEstadia());
    }

    public static DepositoResponse toDepositoResponse(Deposito deposito) {
        return new DepositoResponse(
                deposito.getId(),
                deposito.getNombre(),
                deposito.getDireccion(),
                deposito.getLat(),
                deposito.getLng(),
                deposito.getCostoEstadiaDia());
    }
}
