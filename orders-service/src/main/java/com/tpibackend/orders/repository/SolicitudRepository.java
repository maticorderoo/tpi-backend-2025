package com.tpibackend.orders.repository;

import com.tpibackend.orders.model.Solicitud;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @EntityGraph(attributePaths = {"cliente", "contenedor"})
    Optional<Solicitud> findById(Long id);

    @EntityGraph(attributePaths = {"cliente", "contenedor"})
    Optional<Solicitud> findByContenedorId(Long contenedorId);

    @Override
    @EntityGraph(attributePaths = {"cliente", "contenedor"})
    List<Solicitud> findAll();

    @EntityGraph(attributePaths = {"cliente", "contenedor"})
    List<Solicitud> findAllByClienteCuit(String cuit);
}
