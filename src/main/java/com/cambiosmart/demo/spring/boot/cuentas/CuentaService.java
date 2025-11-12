package com.cambiosmart.demo.spring.boot.cuentas;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository repo;

    // ===== Helpers simples =====
    private static String onlyDigits(String s) { return s == null ? null : s.replaceAll("\\D+", ""); }

    private static String fmtCci(String raw) {
        String d = onlyDigits(raw);
        if (d == null || d.length() != 20) return raw == null ? "" : raw;
        return d.substring(0,3) + "-" + d.substring(3,6) + "-" + d.substring(6,18) + "-" + d.substring(18);
    }

    private static String fmtCuenta(String raw) {
        if (raw == null) return "";
        // si ya viene con separadores, lo dejamos así para UI
        if (raw.matches(".*[-\\s].*")) return raw.trim();
        return onlyDigits(raw);
    }

    private String currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) throw new RuntimeException("No autenticado");
        return a.getName();
    }

    // ===== Operaciones =====
    @Transactional(readOnly = true)
    public List<CuentaResponse> listar(Moneda moneda) {
        String owner = currentUser();
        List<CuentaBancaria> list = repo.findByOwnerAndMonedaAndActivoTrueOrderByPrincipalDescIdAsc(owner, moneda);
        List<CuentaResponse> out = new ArrayList<>();
        for (CuentaBancaria c : list) out.add(toResponse(c));
        return out;
    }

    @Transactional
        public CuentaResponse crear(CrearCuentaRequest r) {
        String owner = currentUser();

        String ctaDigits = onlyDigits(r.getNumeroCuenta());
        String cciDigits = onlyDigits(r.getCci());
        if (cciDigits == null || cciDigits.length() != 20)
            throw new IllegalArgumentException("El CCI debe tener 20 dígitos.");

        if (repo.existsByOwnerAndMonedaAndNumeroCuentaAndActivoTrue(owner, r.getMoneda(), ctaDigits))
            throw new IllegalArgumentException("El número de cuenta ya existe para esta moneda.");

        if (repo.existsByOwnerAndMonedaAndCciAndActivoTrue(owner, r.getMoneda(), cciDigits))
            throw new IllegalArgumentException("El CCI ya existe para esta moneda.");

        CuentaBancaria c = new CuentaBancaria();
        c.setOwner(owner);
        c.setAlias(r.getAlias());
        c.setBanco(r.getBanco());
        c.setMoneda(r.getMoneda());
        c.setTipoCuenta(r.getTipoCuenta());   // ⬅️ importante
        c.setNumeroCuenta(ctaDigits);
        c.setCci(cciDigits);

        boolean hayAlguna = !repo
                .findByOwnerAndMonedaAndActivoTrueOrderByPrincipalDescIdAsc(owner, r.getMoneda())
                .isEmpty();
        c.setPrincipal(hayAlguna ? r.isPrincipal() : true);

        c = repo.save(c);

        if (c.isPrincipal()) {
            List<CuentaBancaria> otras = repo
                    .findByOwnerAndMonedaAndActivoTrueOrderByPrincipalDescIdAsc(owner, r.getMoneda());
            for (CuentaBancaria o : otras) {
                if (!Objects.equals(o.getId(), c.getId()) && o.isPrincipal()) {
                    o.setPrincipal(false);
                    repo.save(o);
                }
            }
        }

        return toResponse(c);
    }


    @Transactional
    public CuentaResponse actualizar(Long id, ActualizarCuentaRequest r) {
        String owner = currentUser();

        CuentaBancaria c = repo.findByIdAndOwnerAndActivoTrue(id, owner)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));

        String ctaDigits = onlyDigits(r.getNumeroCuenta());
        String cciDigits = onlyDigits(r.getCci());
        if (cciDigits == null || cciDigits.length() != 20)
            throw new IllegalArgumentException("El CCI debe tener 20 dígitos.");

        if (!ctaDigits.equals(c.getNumeroCuenta()) &&
                repo.existsByOwnerAndMonedaAndNumeroCuentaAndActivoTrue(owner, c.getMoneda(), ctaDigits))
            throw new IllegalArgumentException("El número de cuenta ya existe para esta moneda.");

        if (!cciDigits.equals(c.getCci()) &&
                repo.existsByOwnerAndMonedaAndCciAndActivoTrue(owner, c.getMoneda(), cciDigits))
            throw new IllegalArgumentException("El CCI ya existe para esta moneda.");

        c.setAlias(r.getAlias());
        c.setBanco(r.getBanco());
        c.setNumeroCuenta(ctaDigits);
        c.setCci(cciDigits);

        boolean marcandoPrincipal = r.isPrincipal();
        if (marcandoPrincipal && !c.isPrincipal()) {
            c.setPrincipal(true);
            // desmarcar otras
            List<CuentaBancaria> otras = repo.findByOwnerAndMonedaAndActivoTrueOrderByPrincipalDescIdAsc(owner, c.getMoneda());
            for (CuentaBancaria o : otras) {
                if (!Objects.equals(o.getId(), c.getId()) && o.isPrincipal()) {
                    o.setPrincipal(false);
                    repo.save(o);
                }
            }
        } else if (!marcandoPrincipal && c.isPrincipal()) {
            // si quita principal, mantener al menos una principal si existen otras
            c.setPrincipal(false);
            List<CuentaBancaria> otras = repo.findByOwnerAndMonedaAndActivoTrueOrderByPrincipalDescIdAsc(owner, c.getMoneda());
            for (CuentaBancaria o : otras) {
                if (!Objects.equals(o.getId(), c.getId())) {
                    o.setPrincipal(true);
                    repo.save(o);
                    break;
                }
            }
        }

        return toResponse(repo.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        String owner = currentUser();
        CuentaBancaria c = repo.findByIdAndOwnerAndActivoTrue(id, owner)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));

        boolean eraPrincipal = c.isPrincipal();
        Moneda moneda = c.getMoneda();

        c.setActivo(false);
        c.setPrincipal(false);
        repo.save(c);

        if (eraPrincipal) {
            List<CuentaBancaria> restantes = repo.findByOwnerAndMonedaAndActivoTrueOrderByPrincipalDescIdAsc(owner, moneda);
            if (!restantes.isEmpty()) {
                restantes.get(0).setPrincipal(true);
                repo.save(restantes.get(0));
            }
        }
    }

    private CuentaResponse toResponse(CuentaBancaria c) {
        CuentaResponse r = new CuentaResponse();
        r.setId(c.getId());
        r.setAlias(c.getAlias());
        r.setBanco(c.getBanco());
        r.setMoneda(c.getMoneda());
        r.setTipoCuenta(c.getTipoCuenta());   // ⬅️ NUEVO
        r.setNumeroCuenta(fmtCuenta(c.getNumeroCuenta()));
        r.setCci(fmtCci(c.getCci()));
        r.setPrincipal(c.isPrincipal());
        return r;
    }
}
