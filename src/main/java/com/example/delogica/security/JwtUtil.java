package com.example.delogica.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para la generación, validación y análisis de tokens JWT.
 * <p>
 * Gestiona la clave secreta y el tiempo de expiración configurado
 * en las propiedades de la aplicación.
 * </p>
 */
@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;
    private SecretKey secretKey;

    /**
     * Constructor que inicializa los valores de configuración JWT.
     *
     * @param secret        Clave secreta definida en la configuración.
     * @param expirationMs  Tiempo de expiración del token en milisegundos.
     */
    public JwtUtil(
            @Value("${security.jwt.secret:}") String secret,
            @Value("${security.jwt.expiration-ms:3600000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    /**
     * Inicializa la clave secreta JWT después de la construcción del bean.
     * <p>
     * Si la clave configurada es nula o demasiado corta, se utiliza
     * una clave por defecto adecuada solo para entornos de desarrollo o pruebas.
     * </p>
     */
    @PostConstruct
    private void init() {
        // Fallback seguro: si no hay clave o es demasiado corta, usar una fija solo para testing
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            String fallbackSecret = "default-test-secret-key-123456789012345678901234";
            this.secretKey = Keys.hmacShaKeyFor(fallbackSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Genera un token JWT firmado para un usuario específico.
     *
     * @param username  Nombre de usuario que se incluirá como sujeto del token.
     * @return Token JWT firmado y con tiempo de expiración configurado.
     * @throws IllegalStateException si la clave secreta no está inicializada.
     */
    public String generateToken(String username) {
        if (secretKey == null) {
            throw new IllegalStateException("secretKey no inicializado, no se puede generar token");
        }

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica si un token JWT es válido y no ha expirado.
     *
     * @param token  Token JWT a validar.
     * @return {@code true} si el token es válido, {@code false} si está expirado o alterado.
     */
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

    /**
     * Extrae el nombre de usuario del cuerpo de un token JWT.
     *
     * @param token  Token JWT del cual se extraerá el nombre de usuario.
     * @return Nombre de usuario contenido en el token.
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
