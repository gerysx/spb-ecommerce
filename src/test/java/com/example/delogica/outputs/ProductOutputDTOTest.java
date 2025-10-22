package com.example.delogica.outputs;



import org.junit.jupiter.api.Test;

import com.example.delogica.dtos.output.ProductOutputDTO;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductOutputDTOTest {

    @Test
    void testGettersSetters() {
        ProductOutputDTO dto = new ProductOutputDTO();
        dto.setId(1L);
        dto.setSku("SKU001");
        dto.setName("Nombre");
        dto.setDescription("Descripción");
        dto.setPrice(new BigDecimal("20.00"));
        dto.setStock(10);
        dto.setActive(true);

        assertEquals(1L, dto.getId());
        assertEquals("SKU001", dto.getSku());
        assertEquals("Nombre", dto.getName());
        assertEquals("Descripción", dto.getDescription());
        assertEquals(new BigDecimal("20.00"), dto.getPrice());
        assertEquals(10, dto.getStock());
        assertTrue(dto.isActive());
    }
}
