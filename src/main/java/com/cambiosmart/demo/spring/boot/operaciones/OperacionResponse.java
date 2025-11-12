package com.cambiosmart.demo.spring.boot.operaciones;

import com.cambiosmart.demo.spring.boot.cuentas.Banco;
import com.cambiosmart.demo.spring.boot.cuentas.Moneda;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperacionResponse {
    public Long id;
    public String owner;
    public TipoOperacion tipo;
    public Moneda monedaOrigen;
    public Moneda monedaDestino;
    public BigDecimal montoOrigen;
    public BigDecimal tasaAplicada;
    public BigDecimal montoDestino;
    public EstadoOperacion estado;
    public LocalDateTime expiraEn;

    public Long cuentaOrigenId;
    public Long cuentaDestinoId;

    public Banco bancoEmpresa;
    public String cuentaEmpresa;
    public String cciEmpresa;
    public String titularEmpresa;

    public String referenciaTransferencia;
    public String comprobantePath;
}
