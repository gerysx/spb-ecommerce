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

import com.example.delogica.config.errors.ErrorCode;
import com.example.delogica.config.errors.ErrorDetail;
import com.example.delogica.config.errors.ErrorResponse;
import com.example.delogica.config.exceptions.DefaultAddressChangeNotAllowedException;
import com.example.delogica.config.exceptions.EmailAlreadyInUseException;
import com.example.delogica.config.exceptions.InsufficientStockException;
import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.config.exceptions.SkuAlreadyInUseException;

import org.springframework.validation.BindException;

import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // ==== 400 BAD REQUEST ====

        // Validación @Valid en body (DTOs)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                logger.warn("Validación fallida en {}: {}", request.getRequestURI(), ex.getMessage());

                List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
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

        // Validación UPDATE ADDRESS DEFAULT (DTOs)
        @ExceptionHandler(DefaultAddressChangeNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleDefaultChange(
            DefaultAddressChangeNotAllowedException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code(ErrorCode.BAD_REQUEST)
                .message(ex.getMessage())
                .details(List.of()) // puedes añadir detalles si los tuvieras
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
        // Validación @Validated en params, path variables, etc.
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

        // Errores de lectura de body o tipos incorrectos en params, query, etc.
        @ExceptionHandler({
                        HttpMessageNotReadableException.class,
                        MethodArgumentTypeMismatchException.class,
                        MissingServletRequestParameterException.class,
                        BindException.class
        })
        public ResponseEntity<ErrorResponse> handleBadRequest(
                        Exception ex, HttpServletRequest request) {

                logger.warn("Solicitud inválida en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, request)
                                .message(mensajeSeguro(ex, "Solicitud inválida"))
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // Excepciones de negocio / validaciones manuales
        @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        RuntimeException ex, HttpServletRequest request) {

                logger.warn("Error de negocio en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, request)
                                .message(mensajeSeguro(ex, "Solicitud inválida"))
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // Excepción personalizada: stock insuficiente
        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<ErrorResponse> handleInsufficientStock(
                        InsufficientStockException ex, HttpServletRequest request) {

                logger.warn("Stock insuficiente en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, request)
                                .message(ex.getMessage())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // ==== 404 NOT FOUND ====

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(
                        EntityNotFoundException ex, HttpServletRequest request) {

                logger.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, request)
                                .message(mensajeSeguro(ex, "Recurso no encontrado"))
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
                        ResourceNotFoundException ex, HttpServletRequest request) {

                logger.warn("Recurso no encontrado personalizado en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, request)
                                .message(ex.getMessage())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        // ==== 405 METHOD NOT ALLOWED ====

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

                logger.warn("Método HTTP no soportado en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.BAD_REQUEST, request)
                                .message("Método HTTP no soportado en esta ruta")
                                .build();

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
        }

        // ==== 409 CONFLICT ====

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleConflict(
                        DataIntegrityViolationException ex, HttpServletRequest request) {

                logger.warn("Conflicto en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.CONFLICT, ErrorCode.CONFLICT, request)
                                .message("Conflicto con el estado actual del recurso o la base de datos")
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        @ExceptionHandler(EmailAlreadyInUseException.class)
        public ResponseEntity<ErrorResponse> handleEmailAlreadyInUse(
                        EmailAlreadyInUseException ex, HttpServletRequest request) {

                logger.warn("Email ya en uso en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.CONFLICT, ErrorCode.CONFLICT, request)
                                .message(ex.getMessage())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        @ExceptionHandler(SkuAlreadyInUseException.class)
        public ResponseEntity<ErrorResponse> handleSkuAlreadyInUse(
                        SkuAlreadyInUseException ex, HttpServletRequest request) {

                logger.warn("SKU ya en uso en {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse body = baseBuilder(HttpStatus.CONFLICT, ErrorCode.CONFLICT, request)
                                .message(ex.getMessage())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // ==== 500 INTERNAL SERVER ERROR ====

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleAll(
                        Exception ex, HttpServletRequest request) {

                logger.error("Error interno en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

                ErrorResponse body = baseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, request)
                                .message("Ha ocurrido un error interno")
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }

        // ==== Helpers ====

        private ErrorResponse.ErrorResponseBuilder baseBuilder(
                        HttpStatus status, ErrorCode code, HttpServletRequest request) {
                return ErrorResponse.builder()
                                .timestamp(Instant.now().toString())
                                .path(request.getRequestURI())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .code(code)
                                .message(status.getReasonPhrase())
                                .details(null);
                // .traceId(null); dejar null para omitirlo en JSON
        }

        private String mensajeSeguro(Exception ex, String porDefecto) {
                String msg = ex.getMessage();
                return (msg == null || msg.isBlank()) ? porDefecto : msg;
        }
}
