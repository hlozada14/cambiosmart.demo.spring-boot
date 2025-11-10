package com.cambiosmart.demo.spring.boot.cotizador;

public class CotizarResponse {

    private String lado;            // COMPRAR_USD | VENDER_USD
    private Double montoEntrada;
    private String monedaEntrada;

    private Double tasaCambioSmart; // compra o venta según lado
    private Double tasaMercado;     // compra o venta según lado
    private Double tasaAplicada;    // misma que tasaCambioSmart (para leyenda UI)

    private Double montoSalida;     // PEN o USD, según lado
    private Double ahorroPen;       // ahorro expresado en PEN
    private Double ahorroUsd;       // ahorro expresado en USD
    private String detalleCalculo;  // texto simple para trazabilidad

    // getters/setters
    public String getLado() { return lado; }
    public void setLado(String lado) { this.lado = lado; }
    public Double getMontoEntrada() { return montoEntrada; }
    public void setMontoEntrada(Double montoEntrada) { this.montoEntrada = montoEntrada; }
    public String getMonedaEntrada() { return monedaEntrada; }
    public void setMonedaEntrada(String monedaEntrada) { this.monedaEntrada = monedaEntrada; }
    public Double getTasaCambioSmart() { return tasaCambioSmart; }
    public void setTasaCambioSmart(Double tasaCambioSmart) { this.tasaCambioSmart = tasaCambioSmart; }
    public Double getTasaMercado() { return tasaMercado; }
    public void setTasaMercado(Double tasaMercado) { this.tasaMercado = tasaMercado; }
    public Double getTasaAplicada() { return tasaAplicada; }
    public void setTasaAplicada(Double tasaAplicada) { this.tasaAplicada = tasaAplicada; }
    public Double getMontoSalida() { return montoSalida; }
    public void setMontoSalida(Double montoSalida) { this.montoSalida = montoSalida; }
    public Double getAhorroPen() { return ahorroPen; }
    public void setAhorroPen(Double ahorroPen) { this.ahorroPen = ahorroPen; }
    public Double getAhorroUsd() { return ahorroUsd; }
    public void setAhorroUsd(Double ahorroUsd) { this.ahorroUsd = ahorroUsd; }
    public String getDetalleCalculo() { return detalleCalculo; }
    public void setDetalleCalculo(String detalleCalculo) { this.detalleCalculo = detalleCalculo; }
}

