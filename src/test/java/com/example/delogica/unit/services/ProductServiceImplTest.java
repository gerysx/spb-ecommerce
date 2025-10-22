package com.example.delogica.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.config.exceptions.SkuAlreadyInUseException;
import com.example.delogica.dtos.input.ProductInputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;
import com.example.delogica.mappers.ProductMapper;
import com.example.delogica.models.Product;
import com.example.delogica.repositories.ProductRepository;
import com.example.delogica.services.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    // ------------- TEST CREATE() ---------------
    @Test
    void create_withUniqueSku_savesProduct() {
        ProductInputDTO input = new ProductInputDTO();
        input.setSku("SKU123");

        Product entity = new Product();
        Product savedEntity = new Product();
        ProductOutputDTO outputDTO = new ProductOutputDTO();

        when(productRepository.existsBySku("SKU123")).thenReturn(false);
        when(productMapper.toEntity(input)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(savedEntity);
        when(productMapper.toOutput(savedEntity)).thenReturn(outputDTO);

        ProductOutputDTO result = productService.create(input);

        assertNotNull(result);
        verify(productRepository).existsBySku("SKU123");
        verify(productMapper).toEntity(input);
        verify(productRepository).save(entity);
        verify(productMapper).toOutput(savedEntity);
    }

    @Test
    void create_withDuplicateSku_throwsException() {
        ProductInputDTO input = new ProductInputDTO();
        input.setSku("SKU123");

        when(productRepository.existsBySku("SKU123")).thenReturn(true);

        assertThrows(SkuAlreadyInUseException.class, () -> productService.create(input));

        verify(productRepository).existsBySku("SKU123");
        verify(productRepository, never()).save(any());
    }

    // ------------- TEST FINDBYID() ---------------
    @Test
    void findById_whenProductExists_returnsProductOutputDTO() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toOutput(product)).thenReturn(new ProductOutputDTO());

        ProductOutputDTO result = productService.findById(productId);

        assertNotNull(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void findById_whenProductNotFound_throwsResourceNotFoundException() {
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(productId));
    }

    // ------------- TEST UPDATE() ---------------
    @Test
    void update_whenProductExistsAndSkuUnique_updatesProduct() {
        Long productId = 1L;
        ProductInputDTO input = new ProductInputDTO();
        input.setSku("new-sku");

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setSku("old-sku");

        when(productRepository.findByIdWithLock(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndIdNot("new-sku", productId)).thenReturn(false);
        doAnswer(invocation -> {
            ProductInputDTO dto = invocation.getArgument(0);
            Product entity = invocation.getArgument(1);
            entity.setSku(dto.getSku()); // simular updateEntityFromDto
            return null;
        }).when(productMapper).updateEntityFromDto(any(), any());
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
        when(productMapper.toOutput(existingProduct)).thenReturn(new ProductOutputDTO());

        ProductOutputDTO result = productService.update(productId, input);

        assertNotNull(result);
        verify(productRepository).save(existingProduct);
        assertEquals("new-sku", existingProduct.getSku());
    }

    @Test
    void update_whenSkuAlreadyUsed_throwsSkuAlreadyInUseException() {
        Long productId = 1L;
        ProductInputDTO input = new ProductInputDTO();
        input.setSku("duplicate-sku");

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setSku("old-sku");

        when(productRepository.findByIdWithLock(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndIdNot("duplicate-sku", productId)).thenReturn(true);

        assertThrows(SkuAlreadyInUseException.class, () -> productService.update(productId, input));
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_whenProductNotFound_throwsResourceNotFoundException() {
        Long productId = 1L;
        ProductInputDTO input = new ProductInputDTO();

        when(productRepository.findByIdWithLock(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.update(productId, input));
    }

    // ------------- TEST DELETE() ---------------
    @Test
    void delete_whenProductExists_deactivatesProduct() {
        Long productId = 1L;
        Product product = new Product();
        product.setActive(true);

        when(productRepository.findByIdWithLock(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.delete(productId);

        assertFalse(product.isActive());
        verify(productRepository).save(product);
    }

    @Test
    void delete_whenProductNotFound_throwsResourceNotFoundException() {
        Long productId = 1L;

        when(productRepository.findByIdWithLock(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.delete(productId));
        verify(productRepository, never()).save(any());
    }

    // ------------- TEST SEARCH() ---------------
    @SuppressWarnings("unchecked")
    @Test
    void search_withFilters_returnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String name = "producto";
        Boolean active = true;

        Product product = new Product();
        product.setId(1L);
        product.setName("producto 1");

        ProductOutputDTO outputDTO = new ProductOutputDTO();
        outputDTO.setId(1L);
        outputDTO.setName("producto 1");

        Page<Product> productsPage = new PageImpl<>(List.of(product));

        // Importante: especificar el tipo del any() para evitar ambigüedad
        when(productRepository.findAll((Specification<Product>) any(), eq(pageable))).thenReturn(productsPage);
        when(productMapper.toOutput(product)).thenReturn(outputDTO);

        // Act
        Page<ProductOutputDTO> result = productService.search(pageable, name, active);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll((Specification<Product>) any(), eq(pageable));
        verify(productMapper).toOutput(product);
    }

}
