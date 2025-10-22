package com.example.delogica.inputs;



import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.input.OrderItemInputDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCreateInputDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        OrderCreateInputDTO dto = new OrderCreateInputDTO();
        dto.setCustomerId(1L);
        dto.setShippingAddressId(2L);

        List<OrderItemInputDTO> items = new ArrayList<>();
        OrderItemInputDTO item = new OrderItemInputDTO();
        item.setProductId(10L);
        item.setQuantity(3);
        items.add(item);

        dto.setItems(items);

        Set<ConstraintViolation<OrderCreateInputDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenCustomerIdNull_thenViolation() {
        OrderCreateInputDTO dto = new OrderCreateInputDTO();
        dto.setShippingAddressId(1L);
        dto.setItems(new ArrayList<>());

        Set<ConstraintViolation<OrderCreateInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerId")));
    }

    @Test
    void whenItemsEmpty_thenViolation() {
        OrderCreateInputDTO dto = new OrderCreateInputDTO();
        dto.setCustomerId(1L);
        dto.setShippingAddressId(1L);
        dto.setItems(new ArrayList<>());

        Set<ConstraintViolation<OrderCreateInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("items")));
    }

    @Test
    void whenShippingAddressIdNull_thenViolation() {
        OrderCreateInputDTO dto = new OrderCreateInputDTO();
        dto.setCustomerId(1L);
        dto.setItems(new ArrayList<>());

        Set<ConstraintViolation<OrderCreateInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("shippingAddressId")));
    }
}
