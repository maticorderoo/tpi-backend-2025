package com.tpibackend.orders.service;

import com.tpibackend.orders.dto.request.SolicitudCostoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudPlanificacionUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;

public interface SolicitudService {

    SolicitudResponseDto crearSolicitud(SolicitudCreateRequest request);

    SolicitudResponseDto obtenerSolicitud(Long solicitudId);

    SeguimientoResponseDto obtenerSeguimientoPorContenedor(Long contenedorId);

    SolicitudResponseDto actualizarCosto(Long solicitudId, SolicitudCostoUpdateRequest request);

    SolicitudResponseDto actualizarPlanificacion(Long solicitudId, SolicitudPlanificacionUpdateRequest request);
}
