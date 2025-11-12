package com.cambiosmart.demo.spring.boot.cuentas;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CuentaRepository extends JpaRepository<CuentaBancaria, Long> {

    List<CuentaBancaria> findByOwnerAndMonedaAndActivoTrueOrderByPrincipalDescIdAsc(String owner, Moneda moneda);

    Optional<CuentaBancaria> findByIdAndOwnerAndActivoTrue(Long id, String owner);

    boolean existsByOwnerAndMonedaAndNumeroCuentaAndActivoTrue(String owner, Moneda moneda, String numeroCuenta);

    boolean existsByOwnerAndMonedaAndCciAndActivoTrue(String owner, Moneda moneda, String cci);
}
