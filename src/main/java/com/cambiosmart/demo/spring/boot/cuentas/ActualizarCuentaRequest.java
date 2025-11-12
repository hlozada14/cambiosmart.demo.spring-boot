package com.cambiosmart.demo.spring.boot.cuentas;

import jakarta.validation.constraints.*;

public class ActualizarCuentaRequest {
    @NotBlank @Size(min = 2, max = 60)
    private String alias;

    @NotNull
    private Banco banco;

    @NotBlank @Size(min = 8, max = 64)
    private String numeroCuenta;

    @NotBlank @Size(min = 20, max = 32)
    private String cci;

    private boolean principal;

    // getters/setters
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Banco getBanco() { return banco; }
    public void setBanco(Banco banco) { this.banco = banco; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    public String getCci() { return cci; }
    public void setCci(String cci) { this.cci = cci; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
}
