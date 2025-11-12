package com.tpibackend.logistics.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.model.enums.TramoTipo;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "tramos")
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_tipo", length = 30)
    private LocationType origenTipo;

    @Column(name = "origen_id")
    private Long origenId;

    @Column(name = "origen_lat")
    private Double origenLat;

    @Column(name = "origen_lng")
    private Double origenLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino_tipo", length = 30)
    private LocationType destinoTipo;

    @Column(name = "destino_id")
    private Long destinoId;

    @Column(name = "destino_lat")
    private Double destinoLat;

    @Column(name = "destino_lng")
    private Double destinoLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TramoTipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private TramoEstado estado = TramoEstado.ESTIMADO;

    @Column(name = "costo_aprox", precision = 14, scale = 2)
    private BigDecimal costoAprox = BigDecimal.ZERO;

    @Column(name = "costo_real", precision = 14, scale = 2)
    private BigDecimal costoReal = BigDecimal.ZERO;

    @Column(name = "fecha_hora_inicio_estimada")
    private OffsetDateTime fechaHoraInicioEstimada;

    @Column(name = "fecha_hora_fin_estimada")
    private OffsetDateTime fechaHoraFinEstimada;

    @Column(name = "fecha_hora_inicio")
    private OffsetDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private OffsetDateTime fechaHoraFin;

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "distancia_km_estimada")
    private Double distanciaKmEstimada;

    @Column(name = "distancia_km_real")
    private Double distanciaKmReal;

    @Column(name = "dias_estadia")
    private Integer diasEstadia = 0;

    @Column(name = "costo_estadia_dia", precision = 12, scale = 2)
    private BigDecimal costoEstadiaDia = BigDecimal.ZERO;

    @Column(name = "costo_estadia", precision = 12, scale = 2)
    private BigDecimal costoEstadia = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public LocationType getOrigenTipo() {
        return origenTipo;
    }

    public void setOrigenTipo(LocationType origenTipo) {
        this.origenTipo = origenTipo;
    }

    public Long getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Long origenId) {
        this.origenId = origenId;
    }

    public Double getOrigenLat() {
        return origenLat;
    }

    public void setOrigenLat(Double origenLat) {
        this.origenLat = origenLat;
    }

    public Double getOrigenLng() {
        return origenLng;
    }

    public void setOrigenLng(Double origenLng) {
        this.origenLng = origenLng;
    }

    public LocationType getDestinoTipo() {
        return destinoTipo;
    }

    public void setDestinoTipo(LocationType destinoTipo) {
        this.destinoTipo = destinoTipo;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(Long destinoId) {
        this.destinoId = destinoId;
    }

    public Double getDestinoLat() {
        return destinoLat;
    }

    public void setDestinoLat(Double destinoLat) {
        this.destinoLat = destinoLat;
    }

    public Double getDestinoLng() {
        return destinoLng;
    }

    public void setDestinoLng(Double destinoLng) {
        this.destinoLng = destinoLng;
    }

    public TramoTipo getTipo() {
        return tipo;
    }

    public void setTipo(TramoTipo tipo) {
        this.tipo = tipo;
    }

    public TramoEstado getEstado() {
        return estado;
    }

    public void setEstado(TramoEstado estado) {
        this.estado = estado;
    }

    public BigDecimal getCostoAprox() {
        return costoAprox;
    }

    public void setCostoAprox(BigDecimal costoAprox) {
        this.costoAprox = costoAprox;
    }

    public BigDecimal getCostoReal() {
        return costoReal;
    }

    public void setCostoReal(BigDecimal costoReal) {
        this.costoReal = costoReal;
    }

    public OffsetDateTime getFechaHoraInicioEstimada() {
        return fechaHoraInicioEstimada;
    }

    public void setFechaHoraInicioEstimada(OffsetDateTime fechaHoraInicioEstimada) {
        this.fechaHoraInicioEstimada = fechaHoraInicioEstimada;
    }

    public OffsetDateTime getFechaHoraFinEstimada() {
        return fechaHoraFinEstimada;
    }

    public void setFechaHoraFinEstimada(OffsetDateTime fechaHoraFinEstimada) {
        this.fechaHoraFinEstimada = fechaHoraFinEstimada;
    }

    public OffsetDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(OffsetDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public OffsetDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(OffsetDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public Long getCamionId() {
        return camionId;
    }

    public void setCamionId(Long camionId) {
        this.camionId = camionId;
    }

    public Double getDistanciaKmEstimada() {
        return distanciaKmEstimada;
    }

    public void setDistanciaKmEstimada(Double distanciaKmEstimada) {
        this.distanciaKmEstimada = distanciaKmEstimada;
    }

    public Double getDistanciaKmReal() {
        return distanciaKmReal;
    }

    public void setDistanciaKmReal(Double distanciaKmReal) {
        this.distanciaKmReal = distanciaKmReal;
    }

    public Integer getDiasEstadia() {
        return diasEstadia;
    }

    public void setDiasEstadia(Integer diasEstadia) {
        this.diasEstadia = diasEstadia;
    }

    public BigDecimal getCostoEstadiaDia() {
        return costoEstadiaDia;
    }

    public void setCostoEstadiaDia(BigDecimal costoEstadiaDia) {
        this.costoEstadiaDia = costoEstadiaDia;
    }

    public BigDecimal getCostoEstadia() {
        return costoEstadia;
    }

    public void setCostoEstadia(BigDecimal costoEstadia) {
        this.costoEstadia = costoEstadia;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
