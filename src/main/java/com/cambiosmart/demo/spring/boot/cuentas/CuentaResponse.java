package com.cambiosmart.demo.spring.boot.cuentas;

public class CuentaResponse {
    private Long id;
    private String alias;
    private Banco banco;
    private Moneda moneda;
    private String numeroCuenta; // formateado para UI
    private String cci;          // formateado para UI
    private boolean principal;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Banco getBanco() { return banco; }
    public void setBanco(Banco banco) { this.banco = banco; }
    public Moneda getMoneda() { return moneda; }
    public void setMoneda(Moneda moneda) { this.moneda = moneda; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    public String getCci() { return cci; }
    public void setCci(String cci) { this.cci = cci; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
}
