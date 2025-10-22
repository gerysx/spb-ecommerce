package com.example.delogica.controllers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.output.OrderOutputDTO;
import com.example.delogica.dtos.output.OrderSimpleOutputDTO;
import com.example.delogica.models.OrderStatus;
import com.example.delogica.services.OrderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders crea pedido con líneas
    @PostMapping
    public ResponseEntity<OrderSimpleOutputDTO> createOrder(@Valid @RequestBody OrderCreateInputDTO orderInputDTO) {
        logger.info("Recibida petición POST /api/orders para crear pedido clienteId={}", orderInputDTO.getCustomerId());
        OrderSimpleOutputDTO createdOrder = orderService.create(orderInputDTO);
        logger.info("Pedido creado con ID {}", createdOrder.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    // GET /api/orders lista pedidos con filtros customerId, fromDate, toDate, status
    @GetMapping
    public ResponseEntity<Page<OrderOutputDTO>> searchOrders(
            Pageable pageable,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) OrderStatus status
    ) {
        logger.info("Recibida petición GET /api/orders con filtros: customerId={}, fromDate={}, toDate={}, status={}",
                customerId, fromDate, toDate, status);
        Page<OrderOutputDTO> ordersPage = orderService.search(pageable, customerId, fromDate, toDate, status);
        logger.info("Resultado: {} pedidos encontrados", ordersPage.getTotalElements());
        return ResponseEntity.ok(ordersPage);
    }

    // GET /api/orders/{id} detalle con líneas
    @GetMapping("/{id}")
    public ResponseEntity<OrderOutputDTO> getOrderById(@PathVariable Long id) {
        logger.info("Recibida petición GET /api/orders/{} para obtener detalles del pedido", id);
        OrderOutputDTO order = orderService.getById(id);
        logger.info("Detalle pedido obtenido para ID {}", id);
        return ResponseEntity.ok(order);
    }

    // PUT /api/orders/{id}/status cambio de estado siguiendo las transiciones válidas
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderOutputDTO> changeOrderStatus(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "El nuevo estado es obligatorio") OrderStatus newStatus) {
        logger.info("Recibida petición PUT /api/orders/{}/status para cambiar estado a {}", id, newStatus);
        OrderOutputDTO updatedOrder = orderService.changeStatus(id, newStatus);
        logger.info("Estado del pedido ID {} cambiado a {}", id, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}
