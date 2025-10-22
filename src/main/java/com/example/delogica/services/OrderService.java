package com.example.delogica.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.output.OrderOutputDTO;
import com.example.delogica.dtos.output.OrderSimpleOutputDTO;
import com.example.delogica.models.OrderStatus;

public interface OrderService {

    OrderSimpleOutputDTO  create(OrderCreateInputDTO input);

    Page<OrderOutputDTO> search(Pageable pageable, Long customerId, LocalDateTime fromDate, LocalDateTime toDate, OrderStatus status);

    OrderOutputDTO getById(Long id);

    OrderOutputDTO changeStatus(Long id, OrderStatus newStatus);
}
