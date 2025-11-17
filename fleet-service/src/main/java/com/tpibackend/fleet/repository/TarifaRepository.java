package com.tpibackend.fleet.repository;

import com.tpibackend.fleet.model.entity.Tarifa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Optional<Tarifa> findTopByOrderByCreatedAtDescIdDesc();
}
