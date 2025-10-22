package com.example.delogica.mappers;


import com.example.delogica.dtos.input.ProductInputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;
import com.example.delogica.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = Mappers.getMapper(ProductMapper.class);
    }

    @Test
    void testToEntity() {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setSku("ABC123");
        dto.setName("Producto Test");
        dto.setDescription("Descripción del producto");
        dto.setPrice(BigDecimal.valueOf(10.99));
        dto.setStock(50);
        dto.setActive(true); // Aunque se ignora

        Product entity = productMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("ABC123", entity.getSku());
        assertEquals("Producto Test", entity.getName());
        assertEquals("Descripción del producto", entity.getDescription());
        assertEquals(BigDecimal.valueOf(10.99), entity.getPrice());
        assertEquals(50, entity.getStock());
        assertNull(entity.getId()); // porque se ignora
        assertNull(entity.getCreatedAt()); // porque se ignora
        assertNull(entity.getUpdatedAt()); // porque se ignora
       assertFalse(entity.isActive());
    }

    @Test
    void testToOutput() {
        Product entity = new Product();
        entity.setId(1L);
        entity.setSku("XYZ789");
        entity.setName("Producto Output");
        entity.setDescription("Salida");
        entity.setPrice(BigDecimal.valueOf(25.50));
        entity.setStock(100);
        entity.setActive(true);

        ProductOutputDTO dto = productMapper.toOutput(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("XYZ789", dto.getSku());
        assertEquals("Producto Output", dto.getName());
        assertEquals("Salida", dto.getDescription());
        assertEquals(BigDecimal.valueOf(25.50), dto.getPrice());
        assertEquals(100, dto.getStock());
        assertTrue(dto.isActive());
    }

    @Test
    void testUpdateEntityFromDto() {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setSku("NEW123");
        dto.setName("Nuevo nombre");
        dto.setDescription("Actualización");
        dto.setPrice(BigDecimal.valueOf(99.99));
        dto.setStock(10);
        dto.setActive(false); // Aquí sí se mapea

        Product entity = new Product();
        entity.setId(10L); // debería conservarse porque lo ignoramos
        entity.setCreatedAt(null);
        entity.setUpdatedAt(null);

        productMapper.updateEntityFromDto(dto, entity);

        assertEquals("NEW123", entity.getSku());
        assertEquals("Nuevo nombre", entity.getName());
        assertEquals("Actualización", entity.getDescription());
        assertEquals(BigDecimal.valueOf(99.99), entity.getPrice());
        assertEquals(10, entity.getStock());
        assertFalse(entity.isActive());
        assertEquals(10L, entity.getId()); // no fue sobrescrito
    }
}
