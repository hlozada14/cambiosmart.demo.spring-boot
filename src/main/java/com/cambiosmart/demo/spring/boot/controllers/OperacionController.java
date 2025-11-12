package com.cambiosmart.demo.spring.boot.controllers;

import com.cambiosmart.demo.spring.boot.operaciones.CotizarOperacionRequest;
import com.cambiosmart.demo.spring.boot.operaciones.OperacionResponse;
import com.cambiosmart.demo.spring.boot.operaciones.OperacionService;
import com.cambiosmart.demo.spring.boot.operaciones.SeleccionarCuentasRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/operaciones", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class OperacionController {

    private final OperacionService service;

    // Paso 1: Cotizar (crea operación)
    @PostMapping(value = "/cotizar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OperacionResponse> cotizar(@Valid @RequestBody CotizarOperacionRequest req) {
        OperacionResponse resp = service.cotizar(req);
        return ResponseEntity
                .created(URI.create("/api/operaciones/" + resp.getId()))
                .body(resp);
    }

    // Consultar una operación por id
    @GetMapping("/{id}")
    public ResponseEntity<OperacionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    // Paso 2: Seleccionar cuentas del usuario
    @PutMapping(value = "/{id}/cuentas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OperacionResponse> setCuentas(@PathVariable Long id,
                                                        @Valid @RequestBody SeleccionarCuentasRequest req) {
        return ResponseEntity.ok(service.seleccionarCuentas(id, req));
    }

    // Paso 4: Adjuntar constancia + referencia (multipart)
    @PostMapping(value = "/{id}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OperacionResponse> comprobante(@PathVariable Long id,
                                                         @RequestPart("file") MultipartFile file,
                                                         @RequestPart(value = "referencia", required = false) String referencia,
                                                         @RequestPart(value = "nota", required = false) String nota) throws Exception {
        return ResponseEntity.ok(service.adjuntarComprobante(id, referencia, nota, file));
    }
}

