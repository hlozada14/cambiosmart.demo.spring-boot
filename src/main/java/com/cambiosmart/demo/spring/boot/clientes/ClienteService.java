package com.cambiosmart.demo.spring.boot.clientes;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepository.findById(id);
    }
    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }
    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }
}
