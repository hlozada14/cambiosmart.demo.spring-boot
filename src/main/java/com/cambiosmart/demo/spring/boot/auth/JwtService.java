package com.cambiosmart.demo.spring.boot.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpirationMillis;

    private Key getKey() {
        // Usando Base64 decode ya que la clave está en Base64
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String generateToken(UserDetails userDetails) {
        // Incluir rol en el token para evitar consultas adicionales
        Map<String, Object> extraClaims = Map.of(
                "role", userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("ROLE_USER")
        );

        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(extraClaims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtExpirationMillis)))
                .signWith(getKey()) // Nuevo API sin SignatureAlgorithm explícito
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            var claims = Jwts.parser()
                    .setSigningKey(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject().equals(userDetails.getUsername()) &&
                    claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractRole(String token) {
        try {
            var claims = Jwts.parser()
                    .setSigningKey(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
    // ...deja los imports y la clase como está...
    // Añade este metodo dentro de JwtService:
    public String generateToken(UserDetails userDetails, long customExpirationMillis) {
        var extraClaims = Map.of(
                "role", userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("ROLE_USER")
        );
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(extraClaims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(customExpirationMillis)))
                .signWith(getKey())
                .compact();
    }
}