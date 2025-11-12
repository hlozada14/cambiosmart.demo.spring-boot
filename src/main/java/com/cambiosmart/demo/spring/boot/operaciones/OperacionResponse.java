package com.cambiosmart.demo.spring.boot.operaciones;

import com.cambiosmart.demo.spring.boot.cuentas.Banco;
import com.cambiosmart.demo.spring.boot.cuentas.Moneda;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL) // no incluir nulls en la respuesta
public class OperacionResponse {

    private Long id;
    private String owner;                // Si no lo necesitas en el frontend, puedes quitarlo
    private TipoOperacion tipo;
    private Moneda monedaOrigen;
    private Moneda monedaDestino;
    private BigDecimal montoOrigen;
    private BigDecimal tasaAplicada;
    private BigDecimal montoDestino;
    private EstadoOperacion estado;
    private LocalDateTime expiraEn;

    private Long cuentaOrigenId;
    private Long cuentaDestinoId;

    private Banco bancoEmpresa;
    private String cuentaEmpresa;
    private String cciEmpresa;
    private String titularEmpresa;

    private String referenciaTransferencia;
    private String comprobantePath;

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public TipoOperacion getTipo() { return tipo; }
    public void setTipo(TipoOperacion tipo) { this.tipo = tipo; }

    public Moneda getMonedaOrigen() { return monedaOrigen; }
    public void setMonedaOrigen(Moneda monedaOrigen) { this.monedaOrigen = monedaOrigen; }

    public Moneda getMonedaDestino() { return monedaDestino; }
    public void setMonedaDestino(Moneda monedaDestino) { this.monedaDestino = monedaDestino; }

    public BigDecimal getMontoOrigen() { return montoOrigen; }
    public void setMontoOrigen(BigDecimal montoOrigen) { this.montoOrigen = montoOrigen; }

    public BigDecimal getTasaAplicada() { return tasaAplicada; }
    public void setTasaAplicada(BigDecimal tasaAplicada) { this.tasaAplicada = tasaAplicada; }

    public BigDecimal getMontoDestino() { return montoDestino; }
    public void setMontoDestino(BigDecimal montoDestino) { this.montoDestino = montoDestino; }

    public EstadoOperacion getEstado() { return estado; }
    public void setEstado(EstadoOperacion estado) { this.estado = estado; }

    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }

    public Long getCuentaOrigenId() { return cuentaOrigenId; }
    public void setCuentaOrigenId(Long cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }

    public Long getCuentaDestinoId() { return cuentaDestinoId; }
    public void setCuentaDestinoId(Long cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }

    public Banco getBancoEmpresa() { return bancoEmpresa; }
    public void setBancoEmpresa(Banco bancoEmpresa) { this.bancoEmpresa = bancoEmpresa; }

    public String getCuentaEmpresa() { return cuentaEmpresa; }
    public void setCuentaEmpresa(String cuentaEmpresa) { this.cuentaEmpresa = cuentaEmpresa; }

    public String getCciEmpresa() { return cciEmpresa; }
    public void setCciEmpresa(String cciEmpresa) { this.cciEmpresa = cciEmpresa; }

    public String getTitularEmpresa() { return titularEmpresa; }
    public void setTitularEmpresa(String titularEmpresa) { this.titularEmpresa = titularEmpresa; }

    public String getReferenciaTransferencia() { return referenciaTransferencia; }
    public void setReferenciaTransferencia(String referenciaTransferencia) { this.referenciaTransferencia = referenciaTransferencia; }

    public String getComprobantePath() { return comprobantePath; }
    public void setComprobantePath(String comprobantePath) { this.comprobantePath = comprobantePath; }
}
