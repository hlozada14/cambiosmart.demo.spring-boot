package com.cambiosmart.demo.spring.boot.cotizador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CotizarRequest {

    // "COMPRAR_USD" o "VENDER_USD"
    @NotBlank
    private String lado;

    // Monto que el usuario ingresa
    @NotNull @Positive
    private Double montoEntrada;

    // "PEN" o "USD" (según el lado)
    @NotBlank
    private String monedaEntrada;

    public String getLado() { return lado; }
    public void setLado(String lado) { this.lado = lado; }

    public Double getMontoEntrada() { return montoEntrada; }
    public void setMontoEntrada(Double montoEntrada) { this.montoEntrada = montoEntrada; }

    public String getMonedaEntrada() { return monedaEntrada; }
    public void setMonedaEntrada(String monedaEntrada) { this.monedaEntrada = monedaEntrada; }
}
