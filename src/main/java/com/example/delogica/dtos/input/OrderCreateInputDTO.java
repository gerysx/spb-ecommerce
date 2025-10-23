package com.example.delogica.dtos.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Payload para crear un pedido con sus líneas
 * Reglas: requiere customerId, shippingAddressId y al menos un ítem
 */
@Data
@Schema(name = "OrderCreateInput", description = "Datos de entrada para la creación de un pedido")
public class OrderCreateInputDTO {

    @NotNull
    @Schema(description = "Identificador del cliente", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @NotNull
    @Schema(description = "Identificador de la dirección de envío (debe pertenecer al cliente)", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long shippingAddressId;

    @NotEmpty
    @Valid
    @Schema(description = "Líneas del pedido, al menos una", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemInputDTO> items;
}
