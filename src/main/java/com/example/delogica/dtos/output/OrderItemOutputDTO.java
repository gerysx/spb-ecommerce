package com.example.delogica.dtos.output;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "OrderItemOutput", description = "Representa una línea detallada de un pedido")
public class OrderItemOutputDTO {

    @Schema(description = "Identificador de la línea del pedido", example = "5001")
    private Long id;

    @Schema(description = "Producto asociado a la línea")
    private ProductOutputDTO product;

    @Schema(description = "Cantidad solicitada", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario del producto al momento del pedido", example = "19.99")
    private BigDecimal unitPrice;
}
