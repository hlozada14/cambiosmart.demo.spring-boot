package com.cambiosmart.demo.spring.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Validated
@ConfigurationProperties(prefix = "tasa.margin")
public class TasaMarginProperties {
    @NotNull
    private BigDecimal compra = new BigDecimal("0.00"); // se SUMA a compra publicada
    @NotNull
    private BigDecimal venta  = new BigDecimal("0.00"); // se RESTA a venta publicada
    @Min(0)
    private int roundScale = 4;
}