package com.cambiosmart.demo.spring.boot.operaciones;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CotizarOperacionRequest {
    @NotNull
    private TipoOperacion tipo;

    @NotNull @DecimalMin("1.00")
    private BigDecimal monto; // monto que envía el cliente (origen)

    // getters/setters
    public TipoOperacion getTipo() { return tipo; }
    public void setTipo(TipoOperacion tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
