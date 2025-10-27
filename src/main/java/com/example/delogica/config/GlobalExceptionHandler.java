package com.example.delogica.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.delogica.config.errors.ErrorCode;
import com.example.delogica.config.errors.ErrorDetail;
import com.example.delogica.config.errors.ErrorResponse;
import com.example.delogica.config.exceptions.DefaultAddressChangeNotAllowedException;
import com.example.delogica.config.exceptions.EmailAlreadyInUseException;
import com.example.delogica.config.exceptions.InsufficientStockException;
import com.example.delogica.config.exceptions.JwtAuthenticationException;
import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.config.exceptions.SkuAlreadyInUseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // ===== 400 - VALIDATION (DTO @Valid body)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                logger.warn("Validación fallida en {}: {}", request.getRequestURI(), ex.getMessage());

                List<ErrorDetail> details = ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(err -> ErrorDetail.builder()
                                                .field(err.getField())
                                                .message(err.getDefaultMessage())
                                                .build())
                                .collect(Collectors.toList());

                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, request)
                                .message("Los datos enviados no superan la validación")
                                .details(details)
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // 400 - Validación de “cambiar dirección por defecto” (regla de negocio)
        @ExceptionHandler(DefaultAddressChangeNotAllowedException.class)
        public ResponseEntity<ErrorResponse> handleDefaultChange(DefaultAddressChangeNotAllowedException ex,
                        HttpServletRequest request) {
                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, request)
                                .message(ex.getMessage())
                                .details(List.of())
                                .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // 400 - @Validated en params/path
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex, HttpServletRequest request) {

                logger.warn("Constraint violation en {}: {}", request.getRequestURI(), ex.getMessage());

                List<ErrorDetail> details = ex.getConstraintViolations().stream()
                                .map(v -> ErrorDetail.builder()
                                                .field(v.getPropertyPath() != null ? v.getPropertyPath().toString()
                                                                : null)
                                                .message(v.getMessage())
                                                .build())
                                .collect(Collectors.toList());

                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, request)
                                .message("Los datos enviados no superan la validación")
                                .details(details)
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // 400 - body ilegible, tipos inválidos, parámetros ausentes, binding de params
        @ExceptionHandler({
                        HttpMessageNotReadableException.class,
                        MethodArgumentTypeMismatchException.class,
                        MissingServletRequestParameterException.class,
                        BindException.class
        })
        public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
                logger.warn("Solicitud inválida en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, request)
                                .message(safeMessage(ex, "Solicitud inválida"))
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // 400 - otras validaciones/estado de negocio no cubiertas arriba
        @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class,
                        InsufficientStockException.class })
        public ResponseEntity<ErrorResponse> handleBusiness400(RuntimeException ex, HttpServletRequest request) {
                logger.warn("Error de negocio en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, request)
                                .message(safeMessage(ex, "Solicitud inválida"))
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // ===== 404
        @ExceptionHandler({ ResourceNotFoundException.class, EntityNotFoundException.class })
        public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
                logger.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, request)
                                .message(safeMessage(ex, "Recurso no encontrado"))
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        // ===== 405
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

                logger.warn("Método HTTP no soportado en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.BAD_REQUEST, request)
                                .message("Método HTTP no soportado en esta ruta")
                                .build();

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
        }

        // ===== 409 (conflictos)
        @ExceptionHandler({
                        DataIntegrityViolationException.class,
                        EmailAlreadyInUseException.class,
                        SkuAlreadyInUseException.class
        })
        public ResponseEntity<ErrorResponse> handleConflict(Exception ex, HttpServletRequest request) {
                logger.warn("Conflicto en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.CONFLICT, ErrorCode.CONFLICT, request)
                                .message(safeMessage(ex,
                                                "Conflicto con el estado actual del recurso o la base de datos"))
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // ===== 500
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
                logger.error("Error interno en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

                ErrorResponse body = baseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, request)
                                .message("Ha ocurrido un error interno")
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }

        // ===== 500
        @ExceptionHandler(JwtAuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleJwtAuthentication(JwtAuthenticationException ex,
                        jakarta.servlet.http.HttpServletRequest request) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now().toString())
                                .path(request.getRequestURI())
                                .status(ex.getStatus().value())
                                .error(ex.getStatus().getReasonPhrase())
                                .code(ex.getCode())
                                .message(ex.getMessage())
                                .build();

                return ResponseEntity.status(ex.getStatus()).body(response);
        }

        // ===== Helpers
        private ErrorResponse.ErrorResponseBuilder baseBuilder(
                        HttpStatus status, ErrorCode code, HttpServletRequest request) {
                return ErrorResponse.builder()
                                .timestamp(Instant.now().toString())
                                .path(request.getRequestURI())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .code(code)
                                .details(null);
        }

        private String safeMessage(Exception ex, String fallback) {
                String msg = ex.getMessage();
                return (msg == null || msg.isBlank()) ? fallback : msg;
        }
}
