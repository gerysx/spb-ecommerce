package com.example.delogica.controllers;

import com.example.delogica.dtos.input.ProductInputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;
import com.example.delogica.services.ProductService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    // POST /api/products 
    // Crear producto
    @PostMapping
    public ProductOutputDTO create(@Valid @RequestBody ProductInputDTO input) {
        logger.info("Creando producto con SKU: {}", input.getSku());
        ProductOutputDTO response = productService.create(input);
        logger.info("Producto creado con ID: {}", response.getId());
        return response;
    }

    // GET /api/products 
    // Lista con paginación, filtro por nombre y estado activo
    @GetMapping
    public Page<ProductOutputDTO> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        logger.info("Listando productos con filtro - name: {}, active: {}", name, active);
        Page<ProductOutputDTO> result = productService.search(pageable, name, active);
        logger.info("Productos encontrados: {}", result.getTotalElements());
        return result;
    }

    // GET /api/products/{id} 
    // Detalle de producto
    @GetMapping("/{id}")
    public ProductOutputDTO detail(@PathVariable Long id) {
        logger.info("Buscando producto con ID: {}", id);
        ProductOutputDTO product = productService.findById(id);
        logger.info("Producto encontrado con ID: {}", id);
        return product;
    }

    // PUT /api/products/{id} 
    // Actualizar producto
    @PutMapping("/{id}")
    public ProductOutputDTO update(@PathVariable Long id, @Valid @RequestBody ProductInputDTO input) {
        logger.info("Actualizando producto con ID: {}", id);
        ProductOutputDTO response = productService.update(id, input);
        logger.info("Producto actualizado con ID: {}", id);
        return response;
    }

    // DELETE /api/products/{id} 
    // Baja lógica del producto
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        logger.info("Eliminando producto con ID: {}", id);
        productService.delete(id);
        logger.info("Producto eliminado con ID: {}", id);
    }
}
