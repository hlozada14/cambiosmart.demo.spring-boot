package com.cambiosmart.demo.spring.boot.operaciones;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperacionRepository extends JpaRepository<Operacion, Long> {
    Optional<Operacion> findByIdAndOwner(Long id, String owner);
}