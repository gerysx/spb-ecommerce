package com.example.delogica.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles del pedido")
public enum OrderStatus {

    @Schema(description = "Pedido creado, pendiente de pago o procesamiento")
    CREATED,

    @Schema(description = "Pedido pagado, pendiente de envío")
    PAID,

    @Schema(description = "Pedido enviado al cliente")
    SHIPPED,

    @Schema(description = "Pedido cancelado antes de ser completado")
    CANCELLED
}
