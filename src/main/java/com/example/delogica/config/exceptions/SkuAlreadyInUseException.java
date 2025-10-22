package com.example.delogica.config.exceptions;

public class SkuAlreadyInUseException extends RuntimeException {

    public SkuAlreadyInUseException(String sku) {
        super("SKU '" + sku + "' ya está en uso.");
    }
}