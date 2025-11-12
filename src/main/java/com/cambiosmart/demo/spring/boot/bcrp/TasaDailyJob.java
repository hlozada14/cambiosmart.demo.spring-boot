package com.cambiosmart.demo.spring.boot.bcrp;

import com.cambiosmart.demo.spring.boot.config.BcrpProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "bcrp.sync.enabled", havingValue = "true", matchIfMissing = true)
public class TasaDailyJob {
    private static final Logger log = LoggerFactory.getLogger(TasaDailyJob.class);

    private final TasaSyncService service;
    private final BcrpProperties props;

    @Scheduled(cron = "${bcrp.sync.cron}", zone = "${bcrp.zone:America/Lima}")
    public void run() {
        ZoneId zone = ZoneId.of(props.getZone());
        LocalDate hoy = LocalDate.now(zone);
        LocalDate desde = hoy.minusDays(3);
        log.info("TasaDailyJob disparado. Rango: {} .. {}", desde, hoy);
        int n = service.backfill(desde, hoy);
        log.info("TasaDailyJob upserts: {}", n);
    }
}