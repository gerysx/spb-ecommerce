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
     * Recupera una orden por su ID, cargando de forma anticipada
     * sus relaciones clave mediante un {@link EntityGraph}.
     * 
     * <p>
     * Este método está optimizado para casos donde se necesita
     * mostrar el detalle completo de una orden, incluyendo:
     * </p>
     * <ul>
     * <li>Los ítems de la orden</li>
     * <li>El producto de cada ítem</li>
     * <li>El cliente asociado</li>
     * <li>La dirección de envío</li>
     * </ul>
     * 
     * <p>
     * Útil para endpoints tipo <code>GET /api/orders/{id}</code>.
     * </p>
     *
     * @param id el ID de la orden
     * @return una {@link Optional} con la orden y sus relaciones si existe
     */
    @EntityGraph(type = EntityGraphType.LOAD, attributePaths = { "items", "items.product", "customer", "shippingAddress" })
    Optional<Order> findWithDetailsById(Long id);

    /**
     * Recupera una orden por su ID aplicando un bloqueo pesimista de escritura.
     * 
     * <p>Este método garantiza que, mientras se procesa la orden, 
     * otros procesos no puedan leer ni escribir sobre la misma fila en la base de datos.</p>
     * 
     * <p>Es especialmente útil en operaciones que cambian el estado de la orden,
     * como en <code>PUT /api/orders/{id}/status</code>, donde se requiere garantizar
     * la consistencia en entornos concurrentes.</p>
     *
     * @param id el ID de la orden
     * @return una {@link Optional} con la orden si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(Long id);
}
