package com.cambiosmart.demo.spring.boot;

import com.cambiosmart.demo.spring.boot.bcrp.TasaSyncService;
import com.cambiosmart.demo.spring.boot.config.BcrpProperties;
import com.cambiosmart.demo.spring.boot.config.TasaMarginProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ BcrpProperties.class, TasaMarginProperties.class })
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// Backfill automático al iniciar (si está habilitado en application.properties)
	@Bean
	CommandLineRunner preload(TasaSyncService svc, BcrpProperties props) {
		return args -> {
			try {
				var bf = props.getBackfill();
				if (bf != null && bf.isEnabled()) {
					int n = svc.backfillFromProps();
					log.info("Backfill al inicio -> {} registros ({} .. {})",
							n, bf.getStart(), bf.getEnd());
				} else {
					log.info("Backfill al inicio: deshabilitado (bcrp.backfill.enabled=false)");
				}
			} catch (Exception ex) {
				// Importante: no bloquear el arranque si falla la integración
				log.error("Backfill al inicio falló (no bloquea el arranque): {}", ex.toString(), ex);
			}
		};
	}
}
