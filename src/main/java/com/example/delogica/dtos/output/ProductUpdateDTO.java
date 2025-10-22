package com.example.delogica.dtos.output;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductUpdateDTO {
    @Size(max = 160) @NotBlank private String name;
    @Size(max = 2000) private String description;
    @NotNull @DecimalMin(value = "0.00", inclusive = false) private BigDecimal price;
    @NotNull @PositiveOrZero private Integer stock;
    @NotNull private Boolean active; 
}
