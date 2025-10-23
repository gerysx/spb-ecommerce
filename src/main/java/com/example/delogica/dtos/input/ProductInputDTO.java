package com.example.delogica.dtos.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Datos de entrada para crear/actualizar productos
 * Reglas: precio > 0, stock >= 0
 */
@Data
@Schema(name = "ProductInput", description = "Datos de entrada para el catálogo de productos")
public class ProductInputDTO {

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 40, message = "El SKU no puede superar los 40 caracteres")
    @Schema(description = "SKU del producto", example = "SKU-ABC-001", maxLength = 40, requiredMode = Schema.RequiredMode.REQUIRED)
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 160, message = "El nombre no puede superar los 160 caracteres")
    @Schema(description = "Nombre del producto", example = "Café Molido 500g", maxLength = 160, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    @Schema(description = "Descripción del producto", example = "Café arábica 100% de tueste natural", maxLength = 2000)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "El precio debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Precio unitario", example = "8.95")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    @Schema(description = "Unidades disponibles en inventario", example = "150")
    private Integer stock;

    @Schema(description = "Indica si el producto está activo para la venta", example = "true", nullable = true)
    private Boolean active;
}
