package com.tpibackend.fleet.repository;

import com.tpibackend.fleet.model.entity.Camion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {

    Optional<Camion> findByDominioIgnoreCase(String dominio);

    boolean existsByDominioIgnoreCase(String dominio);

    boolean existsByDominioIgnoreCaseAndIdNot(String dominio, Long id);

    List<Camion> findByDisponibleTrue();

    List<Camion> findByDisponibleFalse();
}
