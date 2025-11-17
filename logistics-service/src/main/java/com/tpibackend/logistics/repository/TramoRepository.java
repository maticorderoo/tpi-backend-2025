package com.tpibackend.logistics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tpibackend.logistics.model.Tramo;
import com.tpibackend.logistics.model.enums.TramoEstado;

public interface TramoRepository extends JpaRepository<Tramo, Long> {

    List<Tramo> findByEstado(TramoEstado estado);

    List<Tramo> findByEstadoIn(List<TramoEstado> estados);

    List<Tramo> findByCamionIdOrderByRutaIdAsc(Long camionId);

    List<Tramo> findByRutaIdOrderByIdAsc(Long rutaId);
}
