package com.example.delogica.dtos.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "OrderStatusInput", description = "Nuevo estado para un pedido")
public class OrderStatusInputDTO {

    @NotNull(message = "El nuevo estado no puede ser nulo")
    @Schema(description = "Nuevo estado del pedido", example = "PAID")
    private String status;

}