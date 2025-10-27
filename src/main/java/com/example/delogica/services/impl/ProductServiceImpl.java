package com.example.delogica.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.config.exceptions.SkuAlreadyInUseException;
import com.example.delogica.config.specifications.ProductSpecifications;
import com.example.delogica.dtos.input.ProductInputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;
import com.example.delogica.mappers.ProductMapper;
import com.example.delogica.models.Product;
import com.example.delogica.repositories.ProductRepository;
import com.example.delogica.services.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductOutputDTO create(ProductInputDTO input) {
        logger.info("Creando producto con SKU: {}", input.getSku());

        if (productRepository.existsBySku(input.getSku())) {
            logger.warn("Intento de crear producto con SKU ya usado: {}", input.getSku());
            throw new SkuAlreadyInUseException(input.getSku());
        }

        Product saved = productRepository.save(productMapper.toEntity(input));

        logger.info("Producto creado correctamente con SKU: {}", input.getSku());
        return productMapper.toOutput(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductOutputDTO findById(Long productId) {
        logger.info("Buscando producto con ID: {}", productId);

        Product found = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn("Producto no encontrado con ID: {}", productId);
                    return ResourceNotFoundException.forId(Product.class, productId);
                });

        logger.info("Producto encontrado con ID: {}", productId);
        return productMapper.toOutput(found);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductOutputDTO> search(Pageable pageable, String name, Boolean active) {
        logger.info("Buscando productos con filtros - nombre: {}, activo: {}", name, active);

        Specification<Product> spec = null;

        if (name != null && !name.isBlank()) {
            spec = (spec == null)
                    ? ProductSpecifications.nameContains(name)
                    : spec.and(ProductSpecifications.nameContains(name));
        }

        if (active != null) {
            spec = (spec == null)
                    ? ProductSpecifications.hasActive(active)
                    : spec.and(ProductSpecifications.hasActive(active));
        }

        Page<ProductOutputDTO> result = productRepository.findAll(spec, pageable).map(productMapper::toOutput);

        logger.info("Búsqueda completada con {} resultados", result.getTotalElements());
        return result;
    }

    @Override
    @Transactional
    public ProductOutputDTO update(Long productId, ProductInputDTO input) {
        logger.info("Actualizando producto con ID: {}", productId);

        Product db = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> {
                    logger.warn("Producto no encontrado para actualizar con ID: {}", productId);
                    return ResourceNotFoundException.forId(Product.class, productId);
                });

        if (input.getSku() != null) {
            // solo validar exclusión si cambia realmente
            if (!input.getSku().equals(db.getSku())
                    && productRepository.existsBySkuAndIdNot(input.getSku(), productId)) {
                logger.warn("Intento de actualizar producto con SKU ya usado: {}", input.getSku());
                throw new SkuAlreadyInUseException(input.getSku());
            }
            // actualizar SKU aunque sea el mismo (no rompe nada)
            db.setSku(input.getSku());
        }

        productMapper.updateEntityFromDto(input, db);

        Product saved = productRepository.save(db);

        logger.info("Producto actualizado correctamente con ID: {}", productId);
        return productMapper.toOutput(saved);
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        logger.info("Desactivando producto con ID: {}", productId);

        Product db = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> {
                    logger.warn("Producto no encontrado para eliminar con ID: {}", productId);
                    return ResourceNotFoundException.forId(Product.class, productId);
                });

        db.setActive(false);
        productRepository.save(db);

        logger.info("Producto desactivado correctamente con ID: {}", productId);
    }
}
