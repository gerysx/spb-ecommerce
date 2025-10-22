package com.example.delogica.dtos.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.delogica.models.OrderStatus;

import lombok.Data;

@Data
public class OrderSimpleOutputDTO {
    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private Long customerId;
    private Long shippingAddressId;
    private List<OrderItemSimpleOutputDTO> items;
    private BigDecimal total;
}

