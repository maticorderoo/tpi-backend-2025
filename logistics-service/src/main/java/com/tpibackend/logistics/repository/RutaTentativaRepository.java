package com.tpibackend.logistics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tpibackend.logistics.model.RutaTentativa;

public interface RutaTentativaRepository extends JpaRepository<RutaTentativa, Long> {

    List<RutaTentativa> findBySolicitudIdOrderByCreatedAtAsc(Long solicitudId);

    Optional<RutaTentativa> findByIdAndSolicitudId(Long id, Long solicitudId);

    void deleteBySolicitudId(Long solicitudId);
}
