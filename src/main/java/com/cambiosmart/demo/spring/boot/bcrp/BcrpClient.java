package com.cambiosmart.demo.spring.boot.bcrp;

import com.cambiosmart.demo.spring.boot.config.BcrpProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class BcrpClient {

    private static final Logger log = LoggerFactory.getLogger(BcrpClient.class);
    private final BcrpProperties props;

    private RestTemplate rt() {
        var f = new SimpleClientHttpRequestFactory();
        f.setReadTimeout(props.getHttp().getTimeoutMs());
        f.setConnectTimeout(props.getHttp().getConnectTimeoutMs());
        return new RestTemplate(f);
    }

    public String fetchCompra(String start, String end) {
        return callSeries(props.getSeries().getCompra(), start, end);
    }

    public String fetchVenta(String start, String end) {
        return callSeries(props.getSeries().getVenta(), start, end);
    }

    private String callSeries(String serie, String start, String end) {
        String base = props.getApi().getBaseUrl();
        String format = props.getApi().getFormat();
        String lang = props.getApi().getLang();

        String url = (start == null || start.isBlank() || end == null || end.isBlank())
                ? String.format("%s/%s/%s/%s", base, serie, format, lang)
                : String.format("%s/%s/%s/%s/%s/%s", base, serie, format, start, end, lang);

        log.info("BCRP URL [{}]: {}", serie, url);
        try {
            String body = rt().getForObject(url, String.class);
            int len = (body == null ? 0 : body.length());
            log.info("BCRP body [{}] len={}", serie, len);
            return (body == null ? "" : body);
        } catch (RestClientResponseException ex) {
            log.error("BCRP [{}] HTTP {} {}. Body: {}",
                    serie, ex.getRawStatusCode(), ex.getStatusText(), ex.getResponseBodyAsString());
            return "";
        } catch (ResourceAccessException ex) {
            log.error("BCRP [{}] acceso falló (timeout/conexión): {}", serie, ex.getMessage());
            return "";
        } catch (Exception ex) {
            log.error("BCRP [{}] error inesperado: {}", serie, ex.toString(), ex);
            return "";
        }
    }
}
