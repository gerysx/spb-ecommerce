package com.example.delogica.dtos.output;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "OrderItemSimpleOutput", description = "Línea simplificada del pedido para vistas resumidas")
public class OrderItemSimpleOutputDTO {

    @Schema(description = "Identificador del producto", example = "1001")
    private Long productId;

    @Schema(description = "Cantidad solicitada", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario al momento del pedido", example = "19.99")
    private BigDecimal unitPrice;

    @Schema(description = "Total de línea (unitPrice x quantity)", example = "39.98")
    public BigDecimal getLineTotal() {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
