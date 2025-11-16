package com.tpibackend.orders.model;

import com.tpibackend.orders.model.enums.ContenedorEstado;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "contenedores")
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(nullable = false)
    private BigDecimal peso;

    @Column(nullable = false)
    private BigDecimal volumen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContenedorEstado estado = ContenedorEstado.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(mappedBy = "contenedor", fetch = FetchType.LAZY)
    private Solicitud solicitudActiva;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
