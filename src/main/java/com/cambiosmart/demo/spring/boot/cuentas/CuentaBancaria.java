// com.cambiosmart.demo.spring.boot.cuentas.CuentaBancaria
package com.cambiosmart.demo.spring.boot.cuentas;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cuentas_bancarias",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_owner_moneda_num", columnNames = {"owner", "moneda", "numero_cuenta"}),
                @UniqueConstraint(name = "uq_owner_moneda_cci", columnNames = {"owner", "moneda", "cci"})
        },
        indexes = {
                @Index(name = "idx_owner_moneda", columnList = "owner, moneda"),
                @Index(name = "idx_owner_activo", columnList = "owner, activo")
        }
)
public class CuentaBancaria {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String owner; // username del usuario autenticado

    @Column(nullable = false, length = 60)
    private String alias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Banco banco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Moneda moneda; // PEN, USD

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta; // AHORROS, CORRIENTE

    // Guarda solo dígitos (normaliza en el servicio)
    @Column(name = "numero_cuenta", nullable = false, length = 40)
    private String numeroCuenta;

    // 20 dígitos; para tolerar entradas con guiones si se escapara la normalización, dejamos 32
    @Column(name = "cci", nullable = false, length = 32)
    private String cci;

    @Column(nullable = false)
    private boolean principal = false;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.creadoEn = now;
        this.actualizadoEn = now;
    }

    @PreUpdate
    void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    // getters & setters
    public Long getId() { return id; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Banco getBanco() { return banco; }
    public void setBanco(Banco banco) { this.banco = banco; }
    public Moneda getMoneda() { return moneda; }
    public void setMoneda(Moneda moneda) { this.moneda = moneda; }
    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(TipoCuenta tipoCuenta) { this.tipoCuenta = tipoCuenta; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    public String getCci() { return cci; }
    public void setCci(String cci) { this.cci = cci; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
