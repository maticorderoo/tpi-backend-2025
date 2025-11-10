package com.tpibackend.orders.model;

import com.tpibackend.orders.model.enums.SolicitudEstado;
import com.tpibackend.orders.model.history.SolicitudEvento;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private SolicitudEstado estado;

    private BigDecimal costoEstimado;

    private Long tiempoEstimadoMinutos;

    private BigDecimal costoFinal;

    private Long tiempoRealMinutos;

    private BigDecimal estadiaEstimada;

    @Column(length = 255)
    private String origen;

    @Column(length = 255)
    private String destino;

    @Column(nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitudEvento> eventos = new ArrayList<>();

    public void agregarEvento(SolicitudEvento evento) {
        eventos.add(evento);
        evento.setSolicitud(this);
    }
}
