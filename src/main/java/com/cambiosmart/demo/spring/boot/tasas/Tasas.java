package com.cambiosmart.demo.spring.boot.tasas;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tasas") // <-- en minúsculas para evitar líos en Postgres
public class Tasas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String moneda;
    private Double compra;
    private Double venta;
    private String fecha;   // almacenada como texto yyyy-MM-dd
    private String fuente;
    private String hora;    // HH:mm:ss
    private String tipo;

    public Tasas() {} // requerido por JPA

    public Tasas(Long id, String moneda, Double compra, Double venta,
                 String fecha, String fuente, String hora, String tipo) {
        this.id = id;
        this.moneda = moneda;
        this.compra = compra;
        this.venta = venta;
        this.fecha = fecha;
        this.fuente = fuente;
        this.hora = hora;
        this.tipo = tipo;
    }

    // Getters
    public Long getId() { return id; }
    public String getMoneda() { return moneda; }
    public Double getCompra() { return compra; }
    public Double getVenta() { return venta; }
    public String getFecha() { return fecha; }
    public String getFuente() { return fuente; }
    public String getHora() { return hora; }
    public String getTipo() { return tipo; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    // Acepta Double y BigDecimal (overloads)
    public void setCompra(Double compra) { this.compra = compra; }
    public void setCompra(BigDecimal compra) {
        this.compra = (compra == null ? null : compra.doubleValue());
    }

    public void setVenta(Double venta) { this.venta = venta; }
    public void setVenta(BigDecimal venta) {
        this.venta = (venta == null ? null : venta.doubleValue());
    }

    // Acepta String y LocalDate (overloads)
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setFecha(LocalDate fecha) {
        this.fecha = (fecha == null ? null : fecha.toString());
    }

    public void setFuente(String fuente) { this.fuente = fuente; }

    // Acepta String y LocalTime (overloads)
    public void setHora(String hora) { this.hora = hora; }
    public void setHora(LocalTime hora) {
        this.hora = (hora == null ? null : hora.toString());
    }

    public void setTipo(String tipo) { this.tipo = tipo; }
}
