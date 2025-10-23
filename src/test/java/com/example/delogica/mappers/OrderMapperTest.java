package com.example.delogica.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.example.delogica.dtos.input.OrderCreateInputDTO;

import com.example.delogica.models.Order;

/**
 * Valida el mapeo DTO->Entidad para las referencias por id.
 * No llama a helpers internos; prueba el comportamiento observable.
 */
class OrderMapperTest {

    // Usa la implementación generada por MapStruct sin Spring/Mockito
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void toEntity_mapsCustomerAndAddressIds_intoReferences() {
        // Arrange
        OrderCreateInputDTO dto = new OrderCreateInputDTO();
        dto.setCustomerId(1L);
        dto.setShippingAddressId(5L);
        // Evitamos preparar mapeos de items en este test
        dto.setItems(List.of()); // cumple con la propiedad (no es null)

        // Act
        Order order = orderMapper.toEntity(dto);

        // Assert
        assertNotNull(order, "Debe crear la entidad Order");
        assertNotNull(order.getCustomer(), "Debe mapear customerId a Customer");
        assertEquals(1L, order.getCustomer().getId());

        assertNotNull(order.getShippingAddress(), "Debe mapear shippingAddressId a Address");
        assertEquals(5L, order.getShippingAddress().getId());

        // Items vacíos tal cual hemos pasado (no validamos item mapping aquí)
        assertNotNull(order.getItems(), "La lista items no debe ser null");
        assertTrue(order.getItems().isEmpty(), "No esperamos items en este test");
    }
}
