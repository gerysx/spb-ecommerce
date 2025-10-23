package com.example.delogica.dtos.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Línea de pedido
 * Reglas: productId > 0 y quantity > 0
 */
@Data
@Schema(name = "OrderItemInput", description = "Datos de entrada para una línea del pedido")
public class OrderItemInputDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser un número positivo")
    @Schema(description = "Identificador del producto", example = "1001", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    @Schema(description = "Cantidad solicitada", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
