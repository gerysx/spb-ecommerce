package com.example.delogica.dtos.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.delogica.models.OrderStatus;
import lombok.Data;

@Data
public class OrderOutputDTO {

    private Long id;
    private CustomerOutputDTO customer;
    private AddressOutputDTO shippingAddress;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal total;
    private List<OrderItemOutputDTO> items = new ArrayList<>();
}
