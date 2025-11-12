package com.cambiosmart.demo.spring.boot.operaciones;

import com.cambiosmart.demo.spring.boot.cuentas.Banco;
import com.cambiosmart.demo.spring.boot.cuentas.Moneda;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operaciones", indexes = {
        @Index(name = "idx_owner_estado", columnList = "owner, estado"),
        @Index(name = "idx_owner_creado", columnList = "owner, creado_en")
})
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String owner; // username

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOperacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Moneda monedaOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Moneda monedaDestino;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoOrigen;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal tasaAplicada;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOperacion estado;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    // Selección de cuentas del usuario
    private Long cuentaOrigenId;
    private Long cuentaDestinoId;

    // Cuenta empresa seleccionada para el depósito del cliente (según moneda que envía)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Banco bancoEmpresa;

    @Column(length = 64)
    private String cuentaEmpresa;

    @Column(length = 32)
    private String cciEmpresa;

    @Column(length = 120)
    private String titularEmpresa;

    // Confirmación de transferencia
    @Column(length = 64)
    private String referenciaTransferencia;

    @Column(length = 512)
    private String notaCliente;

    @Column(length = 256)
    private String comprobantePath;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        creadoEn = now;
        actualizadoEn = now;
    }

    @PreUpdate
    void preUpdate() { actualizadoEn = LocalDateTime.now(); }

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

    public String getNotaCliente() { return notaCliente; }
    public void setNotaCliente(String notaCliente) { this.notaCliente = notaCliente; }

    public String getComprobantePath() { return comprobantePath; }
    public void setComprobantePath(String comprobantePath) { this.comprobantePath = comprobantePath; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
