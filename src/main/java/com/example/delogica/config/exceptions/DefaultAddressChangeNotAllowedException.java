package com.example.delogica.config.exceptions;


public class DefaultAddressChangeNotAllowedException extends RuntimeException {
    public DefaultAddressChangeNotAllowedException(String msj) {
        super("No se permite cambiar la dirección por defecto desde esta operación.");
    }
}
