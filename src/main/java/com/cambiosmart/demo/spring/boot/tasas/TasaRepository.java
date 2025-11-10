package com.cambiosmart.demo.spring.boot.tasas;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TasaRepository extends JpaRepository<Tasas, Long> {

    // Último registro por fuente y moneda (asumimos id creciente)
    Optional<Tasas> findFirstByFuenteAndMonedaOrderByIdDesc(String fuente, String moneda);
}

