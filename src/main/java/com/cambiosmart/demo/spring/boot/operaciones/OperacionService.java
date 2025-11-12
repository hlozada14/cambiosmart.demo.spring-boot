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
import java.util.List;

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

    private OperacionResponse toResponse(Operacion o) {
        OperacionResponse r = new OperacionResponse();
        r.id = o.getId();
        r.owner = o.getOwner();
        r.tipo = o.getTipo();
        r.monedaOrigen = o.getMonedaOrigen();
        r.monedaDestino = o.getMonedaDestino();
        r.montoOrigen = o.getMontoOrigen();
        r.tasaAplicada = o.getTasaAplicada();
        r.montoDestino = o.getMontoDestino();
        r.estado = o.getEstado();
        r.expiraEn = o.getExpiraEn();
        r.cuentaOrigenId = o.getCuentaOrigenId();
        r.cuentaDestinoId = o.getCuentaDestinoId();
        r.bancoEmpresa = o.getBancoEmpresa();
        r.cuentaEmpresa = o.getCuentaEmpresa();
        r.cciEmpresa = o.getCciEmpresa();
        r.titularEmpresa = o.getTitularEmpresa();
        r.referenciaTransferencia = o.getReferenciaTransferencia();
        r.comprobantePath = o.getComprobantePath();
        return r;
    }

    @Transactional
    public OperacionResponse cotizar(CotizarOperacionRequest req) {
        String owner = currentUser();

        // Monedas según el tipo
        Moneda mOrigen = (req.getTipo() == TipoOperacion.COMPRA_USD) ? Moneda.PEN : Moneda.USD;
        Moneda mDestino = (req.getTipo() == TipoOperacion.COMPRA_USD) ? Moneda.USD : Moneda.PEN;

        // Tasa: si cliente compra USD → usamos VENTA; si vende USD → usamos COMPRA
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

        CuentaBancaria origen = cuentaRepo.findById(req.getCuentaOrigenId())
                .orElseThrow(() -> new EntityNotFoundException("Cuenta origen no existe"));
        CuentaBancaria destino = cuentaRepo.findById(req.getCuentaDestinoId())
                .orElseThrow(() -> new EntityNotFoundException("Cuenta destino no existe"));

        if (!owner.equals(origen.getOwner()) || !owner.equals(destino.getOwner()))
            throw new IllegalStateException("Las cuentas no pertenecen al usuario");

        if (origen.getMoneda() != op.getMonedaOrigen() || destino.getMoneda() != op.getMonedaDestino())
            throw new IllegalStateException("Las monedas de las cuentas no coinciden con la operación");

        op.setCuentaOrigenId(origen.getId());
        op.setCuentaDestinoId(destino.getId());

        // Ofrecer/seleccionar por defecto una cuenta empresa de la moneda que el cliente ENVÍA
        Moneda monedaClienteEnvía = op.getMonedaOrigen();
        CuentaEmpresaCatalogo empresa = null;
        for (CuentaEmpresaCatalogo c : CuentaEmpresaCatalogo.values()) {
            if (c.moneda == monedaClienteEnvía) { empresa = c; break; }
        }
        if (empresa != null) {
            op.setBancoEmpresa(empresa.banco);
            op.setCuentaEmpresa(empresa.numeroCuenta);
            op.setCciEmpresa(empresa.cci);
            op.setTitularEmpresa(empresa.titular);
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

        // Guardado simple en carpeta local 'uploads'
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
