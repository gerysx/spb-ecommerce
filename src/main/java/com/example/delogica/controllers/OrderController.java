package com.example.delogica.controllers;

import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.input.OrderStatusInputDTO;
import com.example.delogica.dtos.output.OrderOutputDTO;
import com.example.delogica.dtos.output.OrderSimpleOutputDTO;
import com.example.delogica.models.OrderStatus;
import com.example.delogica.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
@Validated
@Tag(name = "Orders", description = "Operaciones de gestión de pedidos")
public class OrderController {

        private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
        private final OrderService orderService;

        public OrderController(OrderService orderService) {
                this.orderService = orderService;
        }

        /**
         * Crea un pedido con sus líneas
         */
        @Operation(summary = "Crear pedido", description = "Crea un pedido para un cliente con su dirección de envío y líneas. Valida stock, productos activos y pertenencia de la dirección al cliente", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = OrderCreateInputDTO.class), examples = {
                        @ExampleObject(name = "Pedido mínimo", value = """
                                        {
                                          "customerId": 123,
                                          "shippingAddressId": 45,
                                          "items": [
                                            { "productId": 1001, "quantity": 2 }
                                          ]
                                        }
                                        """),
                        @ExampleObject(name = "Pedido con varias líneas", value = """
                                        {
                                          "customerId": 123,
                                          "shippingAddressId": 45,
                                          "items": [
                                            { "productId": 1001, "quantity": 2 },
                                            { "productId": 1002, "quantity": 1 }
                                          ]
                                        }
                                        """)
        })))
        @ApiResponse(responseCode = "201", description = "Pedido creado", content = @Content(schema = @Schema(implementation = OrderSimpleOutputDTO.class)))
        @ApiResponse(responseCode = "400", description = "Entrada inválida (validación, stock insuficiente, producto inactivo, dirección no pertenece al cliente)", content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Dirección o producto no encontrado", content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))

        @PostMapping
        public ResponseEntity<OrderSimpleOutputDTO> createOrder(@Valid @RequestBody OrderCreateInputDTO orderInputDTO) {
                logger.info("Recibida petición POST /api/orders para crear pedido clienteId={}",
                                orderInputDTO.getCustomerId());
                OrderSimpleOutputDTO createdOrder = orderService.create(orderInputDTO);
                logger.info("Pedido creado con ID {}", createdOrder.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        }

        /**
         * Lista pedidos con filtros opcionales
         */
        @Operation(summary = "Buscar pedidos (paginado)", description = "Devuelve una página de pedidos filtrando opcionalmente por cliente, rango de fecha y estado")
        @ApiResponse(responseCode = "200", description = "Página de pedidos", content = @Content(schema = @Schema(implementation = OrderOutputDTO.class)))
        @PageableAsQueryParam
        @GetMapping
        public ResponseEntity<Page<OrderOutputDTO>> searchOrders(
                        @ParameterObject Pageable pageable,
                        @Parameter(description = "ID del cliente", example = "123") @RequestParam(required = false) Long customerId,
                        @Parameter(description = "Fecha/hora desde (ISO-8601). Ej: 2025-10-01T00:00:00", example = "2025-10-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
                        @Parameter(description = "Fecha/hora hasta (ISO-8601). Ej: 2025-10-23T23:59:59", example = "2025-10-23T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
                        @Parameter(description = "Estado del pedido", examples = {
                                        @ExampleObject(name = "CREATED", value = "CREATED"),
                                        @ExampleObject(name = "PAID", value = "PAID"),
                                        @ExampleObject(name = "SHIPPED", value = "SHIPPED"),
                                        @ExampleObject(name = "CANCELLED", value = "CANCELLED")
                        }) @RequestParam(required = false) OrderStatus status) {
                logger.info("Recibida petición GET /api/orders con filtros: customerId={}, fromDate={}, toDate={}, status={}",
                                customerId, fromDate, toDate, status);
                Page<OrderOutputDTO> ordersPage = orderService.search(pageable, customerId, fromDate, toDate, status);
                logger.info("Resultado: {} pedidos encontrados", ordersPage.getTotalElements());
                return ResponseEntity.ok(ordersPage);
        }

        /**
         * Obtiene los detalles de un pedido por ID
         */
        @Operation(summary = "Obtener pedido por ID", description = "Devuelve el pedido con sus líneas y totales")
        @ApiResponse(responseCode = "200", description = "Pedido encontrado", content = @Content(schema = @Schema(implementation = OrderOutputDTO.class)))
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
        @GetMapping("/{id}")
        public ResponseEntity<OrderOutputDTO> getOrderById(
                        @Parameter(in = ParameterIn.PATH, description = "Identificador del pedido", example = "500") @PathVariable Long id) {
                logger.info("Recibida petición GET /api/orders/{} para obtener detalles del pedido", id);
                OrderOutputDTO order = orderService.getById(id);
                logger.info("Detalle pedido obtenido para ID {}", id);
                return ResponseEntity.ok(order);
        }

        /**
         * Cambia el estado del pedido siguiendo transiciones válidas.
         */
        @Operation(summary = "Cambiar estado del pedido", description = """
                        Cambia el estado del pedido si la transición es válida.
                        Transiciones válidas:
                        - CREATED → PAID | CANCELLED
                        - PAID → SHIPPED | CANCELLED
                        Transiciones no válidas:
                        - SHIPPED o CANCELLED → cualquier otro estado
                        """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Nuevo estado del pedido a aplicar", content = @Content(schema = @Schema(implementation = com.example.delogica.dtos.input.OrderStatusInputDTO.class), examples = {
                        @ExampleObject(name = "Marcar como pagado", value = "{ \"status\": \"PAID\" }"),
                        @ExampleObject(name = "Marcar como enviado", value = "{ \"status\": \"SHIPPED\" }"),
                        @ExampleObject(name = "Cancelar pedido", value = "{ \"status\": \"CANCELLED\" }")
        })))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente", content = @Content(schema = @Schema(implementation = com.example.delogica.dtos.output.OrderOutputDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Entrada inválida o estado no reconocido", content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Transición de estado inválida", content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
        })
        @PutMapping("/{id}/status")
        public ResponseEntity<OrderOutputDTO> changeOrderStatus(
                        @Parameter(in = ParameterIn.PATH, description = "Identificador único del pedido", example = "500") @PathVariable Long id,

                        @Valid @RequestBody @Parameter(description = "Nuevo estado del pedido en formato JSON", required = true) OrderStatusInputDTO input) {
                logger.info("Recibida petición PUT /api/orders/{}/status para cambiar estado a {}", id,
                                input.getStatus());
                OrderOutputDTO updatedOrder = orderService.changeStatus(id, input);
                logger.info("Estado del pedido ID {} cambiado a {}", id, input.getStatus());
                return ResponseEntity.ok(updatedOrder);
        }

}
