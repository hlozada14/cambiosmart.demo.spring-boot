package com.cambiosmart.demo.spring.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@Validated
@ConfigurationProperties(prefix = "bcrp")
public class BcrpProperties {

    private Api api = new Api();
    private Series series = new Series();
    private Http http = new Http();
    private Sync sync = new Sync();
    private Backfill backfill = new Backfill();

    @NotBlank
    private String zone = "America/Lima";

    @Data
    public static class Api {
        @NotBlank
        private String baseUrl;                 // p.ej. https://estadisticas.bcrp.gob.pe/estadisticas/series/api
        @NotBlank
        private String format = "json";         // json
        @NotBlank
        private String lang   = "esp";          // esp
    }

    @Data
    public static class Series {
        @NotBlank
        private String compra;                  // PD04637PD
        @NotBlank
        private String venta;                   // PD04638PD
    }

    @Data
    public static class Http {
        @Positive
        private int timeoutMs = 10000;
        @Positive
        private int connectTimeoutMs = 3000;
    }

    @Data
    public static class Sync {
        private boolean enabled = true;
        @NotBlank
        private String cron = "0 10 11 * * MON-FRI"; // IMPORTANTE: sin comentarios al final de la línea
    }

    @Data
    public static class Backfill {
        private boolean enabled = true;
        // opcionales; si se ponen, usar formato yyyy-MM-dd
        private String start;
        private String end;
    }
}
