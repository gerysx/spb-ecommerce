package com.example.delogica.config.exceptions;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("El email '" + email + "' ya está en uso.");
    }
}