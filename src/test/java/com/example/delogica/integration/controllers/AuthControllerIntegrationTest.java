package com.example.delogica.integration.controllers;

import com.example.delogica.ApiCommerceApplication;
import com.example.delogica.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ApiCommerceApplication.class)
@ActiveProfiles("testing")
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;

    @BeforeEach
    void initToken() throws Exception {
        // 🔐 Genera token mediante /login
        MvcResult result = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        validToken = json.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        assertThat(validToken).isNotBlank();
    }

    @Test
    @DisplayName("🔹 Login genera token válido y verificable")
    void login_generatesValidToken() {
        assertThat(jwtUtil.isTokenValid(validToken)).isTrue();
        assertThat(jwtUtil.extractUsername(validToken)).isEqualTo("tester");
    }

    @Test
    @DisplayName("🔹 /validate con token válido devuelve 200 y username")
    void validate_withValidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("tester"));
    }

    @Test
    @DisplayName("🔹 /validate sin token devuelve 401 con mensaje correcto")
    void validate_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Token no proporcionado"));
    }

    @Test
    @DisplayName("🔹 /validate con token inválido devuelve 401")
    void validate_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("🔹 JwtUtil genera y valida token manualmente")
    void jwtUtil_generateAndValidateManual() {
        String token = jwtUtil.generateToken("manualUser");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("manualUser");
    }
}
