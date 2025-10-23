package com.example.delogica.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.delogica.dtos.output.OrderItemOutputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;
import com.example.delogica.models.OrderItem;
import com.example.delogica.models.Product;

@ExtendWith(MockitoExtension.class)
class OrderItemMapperTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private OrderItemMapperImpl orderItemMapper;

    @Test
    void testToOutput() {
        Product product = new Product();
        product.setId(1L);

        OrderItem entity = new OrderItem();
        entity.setId(10L);
        entity.setProduct(product);
        entity.setQuantity(2);
        entity.setUnitPrice(new BigDecimal("19.99"));

        ProductOutputDTO productOutputDTO = new ProductOutputDTO();
        productOutputDTO.setId(1L);

        Mockito.when(productMapper.toOutput(product)).thenReturn(productOutputDTO);

        OrderItemOutputDTO dto = orderItemMapper.toOutput(entity);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals(2, dto.getQuantity());
        assertTrue(new BigDecimal("19.99").compareTo(dto.getUnitPrice()) == 0);
        assertNotNull(dto.getProduct());
        assertEquals(1L, dto.getProduct().getId());
    }
}
