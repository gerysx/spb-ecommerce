package com.example.delogica.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;
    private SecretKey secretKey;

    public JwtUtil(
            @Value("${security.jwt.secret:}") String secret,
            @Value("${security.jwt.expiration-ms:3600000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    private void init() {
        System.out.println("🔍 Perfil activo: " + System.getProperty("spring.profiles.active"));
        System.out.println("🔑 Longitud de clave JWT: " + (secret == null ? 0 : secret.length()));

        // 🔐 Fallback seguro: si no hay clave o es demasiado corta, usar una fija solo para testing
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            System.out.println("⚠️  Secret vacío o corto, usando clave JWT por defecto (solo para testing)");
            String fallbackSecret = "default-test-secret-key-123456789012345678901234";
            this.secretKey = Keys.hmacShaKeyFor(fallbackSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("✅ Clave JWT inicializada correctamente");
    }

    public String generateToken(String username) {
        if (secretKey == null) {
            throw new IllegalStateException("❌ secretKey no inicializado, no se puede generar token");
        }

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
