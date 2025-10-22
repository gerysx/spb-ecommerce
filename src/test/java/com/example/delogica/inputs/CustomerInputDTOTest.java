package com.example.delogica.inputs;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.delogica.dtos.input.CustomerInputDTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerInputDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        CustomerInputDTO dto = new CustomerInputDTO();
        dto.setFullName("Juan Pérez");
        dto.setEmail("juan.perez@example.com");
        dto.setPhone("123456789");
        // No agregamos direcciones, pero la lista está inicializada

        Set<ConstraintViolation<CustomerInputDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenFullNameBlank_thenViolation() {
        CustomerInputDTO dto = new CustomerInputDTO();
        dto.setFullName("");
        dto.setEmail("email@example.com");
        dto.setPhone("123456789");

        Set<ConstraintViolation<CustomerInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
    }

    @Test
    void whenEmailInvalid_thenViolation() {
        CustomerInputDTO dto = new CustomerInputDTO();
        dto.setFullName("Nombre");
        dto.setEmail("no-es-email");
        dto.setPhone("123456789");

        Set<ConstraintViolation<CustomerInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void whenPhoneWrongFormat_thenViolation() {
        CustomerInputDTO dto = new CustomerInputDTO();
        dto.setFullName("Nombre");
        dto.setEmail("email@example.com");
        dto.setPhone("1234"); // inválido

        Set<ConstraintViolation<CustomerInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void whenAddressesNull_thenViolation() {
        CustomerInputDTO dto = new CustomerInputDTO();
        dto.setFullName("Nombre");
        dto.setEmail("email@example.com");
        dto.setPhone("123456789");
        dto.setAddresses(null);

        Set<ConstraintViolation<CustomerInputDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("addresses")));
    }
}
