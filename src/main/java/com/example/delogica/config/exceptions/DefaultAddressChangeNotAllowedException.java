package com.example.delogica.config.exceptions;

public class DefaultAddressChangeNotAllowedException extends RuntimeException {
    public DefaultAddressChangeNotAllowedException() {
        super("Para cambiar la dirección por defecto usa el endpoint /api/customers/{id}/addresses/{addressId}/default");
    }
}
