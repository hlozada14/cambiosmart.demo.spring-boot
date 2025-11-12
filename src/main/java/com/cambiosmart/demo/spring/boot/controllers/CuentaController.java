package com.cambiosmart.demo.spring.boot.controllers;

import com.cambiosmart.demo.spring.boot.cuentas.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuentas-bancarias")
@RequiredArgsConstructor
@Validated
public class CuentaController {

    private final CuentaService service;

    // GET /api/cuentas?moneda=PEN
    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar(@RequestParam Moneda moneda) {
        return ResponseEntity.ok(service.listar(moneda));
    }

    // POST /api/cuentas-bancarias
    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CrearCuentaRequest body) {
        return ResponseEntity.ok(service.crear(body));
    }

    // PUT /api/cuentas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ActualizarCuentaRequest body) {
        return ResponseEntity.ok(service.actualizar(id, body));
    }

    // DELETE /api/cuentas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
