package com.cambiosmart.demo.spring.boot.controllers;

import com.cambiosmart.demo.spring.boot.cotizador.CotizarRequest;
import com.cambiosmart.demo.spring.boot.cotizador.CotizarResponse;
import com.cambiosmart.demo.spring.boot.cotizador.CotizadorService;
import com.cambiosmart.demo.spring.boot.tasas.TasaRepository;
import com.cambiosmart.demo.spring.boot.tasas.Tasas;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class CotizadorPublicController {

    private final CotizadorService cotizadorService;
    private final TasaRepository tasaRepository;

    // Tasas vigentes para pintar el cotizador
    @GetMapping("/tasas/vigentes")
    public ResponseEntity<?> tasasVigentes() {
        Tasas cs = tasaRepository
                .findFirstByFuenteIgnoreCaseAndMonedaIgnoreCaseOrderByIdDesc("CAMBIOSMART", "USD")
                .orElse(null);
        Tasas mdo = tasaRepository
                .findFirstByFuenteIgnoreCaseAndMonedaIgnoreCaseOrderByIdDesc("MERCADO", "USD")
                .orElse(null);

        if (cs == null || mdo == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Faltan tasas vigentes CAMBIOSMART o MERCADO"));
        }

        return ResponseEntity.ok(Map.of(
                "cambiosmart", Map.of("compra", cs.getCompra(), "venta", cs.getVenta()),
                "mercado",     Map.of("compra", mdo.getCompra(), "venta", mdo.getVenta())
        ));
    }

    // Cálculo del cotizador (público)
    @PostMapping("/cotizar")
    public CotizarResponse cotizar(@RequestBody @Valid CotizarRequest request) {
        return cotizadorService.cotizar(request);
    }
}


