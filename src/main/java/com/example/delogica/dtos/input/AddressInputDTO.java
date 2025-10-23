package com.example.delogica.dtos.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload para crear/actualizar direcciones del cliente
 * Nota: en POST /api/customers/{id}/addresses el campo defaultAddress del payload se ignora y,
 * según tu servicio, lanzarás 409 si intentan forzarlo en true
 */
@Data
@Schema(name = "AddressInput", description = "Datos de entrada para la dirección del cliente")
public class AddressInputDTO {

    @Schema(description = "Identificador de la dirección. Presente solo en updates", example = "45", accessMode = Schema.AccessMode.READ_WRITE)
    private Long id;

    @NotBlank(message = "La línea 1 de la dirección es obligatoria")
    @Size(max = 160, message = "La línea 1 no puede superar los 160 caracteres")
    @Schema(description = "Línea 1 de la dirección", example = "Calle Mayor 10", maxLength = 160)
    private String line1;

    @Size(max = 160, message = "La línea 2 no puede superar los 160 caracteres")
    @Schema(description = "Línea 2 de la dirección", example = "Piso 3º B", maxLength = 160, nullable = true)
    private String line2;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80)
    @Schema(description = "Ciudad", example = "Madrid", maxLength = 80)
    private String city;

    @NotBlank(message = "El código postal es obligatorio")
    @Size(max = 20)
    @Schema(description = "Código postal", example = "28013", maxLength = 20)
    private String postalCode;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 80)
    @Schema(description = "País (ISO-3166 alpha-2 o nombre)", example = "ES", maxLength = 80)
    private String country;

    @Schema(
        description = "Intento de marcar como predeterminada. En POST /{id}/addresses no está permitido. En PUT /customers se valida contra el estado real",
        example = "false",
        nullable = true
    )
    private Boolean defaultAddress;
}
