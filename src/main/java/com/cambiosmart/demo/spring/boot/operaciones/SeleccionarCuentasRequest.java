package com.cambiosmart.demo.spring.boot.operaciones;

import jakarta.validation.constraints.NotNull;

public class SeleccionarCuentasRequest {
    @NotNull
    private Long cuentaOrigenId;
    @NotNull
    private Long cuentaDestinoId;

    public Long getCuentaOrigenId() { return cuentaOrigenId; }
    public void setCuentaOrigenId(Long cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }
    public Long getCuentaDestinoId() { return cuentaDestinoId; }
    public void setCuentaDestinoId(Long cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }
}
