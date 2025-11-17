package com.tpibackend.logistics.model;

import java.math.BigDecimal;

import com.tpibackend.logistics.model.enums.LocationType;
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
@Table(name = "tramos_tentativos")
public class TramoTentativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_tentativa_id", nullable = false)
    private RutaTentativa rutaTentativa;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_tipo", length = 30)
    private LocationType origenTipo;

    @Column(name = "origen_id")
    private Long origenId;

    @Column(name = "origen_descripcion")
    private String origenDescripcion;

    @Column(name = "origen_lat")
    private Double origenLat;

    @Column(name = "origen_lng")
    private Double origenLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino_tipo", length = 30)
    private LocationType destinoTipo;

    @Column(name = "destino_id")
    private Long destinoId;

    @Column(name = "destino_descripcion")
    private String destinoDescripcion;

    @Column(name = "destino_lat")
    private Double destinoLat;

    @Column(name = "destino_lng")
    private Double destinoLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 30)
    private TramoTipo tipo;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "tiempo_estimado_minutos")
    private Long tiempoEstimadoMinutos;

    @Column(name = "costo_aproximado", precision = 14, scale = 2)
    private BigDecimal costoAproximado = BigDecimal.ZERO;

    @Column(name = "dias_estadia")
    private Integer diasEstadia = 0;

    @Column(name = "costo_estadia_dia", precision = 12, scale = 2)
    private BigDecimal costoEstadiaDia = BigDecimal.ZERO;

    @Column(name = "costo_estadia", precision = 12, scale = 2)
    private BigDecimal costoEstadia = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RutaTentativa getRutaTentativa() {
        return rutaTentativa;
    }

    public void setRutaTentativa(RutaTentativa rutaTentativa) {
        this.rutaTentativa = rutaTentativa;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
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

    public String getOrigenDescripcion() {
        return origenDescripcion;
    }

    public void setOrigenDescripcion(String origenDescripcion) {
        this.origenDescripcion = origenDescripcion;
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

    public String getDestinoDescripcion() {
        return destinoDescripcion;
    }

    public void setDestinoDescripcion(String destinoDescripcion) {
        this.destinoDescripcion = destinoDescripcion;
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

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Long getTiempoEstimadoMinutos() {
        return tiempoEstimadoMinutos;
    }

    public void setTiempoEstimadoMinutos(Long tiempoEstimadoMinutos) {
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
    }

    public BigDecimal getCostoAproximado() {
        return costoAproximado;
    }

    public void setCostoAproximado(BigDecimal costoAproximado) {
        this.costoAproximado = costoAproximado;
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
}
