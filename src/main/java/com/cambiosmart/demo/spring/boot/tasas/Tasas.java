package com.cambiosmart.demo.spring.boot.tasas;

import jakarta.persistence.*;

@Entity
@Table(name = "Tasas")
public class Tasas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String moneda;
    private Double compra;
    private Double venta;
    private String fecha;
    private String fuente;
    private String hora;
    private String tipo;

    public Tasas (Long id, String moneda, Double compra, Double venta, String fecha, String fuente, String hora, String tipo) {
        this.id = id;
        this.moneda = moneda;
        this.compra = compra;
        this.venta = venta;
        this.fecha = fecha;
        this.fuente = fuente;
        this.hora = hora;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Double getCompra() {
        return compra;
    }

    public void setCompra(Double compra) {
        this.compra = compra;
    }

    public Double getVenta() {
        return venta;
    }

    public void setVenta(Double venta) {
        this.venta = venta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // dentro de Tasas.java
    public Tasas() {}  // <--- constructor sin args requerido por JPA
}
