package com.tpibackend.orders.repository;

import com.tpibackend.orders.model.Solicitud;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @EntityGraph(attributePaths = {"cliente", "contenedor"})
    Optional<Solicitud> findById(Long id);

    @EntityGraph(attributePaths = {"cliente", "contenedor"})
    Optional<Solicitud> findByContenedorId(Long contenedorId);
}
