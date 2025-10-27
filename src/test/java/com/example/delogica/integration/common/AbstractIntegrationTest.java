package com.example.delogica.integration.common;

import com.example.delogica.ApiCommerceApplication;
import com.example.delogica.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Clase base para todos los tests de integración.
 * Genera un JWT válido con JwtUtil, sin tocar la base de datos.
 */
@SpringBootTest(classes = ApiCommerceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;  // Usa el bean real del sistema

    protected String authHeader;

    @BeforeEach
    void setupJwt() {
        // Generamos un token de prueba con un usuario fijo
        String token = jwtUtil.generateToken("integrationTestUser");
        this.authHeader = "Bearer " + token;
    }

    // -----------------------------
    // Métodos helper autenticados
    // -----------------------------
    protected MockHttpServletRequestBuilder authGet(String url, Object... uriVars) {
        return get(url, uriVars).header("Authorization", authHeader);
    }

    protected MockHttpServletRequestBuilder authPost(String url, Object... uriVars) {
        return post(url, uriVars).header("Authorization", authHeader);
    }

    protected MockHttpServletRequestBuilder authPut(String url, Object... uriVars) {
        return put(url, uriVars).header("Authorization", authHeader);
    }

    protected MockHttpServletRequestBuilder authDelete(String url, Object... uriVars) {
        return delete(url, uriVars).header("Authorization", authHeader);
    }
}
