package com.cambiosmart.demo.spring.boot.tasas;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TasaService {
        private final TasaRepository TasaRepository;

        public TasaService(TasaRepository TasaRepository) {
            this.TasaRepository = TasaRepository;
        }

        public List<Tasas> listarTodos() {
            return TasaRepository.findAll();
        }

        public Optional<Tasas> obtenerPorId(Long id) {
            return TasaRepository.findById(id);
        }

        public Tasas guardar(Tasas tasas) {
            return TasaRepository.save(tasas);
        }

        public void eliminar(Long id) {
            TasaRepository.deleteById(id);
        }
}
