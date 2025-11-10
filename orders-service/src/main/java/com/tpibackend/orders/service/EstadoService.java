package com.tpibackend.orders.service;

import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import com.tpibackend.orders.model.enums.SolicitudEstado;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar las transiciones de estado de Contenedor y Solicitud.
 * Implementa reglas de negocio para derivar estados automáticamente según eventos.
 */
@Service
public class EstadoService {

    /**
     * Inicializa el estado de un contenedor y solicitud recién creados.
     * Estado inicial: BORRADOR
     */
    public void inicializarEstados(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoContenedor(contenedor, ContenedorEstado.BORRADOR, usuario);
        actualizarEstadoSolicitud(solicitud, SolicitudEstado.BORRADOR, usuario);
    }

    /**
     * Actualiza estados cuando se confirma un plan de ruta.
     * Solicitud: BORRADOR → PROGRAMADA
     * Contenedor: BORRADOR → PROGRAMADA
     */
    public void confirmarPlan(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoSolicitud(solicitud, SolicitudEstado.PROGRAMADA, usuario);
        actualizarEstadoContenedor(contenedor, ContenedorEstado.PROGRAMADA, usuario);
    }

    /**
     * Actualiza estado del contenedor cuando inicia el primer tramo de retiro.
     * Contenedor: PROGRAMADA → EN_RETIRO
     */
    public void iniciarRetiro(Contenedor contenedor, String usuario) {
        if (contenedor.getEstado() == ContenedorEstado.PROGRAMADA) {
            actualizarEstadoContenedor(contenedor, ContenedorEstado.EN_RETIRO, usuario);
        }
    }

    /**
     * Actualiza estado del contenedor cuando inicia un tramo de viaje.
     * Contenedor: EN_RETIRO → EN_VIAJE (o permanece EN_VIAJE)
     */
    public void iniciarViaje(Contenedor contenedor, String usuario) {
        if (contenedor.getEstado() == ContenedorEstado.EN_RETIRO || 
            contenedor.getEstado() == ContenedorEstado.EN_DEPOSITO) {
            actualizarEstadoContenedor(contenedor, ContenedorEstado.EN_VIAJE, usuario);
        }
    }

    /**
     * Actualiza estado del contenedor cuando inicia un tramo de depósito.
     * Contenedor: EN_VIAJE → EN_DEPOSITO
     */
    public void iniciarDeposito(Contenedor contenedor, String usuario) {
        if (contenedor.getEstado() == ContenedorEstado.EN_VIAJE) {
            actualizarEstadoContenedor(contenedor, ContenedorEstado.EN_DEPOSITO, usuario);
        }
    }

    /**
     * Actualiza estados cuando se finaliza el último tramo (entrega).
     * Contenedor: EN_VIAJE/EN_DEPOSITO → ENTREGADO
     * Solicitud: PROGRAMADA → COMPLETADA
     */
    public void finalizarEntrega(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoContenedor(contenedor, ContenedorEstado.ENTREGADO, usuario);
        actualizarEstadoSolicitud(solicitud, SolicitudEstado.COMPLETADA, usuario);
    }

    /**
     * Cancela una solicitud y contenedor.
     * Solicitud: * → CANCELADA
     * Contenedor: * → CANCELADO
     */
    public void cancelarSolicitud(Contenedor contenedor, Solicitud solicitud, String usuario) {
        actualizarEstadoContenedor(contenedor, ContenedorEstado.CANCELADO, usuario);
        actualizarEstadoSolicitud(solicitud, SolicitudEstado.CANCELADA, usuario);
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

    private void actualizarEstadoSolicitud(Solicitud solicitud, SolicitudEstado nuevoEstado, String usuario) {
        solicitud.setEstado(nuevoEstado);
        solicitud.setUpdatedAt(OffsetDateTime.now());
        solicitud.setUpdatedBy(usuario);
    }
}
