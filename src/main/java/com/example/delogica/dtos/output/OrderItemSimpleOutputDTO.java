package com.example.delogica.dtos.output;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemSimpleOutputDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public BigDecimal getLineTotal() {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
