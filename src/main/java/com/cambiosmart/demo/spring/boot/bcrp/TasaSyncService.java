// com.cambiosmart.demo.spring.boot.bcrp.TasaSyncService
package com.cambiosmart.demo.spring.boot.bcrp;

import com.cambiosmart.demo.spring.boot.config.BcrpProperties;
import com.cambiosmart.demo.spring.boot.config.TasaMarginProperties;
import com.cambiosmart.demo.spring.boot.tasas.TasaRepository;
import com.cambiosmart.demo.spring.boot.tasas.Tasas;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;           // <-- nuevo
import java.time.format.DateTimeFormatterBuilder;   // <-- nuevo
import java.time.format.ResolverStyle;              // <-- nuevo
import java.util.*;

@Service
@RequiredArgsConstructor
public class TasaSyncService {

    private static final Logger log = LoggerFactory.getLogger(TasaSyncService.class);

    private final BcrpClient client;
    private final TasaRepository repo;
    private final BcrpProperties props;
    private final TasaMarginProperties margin;

    // ==== meses ES para dd.MMM.yy ====
    private static final Map<String, Integer> ES_MONTHS;
    static {
        Map<String, Integer> m = new HashMap<>();
        m.put("ene", 1); m.put("feb", 2); m.put("mar", 3); m.put("abr", 4);
        m.put("may", 5); m.put("jun", 6); m.put("jul", 7); m.put("ago", 8);
        m.put("sep", 9); m.put("sept", 9); m.put("set", 9);
        m.put("oct", 10); m.put("nov", 11); m.put("dic", 12);
        ES_MONTHS = Collections.unmodifiableMap(m);
    }

    @Transactional
    public int backfill(LocalDate start, LocalDate end) {
        String s = (start != null ? start.toString() : null);
        String e = (end   != null ? end.toString()   : null);
        log.info("BCRP backfill {} .. {}", s, e);

        String rawCompra = client.fetchCompra(s, e);
        String rawVenta  = client.fetchVenta(s, e);

        Map<LocalDate, BigDecimal> compra = parse(rawCompra);
        Map<LocalDate, BigDecimal> venta  = parse(rawVenta);
        log.info("Fechas parseadas: compra={}, venta={}", compra.size(), venta.size());

        BigDecimal mCompra = Optional.ofNullable(margin.getCompra()).orElse(BigDecimal.ZERO);
        BigDecimal mVenta  = Optional.ofNullable(margin.getVenta()).orElse(BigDecimal.ZERO);
        int scale = Math.max(0, margin.getRoundScale());

        int n = 0;
        for (LocalDate f : new TreeSet<>(compra.keySet())) {
            BigDecimal bc = compra.get(f);
            BigDecimal bv = venta.get(f);
            if (bc == null || bv == null) continue;

            // 1) MERCADO (BCRP)
            upsert(f, "MERCADO", "USD", "REFERENCIA", bc, bv);

            // 2) CAMBIOSMART (margen + redondeo)
            BigDecimal pubC = round(bc.add(mCompra), scale);
            BigDecimal pubV = round(bv.subtract(mVenta), scale);
            upsert(f, "CAMBIOSMART", "USD", "REFERENCIA", pubC, pubV);

            n++;
        }
        log.info("Fechas procesadas: {}", n);
        return n;
    }

    public int backfillFromProps() {
        BcrpProperties.Backfill bf = props.getBackfill();
        LocalDate start = (bf != null && bf.getStart() != null && !bf.getStart().isBlank())
                ? LocalDate.parse(bf.getStart()) : null;
        LocalDate end   = (bf != null && bf.getEnd() != null && !bf.getEnd().isBlank())
                ? LocalDate.parse(bf.getEnd())   : null;
        int n = backfill(start, end);
        log.info("Backfill al inicio -> {} registros ({} .. {})", n,
                start != null ? start : "null", end != null ? end : "null");
        return n;
    }

    private void upsert(LocalDate fecha, String fuente, String moneda, String tipo,
                        BigDecimal compra, BigDecimal venta) {

        String fechaIso = (fecha != null ? fecha.toString() : null); // tu entidad usa String

        var opt = repo.findByFechaAndFuenteAndMoneda(fechaIso, fuente, moneda);
        Tasas t = opt.orElseGet(Tasas::new);

        t.setFecha(fechaIso);
        t.setFuente(fuente);
        t.setMoneda(moneda);
        t.setTipo(tipo);
        t.setHora("12:00:00");

        t.setCompra(compra != null ? compra.doubleValue() : null);
        t.setVenta(venta  != null ? venta.doubleValue()  : null);

        repo.save(t);
    }

    private BigDecimal round(BigDecimal v, int scale) {
        return (v == null) ? null : v.setScale(scale, java.math.RoundingMode.HALF_UP);
    }

    // ================== PARSER MEJORADO ==================
    private Map<LocalDate, BigDecimal> parse(String body) {
        Map<LocalDate, BigDecimal> out = new HashMap<>();
        if (body == null || body.isBlank()) return out;

        String trimmed = body.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);

        // si es HTML, no seguimos
        if (lower.startsWith("<html") || lower.startsWith("<!doctype")) {
            log.warn("BCRP devolvió HTML (posible error de parámetros/formato).");
            return out;
        }

        // --- JSON ---
        if (lower.startsWith("{") || lower.startsWith("[")) {
            try {
                ObjectMapper om = new ObjectMapper();
                JsonNode root = om.readTree(trimmed);

                // 1) periods[] con values dentro de cada periodo
                JsonNode periods = root.path("periods");
                if (periods.isArray() && periods.size() > 0) {
                    for (JsonNode p : periods) {
                        LocalDate f = tryDate(p.path("name").asText(null));
                        BigDecimal v = null;
                        JsonNode values = p.path("values");
                        if (values.isArray() && values.size() > 0) {
                            JsonNode vNode = values.get(0);
                            if (!vNode.isNull()) v = nodeToBig(vNode);
                        }
                        if (f != null && v != null) out.put(f, v);
                    }
                }

                // 2) data.series[0].data[] zipeado con data.periods[]
                if (out.isEmpty()) {
                    JsonNode data = root.has("data") ? root.get("data") : root;
                    JsonNode per2 = data.path("periods");
                    JsonNode series = data.path("series");
                    if (per2.isArray() && series.isArray() && series.size() > 0) {
                        JsonNode d = series.get(0).path("data"); // valores 1-1 con periods
                        if (d.isArray() && d.size() > 0) {
                            int n = Math.min(per2.size(), d.size());
                            for (int i = 0; i < n; i++) {
                                LocalDate f = tryDate(per2.get(i).asText(null));
                                BigDecimal v = nodeToBig(d.get(i));
                                if (f != null && v != null) out.put(f, v);
                            }
                        }
                    }
                }

                // 3) series[0].values[] zipeado con periods[]
                if (out.isEmpty()) {
                    JsonNode series = root.path("series");
                    JsonNode per3 = root.path("periods");
                    if (series.isArray() && series.size() > 0) {
                        JsonNode s0 = series.get(0);
                        JsonNode values = s0.path("values");
                        if (per3.isArray() && values.isArray()) {
                            int n = Math.min(per3.size(), values.size());
                            for (int i = 0; i < n; i++) {
                                LocalDate f = tryDate(per3.get(i).asText(null));
                                BigDecimal v = nodeToBig(values.get(i));
                                if (f != null && v != null) out.put(f, v);
                            }
                        }
                    }
                }

                // 4) series[0].observations[] con {date, value}
                if (out.isEmpty()) {
                    JsonNode data = root.has("data") ? root.get("data") : root;
                    JsonNode series = data.path("series");
                    if (series.isArray() && series.size() > 0) {
                        JsonNode obs = series.get(0).path("observations");
                        if (obs.isArray() && obs.size() > 0) {
                            for (JsonNode o : obs) {
                                LocalDate f = tryDate(o.path("date").asText(null));
                                BigDecimal v = nodeToBig(o.path("value"));
                                if (f != null && v != null) out.put(f, v);
                            }
                        }
                    }
                }

                // 5) Fallback: primera matriz de pares [[fecha, valor], ...]
                if (out.isEmpty()) {
                    JsonNode pairs = findFirstPairsArray(root);
                    if (pairs != null) {
                        for (JsonNode pair : pairs) {
                            if (pair.size() >= 2) {
                                LocalDate f = tryDate(pair.get(0).asText());
                                BigDecimal v = nodeToBig(pair.get(1));
                                if (f != null && v != null) out.put(f, v);
                            }
                        }
                    }
                }

                if (out.isEmpty()) {
                    logPreview("Parser JSON no encontró datos", body);
                }
            } catch (Exception ex) {
                log.warn("No se pudo parsear JSON del BCRP: {}", ex.toString());
            }
        }

        // --- CSV (fallback) ---
        if (out.isEmpty() && body.contains("\n") && (body.contains(",") || body.contains(";"))) {
            String[] lines = body.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                if (i == 0 && line.toLowerCase(Locale.ROOT).matches(".*[a-z].*")) continue; // cabecera
                String[] parts = line.split("[,;]");
                if (parts.length >= 2) {
                    LocalDate f = tryDate(parts[0]);
                    BigDecimal v = tryBig(parts[1]);
                    if (f != null && v != null) out.put(f, v);
                }
            }
            if (out.isEmpty()) {
                logPreview("Parser CSV no encontró datos", body);
            }
        }

        return out;
    }

    private void logPreview(String msg, String body) {
        String preview = body.length() > 800 ? body.substring(0, 800) + " …(truncado)" : body;
        log.warn("{}.\nPreview body:\n{}", msg, preview);
    }

    // Busca el primer arreglo tipo [[fecha, valor], ...]
    private JsonNode findFirstPairsArray(JsonNode node) {
        if (node == null) return null;
        if (node.isArray() && node.size() > 0 && node.get(0).isArray() && node.get(0).size() >= 2
                && node.get(0).get(0).isValueNode()) return node;
        if (node.isContainerNode()) {
            var it = node.elements();
            while (it.hasNext()) {
                JsonNode res = findFirstPairsArray(it.next());
                if (res != null) return res;
            }
            var it2 = node.fieldNames();
            while (it2.hasNext()) {
                String k = it2.next();
                JsonNode res = findFirstPairsArray(node.get(k));
                if (res != null) return res;
            }
        }
        return null;
    }

    // ======= NUEVO tryDate que entiende "dd.MMM.yy" en español =======
    private LocalDate tryDate(String s) {
        if (s == null) return null;
        String t = s.trim();

        // 0) ISO directo
        try { return LocalDate.parse(t); } catch (Exception ignore) {}

        // 1) dd.MMM.yy o dd.MMM.yyyy en español
        String norm = t.replace("/", ".").replace("-", ".").replace(" ", "");
        String[] parts = norm.split("\\.");
        if (parts.length == 3) {
            Integer d = safeInt(parts[0]);
            String monKey = parts[1].toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}]", "");
            Integer yRaw = safeInt(parts[2]);
            Integer mon = ES_MONTHS.get(monKey);
            if (d != null && mon != null && yRaw != null) {
                int y = yRaw;
                // 00..49 -> 2000..2049 ; 50..99 -> 1950..1999
                if (parts[2].length() <= 2) y = (yRaw <= 49) ? (2000 + yRaw) : (1900 + yRaw);
                try { return LocalDate.of(y, mon, d); } catch (Exception ignore) {}
            }
        }

        // 2) Fallback con DateTimeFormatter por locale español
        try {
            Locale es = new Locale("es", "PE");
            String x = t.replace("/", ".").replace("-", ".");
            DateTimeFormatter fmtYY = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd.MMM.uu")
                    .toFormatter(es)
                    .withResolverStyle(ResolverStyle.SMART);
            return LocalDate.parse(x, fmtYY);
        } catch (Exception ignore) {}
        try {
            Locale es = new Locale("es", "PE");
            String x = t.replace("/", ".").replace("-", ".");
            DateTimeFormatter fmtYYYY = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd.MMM.uuuu")
                    .toFormatter(es)
                    .withResolverStyle(ResolverStyle.SMART);
            return LocalDate.parse(x, fmtYYYY);
        } catch (Exception ignore) {}

        // 3) Si viene con hora: "yyyy-MM-dd HH:mm:ss" -> corta la hora
        int sp = t.indexOf(' ');
        if (sp > 0) {
            String iso = t.substring(0, sp);
            try { return LocalDate.parse(iso); } catch (Exception ignore) {}
        }

        return null;
    }

    // helper
    private Integer safeInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private BigDecimal tryBig(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        String tl = t.toLowerCase(Locale.ROOT);
        if (tl.equals("na") || tl.equals("n.d.") || tl.equals("nd") || tl.equals("null")) return null;

        if (t.matches("^-?\\d{1,3}(\\.\\d{3})*,\\d+$")) { // miles con puntos, decimales con coma
            t = t.replace(".", "").replace(',', '.');
        } else if (t.matches("^-?\\d+,\\d+$")) {
            t = t.replace(',', '.');
        }
        try { return new BigDecimal(t); } catch (Exception e) { return null; }
    }

    private BigDecimal nodeToBig(JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (n.isNumber()) return new BigDecimal(n.asText());
        return tryBig(n.asText());
    }
}
