package com.cambiosmart.demo.spring.boot.operaciones;

import com.cambiosmart.demo.spring.boot.cuentas.CuentaBancaria;
import com.cambiosmart.demo.spring.boot.cuentas.CuentaRepository;
import com.cambiosmart.demo.spring.boot.cuentas.Moneda;
import com.cambiosmart.demo.spring.boot.tasas.TasaRepository;
import com.cambiosmart.demo.spring.boot.tasas.Tasas;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OperacionService {

    private final OperacionRepository repo;
    private final CuentaRepository cuentaRepo;
    private final TasaRepository tasaRepo;

    private String currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) throw new RuntimeException("No autenticado");
        return a.getName();
    }

    private BigDecimal ultimaVentaUsd() {
        Tasas t = tasaRepo.findTopByFuenteAndMonedaOrderByFechaDesc("CAMBIOSMART", "USD")
                .orElseThrow(() -> new IllegalStateException("No hay tasa CAMBIOSMART USD"));
        if (t.getVenta() == null) throw new IllegalStateException("Tasa venta nula");
        return BigDecimal.valueOf(t.getVenta());
    }

    private BigDecimal ultimaCompraUsd() {
        Tasas t = tasaRepo.findTopByFuenteAndMonedaOrderByFechaDesc("CAMBIOSMART", "USD")
                .orElseThrow(() -> new IllegalStateException("No hay tasa CAMBIOSMART USD"));
        if (t.getCompra() == null) throw new IllegalStateException("Tasa compra nula");
        return BigDecimal.valueOf(t.getCompra());
    }

    /** Mapea entidad -> DTO usando setters (acorde al OperacionResponse actual). */
    private OperacionResponse toResponse(Operacion o) {
        OperacionResponse r = new OperacionResponse();
        r.setId(o.getId());
        r.setOwner(o.getOwner());
        r.setTipo(o.getTipo());
        r.setMonedaOrigen(o.getMonedaOrigen());
        r.setMonedaDestino(o.getMonedaDestino());
        r.setMontoOrigen(o.getMontoOrigen());
        r.setTasaAplicada(o.getTasaAplicada());
        r.setMontoDestino(o.getMontoDestino());
        r.setEstado(o.getEstado());
        r.setExpiraEn(o.getExpiraEn());
        r.setCuentaOrigenId(o.getCuentaOrigenId());
        r.setCuentaDestinoId(o.getCuentaDestinoId());
        r.setBancoEmpresa(o.getBancoEmpresa());
        r.setCuentaEmpresa(o.getCuentaEmpresa());
        r.setCciEmpresa(o.getCciEmpresa());
        r.setTitularEmpresa(o.getTitularEmpresa());
        r.setReferenciaTransferencia(o.getReferenciaTransferencia());
        r.setComprobantePath(o.getComprobantePath());
        return r;
    }

    @Transactional
    public OperacionResponse cotizar(CotizarOperacionRequest req) {
        String owner = currentUser();

        if (req.getMonto() == null || req.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        // Monedas según el tipo
        Moneda mOrigen = (req.getTipo() == TipoOperacion.COMPRA_USD) ? Moneda.PEN : Moneda.USD;
        Moneda mDestino = (req.getTipo() == TipoOperacion.COMPRA_USD) ? Moneda.USD : Moneda.PEN;

        // Tasa: si cliente compra USD → VENTA; si vende USD → COMPRA
        BigDecimal tasa = (req.getTipo() == TipoOperacion.COMPRA_USD) ? ultimaVentaUsd() : ultimaCompraUsd();

        BigDecimal montoOrigen = req.getMonto().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoDestino;

        if (req.getTipo() == TipoOperacion.COMPRA_USD) {
            // obtiene USD = PEN / tasa
            montoDestino = montoOrigen.divide(tasa, 2, RoundingMode.HALF_UP);
        } else {
            // recibe PEN = USD * tasa
            montoDestino = montoOrigen.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        }

        Operacion op = new Operacion();
        op.setOwner(owner);
        op.setTipo(req.getTipo());
        op.setMonedaOrigen(mOrigen);
        op.setMonedaDestino(mDestino);
        op.setMontoOrigen(montoOrigen);
        op.setTasaAplicada(tasa);
        op.setMontoDestino(montoDestino);
        op.setEstado(EstadoOperacion.COTIZADA);
        op.setExpiraEn(LocalDateTime.now().plusMinutes(15));

        return toResponse(repo.save(op));
    }

    @Transactional
    public OperacionResponse seleccionarCuentas(Long id, SeleccionarCuentasRequest req) {
        String owner = currentUser();

        Operacion op = repo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new EntityNotFoundException("Operación no encontrada"));

        if (op.getEstado() != EstadoOperacion.COTIZADA)
            throw new IllegalStateException("La operación no está en estado COTIZADA");

        // Busca cuentas del usuario y activas
        CuentaBancaria origen = cuentaRepo.findByIdAndOwnerAndActivoTrue(req.getCuentaOrigenId(), owner)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta origen no existe o no pertenece al usuario"));
        CuentaBancaria destino = cuentaRepo.findByIdAndOwnerAndActivoTrue(req.getCuentaDestinoId(), owner)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta destino no existe o no pertenece al usuario"));

        if (origen.getMoneda() != op.getMonedaOrigen() || destino.getMoneda() != op.getMonedaDestino())
            throw new IllegalStateException("Las monedas de las cuentas no coinciden con la operación");

        op.setCuentaOrigenId(origen.getId());
        op.setCuentaDestinoId(destino.getId());

        // Seleccionar cuenta empresa (según moneda que envía el cliente)
        Moneda monedaClienteEnvia = op.getMonedaOrigen();
        for (CuentaEmpresaCatalogo c : CuentaEmpresaCatalogo.values()) {
            if (c.moneda == monedaClienteEnvia) {
                op.setBancoEmpresa(c.banco);
                op.setCuentaEmpresa(c.numeroCuenta);
                op.setCciEmpresa(c.cci);
                op.setTitularEmpresa(c.titular);
                break;
            }
        }

        op.setEstado(EstadoOperacion.CUENTAS_SELECCIONADAS);
        return toResponse(repo.save(op));
    }

    @Transactional(readOnly = true)
    public OperacionResponse obtener(Long id) {
        String owner = currentUser();
        Operacion op = repo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new EntityNotFoundException("Operación no encontrada"));
        return toResponse(op);
    }

    @Transactional
    public OperacionResponse adjuntarComprobante(Long id, String referencia, String nota, MultipartFile file) throws IOException {
        String owner = currentUser();
        Operacion op = repo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new EntityNotFoundException("Operación no encontrada"));

        if (op.getEstado() != EstadoOperacion.CUENTAS_SELECCIONADAS)
            throw new IllegalStateException("La operación no está lista para adjuntar comprobante");

        Files.createDirectories(Path.of("uploads"));
        String filename = "op_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Path.of("uploads", filename);
        Files.write(path, file.getBytes());

        op.setReferenciaTransferencia(referencia);
        op.setNotaCliente(nota);
        op.setComprobantePath(path.toString());
        op.setEstado(EstadoOperacion.PENDIENTE_VERIFICACION);

        return toResponse(repo.save(op));
    }
}
