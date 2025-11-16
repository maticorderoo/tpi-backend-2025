package com.tpibackend.orders.service;

import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar las transiciones de estado de Contenedor y Solicitud.
 * Implementa reglas de negocio para derivar estados automáticamente según eventos.
 *
 * Nota: el estado de la solicitud se modela a través de SolicitudEvento y no mediante
 * un campo de estado en la entidad Solicitud.
 */
@Service
public class EstadoService {

    /**
     * Inicializa el estado de un contenedor recién creado.
     * Estado inicial: BORRADOR
     */
    public void inicializarEstados(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoContenedor(contenedor, ContenedorEstado.BORRADOR, usuario);
    }

    /**
     * Actualiza estados cuando se confirma un plan de ruta.
     * Contenedor: BORRADOR -> PROGRAMADA
     */
    public void confirmarPlan(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoContenedor(contenedor, ContenedorEstado.PROGRAMADA, usuario);
    }

    /**
     * Actualiza estado del contenedor cuando inicia el primer tramo de retiro.
     * Contenedor: PROGRAMADA -> EN_RETIRO
     */
    public void iniciarRetiro(Contenedor contenedor, String usuario) {
        if (contenedor.getEstado() == ContenedorEstado.PROGRAMADA) {
            actualizarEstadoContenedor(contenedor, ContenedorEstado.EN_RETIRO, usuario);
        }
    }

    /**
     * Actualiza estado del contenedor cuando inicia un tramo de viaje.
     * Contenedor: EN_RETIRO/EN_DEPOSITO -> EN_VIAJE
     */
    public void iniciarViaje(Contenedor contenedor, String usuario) {
        if (contenedor.getEstado() == ContenedorEstado.EN_RETIRO
            || contenedor.getEstado() == ContenedorEstado.EN_DEPOSITO) {
            actualizarEstadoContenedor(contenedor, ContenedorEstado.EN_VIAJE, usuario);
        }
    }

    /**
     * Actualiza estado del contenedor cuando inicia un tramo de depósito.
     * Contenedor: EN_VIAJE -> EN_DEPOSITO
     */
    public void iniciarDeposito(Contenedor contenedor, String usuario) {
        if (contenedor.getEstado() == ContenedorEstado.EN_VIAJE) {
            actualizarEstadoContenedor(contenedor, ContenedorEstado.EN_DEPOSITO, usuario);
        }
    }

    /**
     * Actualiza estados cuando se finaliza el último tramo (entrega).
     * Contenedor: EN_VIAJE/EN_DEPOSITO -> ENTREGADO
     */
    public void finalizarEntrega(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoContenedor(contenedor, ContenedorEstado.ENTREGADO, usuario);
    }

    /**
     * Cancela una solicitud y contenedor.
     * Contenedor: * -> CANCELADO
     */
    public void cancelarSolicitud(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoContenedor(contenedor, ContenedorEstado.CANCELADO, usuario);
    }

    /**
     * Permite actualización manual de estado del contenedor (solo OPERADOR).
     */
    public void actualizarEstadoManual(Contenedor contenedor, ContenedorEstado nuevoEstado, String usuario) {
        actualizarEstadoContenedor(contenedor, nuevoEstado, usuario);
    }

    private void actualizarEstadoContenedor(Contenedor contenedor, ContenedorEstado nuevoEstado, String usuario) {
        contenedor.setEstado(nuevoEstado);
        contenedor.setUpdatedAt(OffsetDateTime.now());
        contenedor.setUpdatedBy(usuario);
    }
}

