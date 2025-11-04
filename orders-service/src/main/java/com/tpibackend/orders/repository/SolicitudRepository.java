package com.tpibackend.orders.repository;

import com.tpibackend.orders.model.Solicitud;
import com.tpibackend.orders.model.enums.SolicitudEstado;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    boolean existsByContenedorIdAndEstadoIn(Long contenedorId, Collection<SolicitudEstado> estados);

    @EntityGraph(attributePaths = {"cliente", "contenedor", "eventos"})
    Optional<Solicitud> findById(Long id);

    @EntityGraph(attributePaths = {"cliente", "contenedor", "eventos"})
    Optional<Solicitud> findByContenedorId(Long contenedorId);
}
