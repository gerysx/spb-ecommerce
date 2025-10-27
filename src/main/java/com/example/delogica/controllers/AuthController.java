package com.example.delogica.controllers;

import com.example.delogica.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Operaciones de autenticación y validación de tokens JWT")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Operation(
        summary = "Generar token JWT",
        description = "Genera un token JWT válido para el nombre de usuario proporcionado. "
                    + "Este token deberá ser usado en las cabeceras Authorization de las demás peticiones.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                examples = @ExampleObject(name = "Ejemplo de login simple", value = "username=test")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Token generado correctamente",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9...\"}"))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
        }
    )
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, String>> login(@RequestParam String username) {
        logger.info("Generando token JWT para usuario '{}'", username);
        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Operation(
        summary = "Validar token JWT",
        description = "Verifica si el token JWT incluido en la cabecera Authorization es válido y no ha expirado.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Token validado correctamente",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"valid\": true, \"username\": \"test\"}"))),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"valid\": false, \"message\": \"Token no proporcionado\"}")))
        }
    )
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestHeader(value = "Authorization", required = false)
            @Schema(description = "Cabecera de autenticación en formato 'Bearer {token}'")
            String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Token no proporcionado o formato inválido");
            return ResponseEntity.status(401).body(Map.of(
                    "valid", false,
                    "message", "Token no proporcionado"
            ));
        }

        String token = authHeader.substring(7);
        boolean valid = jwtUtil.isTokenValid(token);
        String username = valid ? jwtUtil.extractUsername(token) : null;

        logger.info("Validación de token: válido={}, usuario={}", valid, username);
        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "username", username
        ));
    }
}
