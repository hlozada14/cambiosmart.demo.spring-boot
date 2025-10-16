package com.cambiosmart.demo.spring.boot.controllers;

import com.cambiosmart.demo.spring.boot.tasas.Tasas;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping ("/api/tasas")
public class TasasController {
    @GetMapping
    public List<Tasas> ListarProductos() {
        return List.of(
            new Tasas(1L, "USD", 3.50, 3.60, "2025-10-16", "BCRP", "12:00", "Bancario")
        );
    }
}
