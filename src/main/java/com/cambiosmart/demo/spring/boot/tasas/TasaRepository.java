package com.cambiosmart.demo.spring.boot.tasas;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TasaRepository extends JpaRepository<Tasas, Long> {

    // Última tasa por fuente/moneda (case-insensitive)
    Optional<Tasas> findFirstByFuenteIgnoreCaseAndMonedaIgnoreCaseOrderByIdDesc(String fuente, String moneda);

    // Coincide con tu entidad: fecha es String
    Optional<Tasas> findByFechaAndFuenteAndMoneda(String fecha, String fuente, String moneda);

    // Variante sin ignore-case si la necesitas
    Optional<Tasas> findFirstByFuenteAndMonedaOrderByIdDesc(String fuente, String moneda);
}


