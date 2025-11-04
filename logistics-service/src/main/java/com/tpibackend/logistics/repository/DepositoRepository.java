package com.tpibackend.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tpibackend.logistics.model.Deposito;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {
}
