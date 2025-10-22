package com.example.delogica.outputs;


import org.junit.jupiter.api.Test;

import com.example.delogica.dtos.output.OrderItemOutputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class OrderItemOutputDTOTest {

    @Test
    void testGettersAndSetters() {
        OrderItemOutputDTO dto = new OrderItemOutputDTO();

        dto.setId(1L);
        ProductOutputDTO product = new ProductOutputDTO();
        product.setId(2L);
        dto.setProduct(product);
        dto.setQuantity(3);
        dto.setUnitPrice(new BigDecimal("10.50"));

        assertEquals(1L, dto.getId());
        assertEquals(product, dto.getProduct());
        assertEquals(3, dto.getQuantity());
        assertEquals(new BigDecimal("10.50"), dto.getUnitPrice());
    }
}
