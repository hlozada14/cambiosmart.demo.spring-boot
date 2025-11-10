package com.cambiosmart.demo.spring.boot.cotizador;

import com.cambiosmart.demo.spring.boot.tasas.TasaRepository;
import com.cambiosmart.demo.spring.boot.tasas.Tasas;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CotizadorService {

    private final TasaRepository tasaRepository;

    public CotizarResponse cotizar(@Valid CotizarRequest req) {
        // Traer tasas vigentes para USD
        Tasas cs = tasaRepository.findFirstByFuenteAndMonedaOrderByIdDesc("CAMBIOSMART", "USD")
                .orElseThrow(() -> new IllegalStateException("No hay tasa CAMBIOSMART vigente"));
        Tasas mdo = tasaRepository.findFirstByFuenteAndMonedaOrderByIdDesc("MERCADO", "USD")
                .orElseThrow(() -> new IllegalStateException("No hay tasa MERCADO vigente"));

        String lado = req.getLado();
        double monto = req.getMontoEntrada();
        String moneda = req.getMonedaEntrada();

        CotizarResponse res = new CotizarResponse();
        res.setLado(lado);
        res.setMontoEntrada(monto);
        res.setMonedaEntrada(moneda);

        if ("COMPRAR_USD".equalsIgnoreCase(lado)) {
            // Usuario entrega PEN, recibe USD → usar tasa COMPRA
            validarMoneda(moneda, "PEN");
            double tasaCS = cs.getCompra();
            double tasaMDO = mdo.getCompra();

            double usdCS = round2(monto / tasaCS);
            double usdMDO = round2(monto / tasaMDO);

            res.setTasaCambioSmart(tasaCS);
            res.setTasaMercado(tasaMDO);
            res.setTasaAplicada(tasaCS);
            res.setMontoSalida(usdCS);

            double ahorroUsd = round2(usdCS - usdMDO);
            double ahorroPen = round2(ahorroUsd * tasaCS);

            res.setAhorroUsd(ahorroUsd);
            res.setAhorroPen(ahorroPen);
            res.setDetalleCalculo("COMPRAR_USD: usdCS = PEN/tasa_compra_cs; ahorro = usdCS - usdMDO");
            return res;

        } else if ("VENDER_USD".equalsIgnoreCase(lado)) {
            // Usuario entrega USD, recibe PEN → usar tasa VENTA
            validarMoneda(moneda, "USD");
            double tasaCS = cs.getVenta();
            double tasaMDO = mdo.getVenta();

            double penCS = round2(monto * tasaCS);
            double penMDO = round2(monto * tasaMDO);

            res.setTasaCambioSmart(tasaCS);
            res.setTasaMercado(tasaMDO);
            res.setTasaAplicada(tasaCS);
            res.setMontoSalida(penCS);

            double ahorroPen = round2(penCS - penMDO);
            double ahorroUsd = round2(ahorroPen / tasaCS);

            res.setAhorroPen(ahorroPen);
            res.setAhorroUsd(ahorroUsd);
            res.setDetalleCalculo("VENDER_USD: penCS = USD*tasa_venta_cs; ahorro = penCS - penMDO");
            return res;

        } else {
            throw new IllegalArgumentException("Lado inválido: use COMPRAR_USD o VENDER_USD");
        }
    }

    // ========= Helpers =========
    private void validarMoneda(String actual, String esperada) {
        if (!esperada.equalsIgnoreCase(actual)) {
            throw new IllegalArgumentException("Moneda de entrada debe ser " + esperada);
        }
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}