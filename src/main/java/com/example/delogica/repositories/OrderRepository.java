package com.example.delogica.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.example.delogica.models.Order;

import jakarta.persistence.LockModeType;

/**
 * Repositorio JPA para la entidad {@link Order}.
 * Proporciona operaciones CRUD, soporte para Specifications
 * y consultas personalizadas con gestión de relaciones y bloqueo.
 */
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * Obtiene una orden junto con sus relaciones asociadas,
     * como ítems, productos, cliente y dirección de envío.
     * <p>Usado normalmente para solicitudes GET a <code>/api/orders/{id}</code>.</p>
     *
     * @param id  ID de la orden.
     * @return Un {@link Optional} con la orden y sus relaciones cargadas.
     */
    @EntityGraph(type = EntityGraphType.LOAD, attributePaths = { "items", "items.product", "customer", "shippingAddress" })
    Optional<Order> findWithDetailsById(Long id);

    /**
     * Busca una orden aplicando un bloqueo pesimista de escritura.
     * <p>Evita conflictos en actualizaciones concurrentes al garantizar
     * acceso exclusivo durante modificaciones.</p>
     *
     * @param id  ID de la orden a bloquear.
     * @return Un {@link Optional} con la orden bloqueada para actualización.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(Long id);
}
