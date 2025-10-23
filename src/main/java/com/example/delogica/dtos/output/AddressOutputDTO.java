package com.example.delogica.dtos.output;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Address", description = "Dirección postal del cliente")
public class AddressOutputDTO {

    @Schema(description = "Identificador de la dirección", example = "45")
    private Long id;

    @Schema(description = "Línea 1 de la dirección", example = "Calle Mayor 10")
    private String line1;

    @Schema(description = "Línea 2 de la dirección", example = "Piso 3º B")
    private String line2;

    @Schema(description = "Ciudad", example = "Madrid")
    private String city;

    @Schema(description = "Código postal", example = "28013")
    private String postalCode;

    @Schema(description = "País en ISO-3166 alpha-2 o nombre común", example = "ES")
    private String country;

    @Schema(description = "Indica si es la dirección por defecto del cliente", example = "true")
    private Boolean defaultAddress;
}
