package com.example.delogica.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.example.delogica.models.Product;

import jakarta.persistence.LockModeType;

/**
 * Repositorio JPA para la entidad {@link Product}.
 * Proporciona operaciones CRUD, soporte para Specifications
 * y consultas personalizadas para gestión de productos.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Busca un producto por su código SKU.
     *
     * @param sku  Código SKU del producto.
     * @return Un {@link Optional} con el producto si existe.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Verifica si existe un producto con el código SKU especificado.
     *
     * @param sku  Código SKU a verificar.
     * @return {@code true} si existe un producto con ese SKU, de lo contrario {@code false}.
     */
    boolean existsBySku(String sku);

    /**
     * Verifica si existe un producto con el mismo SKU pero distinto ID.
     * <p>Útil para validar unicidad del SKU durante una actualización.</p>
     *
     * @param sku  Código SKU a verificar.
     * @param id   ID del producto que se está actualizando.
     * @return {@code true} si existe otro producto con el mismo SKU, {@code false} en caso contrario.
     */
    boolean existsBySkuAndIdNot(String sku, Long id);

    /**
     * Busca un producto aplicando un bloqueo pesimista de escritura.
     * <p>Evita conflictos durante actualizaciones concurrentes.</p>
     *
     * @param id  ID del producto a bloquear.
     * @return Un {@link Optional} con el producto bloqueado para actualización.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithLock(Long id);
}
