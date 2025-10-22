package com.example.delogica.inputs;


import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.delogica.dtos.input.OrderItemInputDTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OrderItemInputDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        OrderItemInputDTO dto = new OrderItemInputDTO();
        dto.setProductId(1L);
        dto.setQuantity(5);

        Set<ConstraintViolation<OrderItemInputDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenProductIdNull_thenViolation() {
        OrderItemInputDTO dto = new OrderItemInputDTO();
        dto.setQuantity(1);

        Set<ConstraintViolation<OrderItemInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productId")));
    }

    @Test
    void whenQuantityNull_thenViolation() {
        OrderItemInputDTO dto = new OrderItemInputDTO();
        dto.setProductId(1L);

        Set<ConstraintViolation<OrderItemInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
    }

    @Test
    void whenQuantityNotPositive_thenViolation() {
        OrderItemInputDTO dto = new OrderItemInputDTO();
        dto.setProductId(1L);
        dto.setQuantity(0);

        Set<ConstraintViolation<OrderItemInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenProductIdNotPositive_thenViolation() {
        OrderItemInputDTO dto = new OrderItemInputDTO();
        dto.setProductId(-1L);
        dto.setQuantity(1);

        Set<ConstraintViolation<OrderItemInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
