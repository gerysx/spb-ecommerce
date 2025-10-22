package com.example.delogica.dtos.output;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemOutputDTO {

    private Long id;
    private ProductOutputDTO product;
    private Integer quantity;
    private BigDecimal unitPrice;
}
