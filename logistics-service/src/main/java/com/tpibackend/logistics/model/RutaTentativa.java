package com.tpibackend.logistics.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tpibackend.logistics.model.enums.RutaTentativaEstado;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "rutas_tentativas")
public class RutaTentativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(name = "cant_tramos", nullable = false)
    private Integer cantTramos;

    @Column(name = "cant_depositos", nullable = false)
    private Integer cantDepositos;

    @Column(name = "distancia_total_km")
    private Double distanciaTotalKm;

    @Column(name = "costo_total_aprox", precision = 14, scale = 2)
    private BigDecimal costoTotalAprox = BigDecimal.ZERO;

    @Column(name = "tiempo_estimado_minutos")
    private Long tiempoEstimadoMinutos = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private RutaTentativaEstado estado = RutaTentativaEstado.GENERADA;

    @Column(name = "ruta_definitiva_id")
    private Long rutaDefinitivaId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "rutaTentativa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TramoTentativo> tramos = new ArrayList<>();

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void addTramo(TramoTentativo tramo) {
        tramo.setRutaTentativa(this);
        this.tramos.add(tramo);
    }

    public void clearTramos() {
        this.tramos.forEach(tramo -> tramo.setRutaTentativa(null));
        this.tramos.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public Integer getCantTramos() {
        return cantTramos;
    }

    public void setCantTramos(Integer cantTramos) {
        this.cantTramos = cantTramos;
    }

    public Integer getCantDepositos() {
        return cantDepositos;
    }

    public void setCantDepositos(Integer cantDepositos) {
        this.cantDepositos = cantDepositos;
    }

    public Double getDistanciaTotalKm() {
        return distanciaTotalKm;
    }

    public void setDistanciaTotalKm(Double distanciaTotalKm) {
        this.distanciaTotalKm = distanciaTotalKm;
    }

    public BigDecimal getCostoTotalAprox() {
        return costoTotalAprox;
    }

    public void setCostoTotalAprox(BigDecimal costoTotalAprox) {
        this.costoTotalAprox = costoTotalAprox;
    }

    public Long getTiempoEstimadoMinutos() {
        return tiempoEstimadoMinutos;
    }

    public void setTiempoEstimadoMinutos(Long tiempoEstimadoMinutos) {
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
    }

    public RutaTentativaEstado getEstado() {
        return estado;
    }

    public void setEstado(RutaTentativaEstado estado) {
        this.estado = estado;
    }

    public Long getRutaDefinitivaId() {
        return rutaDefinitivaId;
    }

    public void setRutaDefinitivaId(Long rutaDefinitivaId) {
        this.rutaDefinitivaId = rutaDefinitivaId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TramoTentativo> getTramos() {
        return tramos;
    }

    public void setTramos(List<TramoTentativo> tramos) {
        this.tramos.clear();
        if (tramos != null) {
            tramos.forEach(this::addTramo);
        }
    }
}
