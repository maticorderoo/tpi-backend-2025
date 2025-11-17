package com.tpibackend.logistics.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "rutas")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id")
    private Long solicitudId;

    @Column(name = "cant_tramos", nullable = false)
    private Integer cantTramos;

    @Column(name = "cant_depositos", nullable = false)
    private Integer cantDepositos;

    @Column(name = "costo_total_aprox", precision = 14, scale = 2)
    private BigDecimal costoTotalAprox = BigDecimal.ZERO;

    @Column(name = "costo_total_real", precision = 14, scale = 2)
    private BigDecimal costoTotalReal = BigDecimal.ZERO;

    @Column(name = "tiempo_estimado_minutos")
    private Long tiempoEstimadoMinutos = 0L;

    @Column(name = "tiempo_real_minutos")
    private Long tiempoRealMinutos = 0L;

    @Column(name = "peso_total", precision = 12, scale = 2)
    private BigDecimal pesoTotal = BigDecimal.ZERO;

    @Column(name = "volumen_total", precision = 12, scale = 2)
    private BigDecimal volumenTotal = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Tramo> tramos = new ArrayList<>();

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

    public void addTramo(Tramo tramo) {
        tramo.setRuta(this);
        this.tramos.add(tramo);
    }

    public void removeTramo(Tramo tramo) {
        tramo.setRuta(null);
        this.tramos.remove(tramo);
    }

    public BigDecimal calcularCostoTotalAprox() {
        return tramos.stream()
                .map(Tramo::getCostoAprox)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularCostoTotalApproxConGestion(BigDecimal cargoGestionPorTramo) {
        BigDecimal costoTramos = calcularCostoTotalAprox();
        BigDecimal cargoGestion = cargoGestionPorTramo.multiply(BigDecimal.valueOf(this.cantTramos));
        return costoTramos.add(cargoGestion);
    }

    public BigDecimal calcularCostoTotalReal() {
        return tramos.stream()
                .map(Tramo::getCostoReal)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularCostoTotalRealConGestion(BigDecimal cargoGestionPorTramo) {
        BigDecimal costoTramos = calcularCostoTotalReal();
        BigDecimal cargoGestion = cargoGestionPorTramo.multiply(BigDecimal.valueOf(this.cantTramos));
        return costoTramos.add(cargoGestion);
    }

    public long calcularTiempoEstimado() {
        return tramos.stream()
                .map(Tramo::getTiempoEstimadoMinutos)
                .filter(valor -> valor != null)
                .mapToLong(Long::longValue)
                .sum();
    }

    public long calcularTiempoReal() {
        return tramos.stream()
                .map(Tramo::getTiempoRealMinutos)
                .filter(valor -> valor != null)
                .mapToLong(Long::longValue)
                .sum();
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

    public BigDecimal getCostoTotalAprox() {
        return costoTotalAprox;
    }

    public void setCostoTotalAprox(BigDecimal costoTotalAprox) {
        this.costoTotalAprox = costoTotalAprox;
    }

    public BigDecimal getCostoTotalReal() {
        return costoTotalReal;
    }

    public void setCostoTotalReal(BigDecimal costoTotalReal) {
        this.costoTotalReal = costoTotalReal;
    }

    public Long getTiempoEstimadoMinutos() {
        return tiempoEstimadoMinutos;
    }

    public void setTiempoEstimadoMinutos(Long tiempoEstimadoMinutos) {
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
    }

    public Long getTiempoRealMinutos() {
        return tiempoRealMinutos;
    }

    public void setTiempoRealMinutos(Long tiempoRealMinutos) {
        this.tiempoRealMinutos = tiempoRealMinutos;
    }

    public BigDecimal getPesoTotal() {
        return pesoTotal;
    }

    public void setPesoTotal(BigDecimal pesoTotal) {
        this.pesoTotal = pesoTotal;
    }

    public BigDecimal getVolumenTotal() {
        return volumenTotal;
    }

    public void setVolumenTotal(BigDecimal volumenTotal) {
        this.volumenTotal = volumenTotal;
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

    public List<Tramo> getTramos() {
        return tramos;
    }

    public void setTramos(List<Tramo> tramos) {
        this.tramos.clear();
        if (tramos != null) {
            tramos.forEach(this::addTramo);
        }
    }
}
