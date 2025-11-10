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

    private static final long REMEMBER_ME_EXPIRATION = 7L * 24 * 60 * 60 * 1000; // 7 días

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (usuarioService.existsByEmail(request.getEmail())) {
            throw new AuthException.UserAlreadyExistsException("El email ya está registrado");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException("Las contraseñas no coinciden");
        }

        var usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());
        usuario.setPassword(request.getPassword());
        usuario.setRole(Role.USER);

        usuario = usuarioService.save(usuario);

        var jwtToken = jwtService.generateToken(usuario); // expiración por defecto

        return new AuthDTO.AuthResponse(
                jwtToken,
                "Bearer",
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }

    public AuthDTO.AuthResponse authenticate(AuthDTO.LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),           // principal = email
                            request.getPassword()
                    )
            );

            var usuario = (Usuario) authentication.getPrincipal();

            // Si "recordarme" es true, emitimos un token más largo
            String jwtToken = (request.getRememberMe() != null && request.getRememberMe())
                    ? jwtService.generateToken(usuario, REMEMBER_ME_EXPIRATION)
                    : jwtService.generateToken(usuario);

            return new AuthDTO.AuthResponse(
                    jwtToken,
                    "Bearer",
                    usuario.getId(),
                    usuario.getNombreCompleto(),
                    usuario.getEmail(),
                    usuario.getRole().name()
            );
        } catch (BadCredentialsException e) {
            throw new AuthException.InvalidCredentialsException();
        }
    }
}
