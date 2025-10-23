package com.example.delogica.dtos.output;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "Product", description = "Representación pública de un producto del catálogo")
public class ProductOutputDTO {

    @Schema(description = "Identificador del producto", example = "1000")
    private Long id;

    @Schema(description = "SKU único del producto", example = "SKU-ABC-001", maxLength = 40)
    private String sku;

    @Schema(description = "Nombre del producto", example = "Café Molido 500g", maxLength = 160)
    private String name;

    @Schema(description = "Descripción del producto", example = "Café arábica 100% de tueste natural", maxLength = 2000)
    private String description;

    @Schema(description = "Precio unitario", example = "8.95")
    private BigDecimal price;

    @Schema(description = "Unidades disponibles", example = "150")
    private Integer stock;

    @Schema(description = "Indicador de disponibilidad para la venta", example = "true")
    private boolean active;
}
