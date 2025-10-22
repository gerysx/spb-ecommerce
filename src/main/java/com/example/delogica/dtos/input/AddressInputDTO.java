package com.example.delogica.dtos.input;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressInputDTO {

    private Long id;

    @NotBlank(message = "La línea 1 de la dirección es obligatoria")
    @Size(max = 160, message = "La línea 1 no puede superar los 160 caracteres")
    private String line1;

    @Size(max = 160, message = "La línea 2 no puede superar los 160 caracteres")
    private String line2;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80)
    private String city;

    @NotBlank(message = "El código postal es obligatorio")
    @Size(max = 20)
    private String postalCode;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 80)
    private String country;

    private Boolean defaultAddress;
}