package com.example.delogica.dtos.output;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductOutputDTO {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private boolean active;
}
