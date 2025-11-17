package com.tpibackend.orders.repository;

import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.enums.ContenedorEstado;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {

    Optional<Contenedor> findFirstByCodigoAndEstadoNotIn(String codigo, Collection<ContenedorEstado> estados);
}
