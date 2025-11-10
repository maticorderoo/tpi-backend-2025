package com.tpibackend.orders.service;

import com.tpibackend.orders.dto.request.ContenedorEstadoUpdateRequest;
import com.tpibackend.orders.dto.response.ContenedorResponseDto;
import com.tpibackend.orders.exception.OrdersNotFoundException;
import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.repository.ContenedorRepository;
import com.tpibackend.orders.repository.SolicitudRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final SolicitudRepository solicitudRepository;
    private final EstadoService estadoService;

    public ContenedorService(
        ContenedorRepository contenedorRepository,
        SolicitudRepository solicitudRepository,
        EstadoService estadoService
    ) {
        this.contenedorRepository = contenedorRepository;
        this.solicitudRepository = solicitudRepository;
        this.estadoService = estadoService;
    }

    /**
     * Actualiza manualmente el estado de un contenedor.
     * Solo disponible para usuarios con rol OPERADOR.
     */
    @Transactional
    public ContenedorResponseDto actualizarEstadoManual(Long solicitudId, ContenedorEstadoUpdateRequest request) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new OrdersNotFoundException("Solicitud no encontrada"));
        
        Contenedor contenedor = solicitud.getContenedor();
        if (contenedor == null) {
            throw new OrdersNotFoundException("La solicitud no tiene un contenedor asociado");
        }

        String usuario = obtenerUsuarioActual();
        estadoService.actualizarEstadoManual(contenedor, request.estado(), usuario);
        
        contenedorRepository.save(contenedor);

        return ContenedorResponseDto.builder()
            .id(contenedor.getId())
            .peso(contenedor.getPeso())
            .volumen(contenedor.getVolumen())
            .estado(contenedor.getEstado())
            .build();
    }

    private String obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
