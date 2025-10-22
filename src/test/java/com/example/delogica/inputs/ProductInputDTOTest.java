package com.example.delogica.inputs;


import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.delogica.dtos.input.ProductInputDTO;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ProductInputDTOTest {

    private static Validator validator;

    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setSku("ABC123");
        dto.setName("Producto Test");
        dto.setDescription("Descripción del producto");
        dto.setPrice(new BigDecimal("10.50"));
        dto.setStock(5);
        dto.setActive(true);

        Set<ConstraintViolation<ProductInputDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenSkuBlank_thenViolation() {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setSku("");
        dto.setName("Nombre");
        dto.setPrice(new BigDecimal("10"));
        dto.setStock(1);

        Set<ConstraintViolation<ProductInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("sku")));
    }

    @Test
    void whenPriceZeroOrNegative_thenViolation() {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setSku("SKU001");
        dto.setName("Nombre");
        dto.setPrice(BigDecimal.ZERO);
        dto.setStock(1);

        Set<ConstraintViolation<ProductInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));

        dto.setPrice(new BigDecimal("-5"));
        violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenStockNegative_thenViolation() {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setSku("SKU001");
        dto.setName("Nombre");
        dto.setPrice(new BigDecimal("10"));
        dto.setStock(-1);

        Set<ConstraintViolation<ProductInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("stock")));
    }
}
