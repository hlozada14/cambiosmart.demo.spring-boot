package com.cambiosmart.demo.spring.boot.auth;

import com.cambiosmart.demo.spring.boot.usuario.Role;
import com.cambiosmart.demo.spring.boot.usuario.Usuario;
import com.cambiosmart.demo.spring.boot.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {

        if (usuarioService.existsByUsername(request.getUsername())) {
            throw new AuthException.UserAlreadyExistsException("El nombre de usuario ya existe");
        }

        if (usuarioService.existsByEmail(request.getEmail())) {
            throw new AuthException.UserAlreadyExistsException("El email ya está registrado");
        }

        var usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setRole(Role.USER);

        usuario = usuarioService.save(usuario);

        var jwtToken = jwtService.generateToken(usuario);

        return new AuthDTO.AuthResponse(
                jwtToken,
                "Bearer",
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }

    public AuthDTO.AuthResponse authenticate(AuthDTO.LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            var usuario = (Usuario) authentication.getPrincipal();
            var jwtToken = jwtService.generateToken(usuario);

            return new AuthDTO.AuthResponse(
                    jwtToken,
                    "Bearer",
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getEmail(),
                    usuario.getRole().name()
            );
        } catch (BadCredentialsException e) {
            throw new AuthException.InvalidCredentialsException();
        }
    }
}