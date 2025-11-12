package com.cambiosmart.demo.spring.boot.controllers;

import com.cambiosmart.demo.spring.boot.bcrp.TasaSyncService;
import com.cambiosmart.demo.spring.boot.config.BcrpProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/tasas/bcrp", produces = "application/json")
@RequiredArgsConstructor
public class AdminBcrpController {

    private static final Logger log = LoggerFactory.getLogger(AdminBcrpController.class);

    private final TasaSyncService service;
    private final BcrpProperties props;

    // POST /api/admin/tasas/bcrp/backfill?start=2025-11-01&end=2025-11-10
    @PostMapping("/backfill")
    public ResponseEntity<?> backfill(@RequestParam(required = false) String start,
                                      @RequestParam(required = false) String end) {
        try {
            LocalDate s = parseDate(start);
            LocalDate e = parseDate(end);

            if (s != null && e != null && e.isBefore(s)) {
                return ResponseEntity.unprocessableEntity().body(Map.of(
                        "error", "INVALID_RANGE",
                        "message", "La fecha 'end' no puede ser menor que 'start'",
                        "start", s.toString(),
                        "end", e.toString()
                ));
            }

            int n = service.backfill(s, e);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "processed", n,
                    "start", (s == null ? null : s.toString()),
                    "end", (e == null ? null : e.toString())
            ));

        } catch (DateTimeParseException dtpe) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "BAD_DATE",
                    "message", "Formato de fecha inválido. Usa yyyy-MM-dd",
                    "detail", dtpe.getParsedString()
            ));
        } catch (Exception ex) {
            log.error("Backfill error", ex);
            return ResponseEntity.status(502).body(Map.of(
                    "error", "INTEGRATION_FAILED",
                    "message", ex.getMessage()
            ));
        }
    }

    // POST /api/admin/tasas/bcrp/sync-today
    @PostMapping("/sync-today")
    public ResponseEntity<?> syncToday() {
        try {
            ZoneId zone = ZoneId.of(props.getZone());
            LocalDate hoy = LocalDate.now(zone);
            LocalDate desde = hoy.minusDays(3);

            int n = service.backfill(desde, hoy);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "processed", n,
                    "start", desde.toString(),
                    "end", hoy.toString(),
                    "zone", zone.toString()
            ));

        } catch (Exception ex) {
            log.error("Sync-today error", ex);
            return ResponseEntity.status(502).body(Map.of(
                    "error", "INTEGRATION_FAILED",
                    "message", ex.getMessage()
            ));
        }
    }

    // (Opcional) POST /api/admin/tasas/bcrp/backfill-props
    // Ejecuta el rango definido en application.properties (bcrp.backfill.start/end)
    @PostMapping("/backfill-props")
    public ResponseEntity<?> backfillProps() {
        try {
            int n = service.backfillFromProps();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "processed", n,
                    "source", "properties"
            ));
        } catch (Exception ex) {
            log.error("Backfill-props error", ex);
            return ResponseEntity.status(502).body(Map.of(
                    "error", "INTEGRATION_FAILED",
                    "message", ex.getMessage()
            ));
        }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s); // lanza DateTimeParseException si es inválida
    }
}
