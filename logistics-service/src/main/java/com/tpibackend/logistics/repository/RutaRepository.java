package com.tpibackend.logistics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tpibackend.logistics.model.Ruta;

public interface RutaRepository extends JpaRepository<Ruta, Long> {

    Optional<Ruta> findBySolicitudId(Long solicitudId);
}
