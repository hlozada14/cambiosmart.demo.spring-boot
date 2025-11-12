package com.cambiosmart.demo.spring.boot.operaciones;

import com.cambiosmart.demo.spring.boot.cuentas.Banco;
import com.cambiosmart.demo.spring.boot.cuentas.Moneda;

public enum CuentaEmpresaCatalogo {
    // Ejemplos de referencia. Cambia con tus cuentas reales.
    BCP_PEN(Banco.BCP, Moneda.PEN, "191-9876543-0-21", "002-191-009876543021-45", "CAMBIOSMART S.A.C."),
    INTERBANK_PEN(Banco.INTERBANK, Moneda.PEN, "200-3009876543", "003-200-0103009876543-89", "CAMBIOSMART S.A.C."),
    BCP_USD(Banco.BCP, Moneda.USD, "191-1111111-1-11", "002-191-001111111111-11", "CAMBIOSMART S.A.C."),
    INTERBANK_USD(Banco.INTERBANK, Moneda.USD, "200-2222222", "003-200-002222222222-22", "CAMBIOSMART S.A.C.");

    public final Banco banco;
    public final Moneda moneda;
    public final String numeroCuenta;
    public final String cci;
    public final String titular;

    CuentaEmpresaCatalogo(Banco banco, Moneda moneda, String numeroCuenta, String cci, String titular) {
        this.banco = banco;
        this.moneda = moneda;
        this.numeroCuenta = numeroCuenta;
        this.cci = cci;
        this.titular = titular;
    }
}
