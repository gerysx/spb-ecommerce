package com.example.delogica.dtos.output;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.delogica.models.OrderStatus;
import lombok.Data;

@Data
@Schema(name = "OrderSimpleOutput", description = "Representación resumida de un pedido para respuestas de creación o listados rápidos")
public class OrderSimpleOutputDTO {

    @Schema(description = "Identificador del pedido", example = "500")
    private Long id;

    @Schema(description = "Fecha y hora en que se realizó el pedido", example = "2025-10-23T09:15:30")
    private LocalDateTime orderDate;

    @Schema(description = "Estado actual del pedido", example = "CREATED")
    private OrderStatus status;

    @Schema(description = "Identificador del cliente asociado", example = "123")
    private Long customerId;

    @Schema(description = "Identificador de la dirección de envío", example = "45")
    private Long shippingAddressId;

    @Schema(description = "Líneas del pedido (versión simplificada)")
    private List<OrderItemSimpleOutputDTO> items;

    @Schema(description = "Total monetario del pedido", example = "59.98")
    private BigDecimal total;
}
