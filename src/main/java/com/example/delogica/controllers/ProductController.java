// src/main/java/com/example/delogica/controllers/ProductController.java
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    // POST /api/products → 201 + Location + body
    @PostMapping
    public ResponseEntity<ProductOutputDTO> create(@Valid @RequestBody ProductInputDTO input,
                                                   UriComponentsBuilder ucb) {
        logger.info("Creando producto con SKU: {}", input.getSku());
        ProductOutputDTO out = productService.create(input);
        var location = ucb.path("/api/products/{id}").buildAndExpand(out.getId()).toUri();
        logger.info("Producto creado con ID: {}", out.getId());
        return ResponseEntity.created(location).body(out);
    }

    // GET /api/products (paginado + filtros)
    @GetMapping
    public Page<ProductOutputDTO> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        logger.info("Listando productos name={}, active={}", name, active);
        return productService.search(pageable, name, active);
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ProductOutputDTO detail(@PathVariable Long id) {
        logger.info("Detalle producto id={}", id);
        return productService.findById(id);
    }

    // PUT /api/products/{id} → 200 + body
    @PutMapping("/{id}")
    public ProductOutputDTO update(@PathVariable Long id, @Valid @RequestBody ProductInputDTO input) {
        logger.info("Actualizando producto id={}", id);
        return productService.update(id, input);
    }

    // DELETE /api/products/{id} → 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Eliminando (lógico) producto id={}", id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
