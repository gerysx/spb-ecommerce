package com.example.delogica.config.exceptions;

import com.example.delogica.config.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtAuthenticationException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode code;

    public JwtAuthenticationException(String message, ErrorCode code) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getCode() {
        return code;
    }
}
