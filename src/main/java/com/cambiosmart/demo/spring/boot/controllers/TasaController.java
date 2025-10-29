package com.cambiosmart.demo.spring.boot.controllers;

import com.cambiosmart.demo.spring.boot.tasas.Tasas;
import com.cambiosmart.demo.spring.boot.tasas.TasaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasas")
public class TasaController {
    private final TasaService tasaService;

    public TasaController(TasaService tasaService) {
        this.tasaService = tasaService;
    }

    @GetMapping
    public List<Tasas> obtenerTodasLasTasas() {
        return tasaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tasas> obtenerTasaPorId(@PathVariable Long id) {
        return tasaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Tasas crearTasa(@RequestBody Tasas tasas) {
        return tasaService.guardar(tasas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tasas> actualizarTasa(@PathVariable Long id, @RequestBody Tasas tasas) {
        return tasaService.obtenerPorId(id)
                .map (p -> {
                    tasas.setId(id);
                    return ResponseEntity.ok(tasaService.guardar(tasas));
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTasa(@PathVariable Long id) {
        if (tasaService.obtenerPorId(id).isPresent()) {
            tasaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
