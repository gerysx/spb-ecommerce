package com.example.delogica.dtos.input;

import java.util.List;

import jakarta.validation.Valid;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateInputDTO {

    @NotNull
    private Long customerId;

    @NotNull
    private Long shippingAddressId;

    @NotEmpty
    @Valid
    private List<OrderItemInputDTO> items;
}
