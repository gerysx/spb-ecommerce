package com.example.delogica.exceptions;

import com.example.delogica.config.GlobalExceptionHandler;
import com.example.delogica.config.errors.*;
import com.example.delogica.config.exceptions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @SuppressWarnings("null")
    @Test
    void handleConstraintViolation_shouldReturn400() {
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        jakarta.validation.Path path = Mockito.mock(jakarta.validation.Path.class);
        Mockito.when(path.toString()).thenReturn("field");
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("Debe ser positivo");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.VALIDATION_ERROR, response.getBody().getCode());
    }

    @SuppressWarnings("null")
    @Test
    void handleNotFound_shouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Cliente no encontrado");
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Cliente"));
    }

    @SuppressWarnings("null")
    @Test
    void handleConflict_shouldReturn409() {
        EmailAlreadyInUseException ex = new EmailAlreadyInUseException("Email duplicado");
        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertNotNull(response.getBody());
        ErrorResponse body = response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.CONFLICT, body.getCode());
        assertTrue(body.getMessage().contains("Email duplicado"));
    }

    @SuppressWarnings("null")
    @Test
    void handleIllegalArgument_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");
        ResponseEntity<ErrorResponse> response = handler.handleBusiness400(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.BAD_REQUEST, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("inválido"));
    }

    @SuppressWarnings("null")
    @Test
    void handleMethodNotSupported_shouldReturn405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST", List.of("GET"));

        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Método HTTP no soportado en esta ruta", response.getBody().getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void handleInternalException_shouldReturn500() {
        Exception ex = new Exception("Fallo inesperado");
        ResponseEntity<ErrorResponse> response = handler.handleAll(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_ERROR, response.getBody().getCode());
        assertEquals("Ha ocurrido un error interno", response.getBody().getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void handleJwtAuthentication_shouldReturnCustomStatus() {
        JwtAuthenticationException ex = new JwtAuthenticationException("Token inválido", ErrorCode.BAD_REQUEST);

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertNotNull(response.getBody()); // <— elimina el warning del IDE
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.CONFLICT, response.getBody().getCode());
    }

    @SuppressWarnings("null")
    @Test
    void handleDefaultAddressChangeNotAllowed_shouldReturn400() {
        DefaultAddressChangeNotAllowedException ex = new DefaultAddressChangeNotAllowedException(
                "No se puede cambiar dirección por defecto");

        ResponseEntity<ErrorResponse> response = handler.handleDefaultChange(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.VALIDATION_ERROR, response.getBody().getCode());
    }
}
