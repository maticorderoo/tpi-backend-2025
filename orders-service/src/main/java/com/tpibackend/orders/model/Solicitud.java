package com.tpibackend.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false, unique = true)
    private Contenedor contenedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContenedorEstado estado;

    private BigDecimal costoEstimado;

    private Long tiempoEstimadoMinutos;

    @Column(name = "ruta_logistica_id")
    private Long rutaLogisticaId;

    private BigDecimal costoFinal;

    private Long tiempoRealMinutos;

    private BigDecimal estadiaEstimada;

    @Column(length = 500)
    private String observaciones;

    @Column(length = 255, nullable = false)
    private String origen;

    @Column(name = "origen_lat", nullable = false)
    private Double origenLat;

    @Column(name = "origen_lng", nullable = false)
    private Double origenLng;

    @Column(length = 255, nullable = false)
    private String destino;

    @Column(name = "destino_lat", nullable = false)
    private Double destinoLat;

    @Column(name = "destino_lng", nullable = false)
    private Double destinoLng;

    @Column(nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

}
