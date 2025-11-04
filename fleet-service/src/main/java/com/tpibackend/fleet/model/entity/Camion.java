package com.tpibackend.fleet.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "camiones", uniqueConstraints = @UniqueConstraint(name = "uk_camiones_dominio", columnNames = "dominio"))
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String dominio;

    @Column(name = "transportista_nombre", nullable = false)
    private String transportistaNombre;

    @Column(nullable = false)
    private String telefono;

    @Column(name = "cap_peso", nullable = false, precision = 12, scale = 2)
    private BigDecimal capPeso;

    @Column(name = "cap_volumen", nullable = false, precision = 12, scale = 2)
    private BigDecimal capVolumen;

    @Column(nullable = false)
    private Boolean disponible;

    @Column(name = "costo_km_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoKmBase;

    @Column(name = "consumo_l_km", nullable = false, precision = 12, scale = 2)
    private BigDecimal consumoLKm;

    public Camion() {
    }

    public Camion(Long id, String dominio, String transportistaNombre, String telefono, BigDecimal capPeso,
                  BigDecimal capVolumen, Boolean disponible, BigDecimal costoKmBase, BigDecimal consumoLKm) {
        this.id = id;
        this.dominio = dominio;
        this.transportistaNombre = transportistaNombre;
        this.telefono = telefono;
        this.capPeso = capPeso;
        this.capVolumen = capVolumen;
        this.disponible = disponible;
        this.costoKmBase = costoKmBase;
        this.consumoLKm = consumoLKm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public String getTransportistaNombre() {
        return transportistaNombre;
    }

    public void setTransportistaNombre(String transportistaNombre) {
        this.transportistaNombre = transportistaNombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public BigDecimal getCapPeso() {
        return capPeso;
    }

    public void setCapPeso(BigDecimal capPeso) {
        this.capPeso = capPeso;
    }

    public BigDecimal getCapVolumen() {
        return capVolumen;
    }

    public void setCapVolumen(BigDecimal capVolumen) {
        this.capVolumen = capVolumen;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public BigDecimal getCostoKmBase() {
        return costoKmBase;
    }

    public void setCostoKmBase(BigDecimal costoKmBase) {
        this.costoKmBase = costoKmBase;
    }

    public BigDecimal getConsumoLKm() {
        return consumoLKm;
    }

    public void setConsumoLKm(BigDecimal consumoLKm) {
        this.consumoLKm = consumoLKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Camion camion = (Camion) o;
        return Objects.equals(id, camion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
