package com.example.delogica.dtos.output;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.delogica.models.OrderStatus;
import lombok.Data;

@Data
@Schema(name = "OrderOutput", description = "Representación detallada de un pedido con cliente, dirección, ítems y total")
public class OrderOutputDTO {

    @Schema(description = "Identificador único del pedido", example = "500")
    private Long id;

    @Schema(description = "Cliente asociado al pedido")
    private CustomerOutputDTO customer;

    @Schema(description = "Dirección de envío utilizada en el pedido")
    private AddressOutputDTO shippingAddress;

    @Schema(description = "Fecha y hora en que se generó el pedido", example = "2025-10-23T09:15:30")
    private LocalDateTime orderDate;

    @Schema(description = "Estado actual del pedido", example = "PAID")
    private OrderStatus status;

    @Schema(description = "Total del pedido calculado en base a sus ítems", example = "89.90")
    private BigDecimal total;

    @Schema(description = "Líneas de pedido con detalles de producto y precios")
    private List<OrderItemOutputDTO> items = new ArrayList<>();
}
