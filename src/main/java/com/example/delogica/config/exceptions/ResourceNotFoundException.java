package com.example.delogica.config.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forId(Class<?> resourceClass, Long id) {
        return new ResourceNotFoundException(resourceClass.getSimpleName() + " con ID " + id + " no encontrado.");
    }
}
