package com.tpibackend.orders.repository;

import com.tpibackend.orders.model.Contenedor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {

    Optional<Contenedor> findByCodigoAndClienteId(String codigo, Long clienteId);
}
