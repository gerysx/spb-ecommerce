package com.example.delogica.config.specifications;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.example.delogica.models.Order;
import com.example.delogica.models.OrderStatus;

/**
 * Clase que agrupa las especificaciones JPA para filtrar registros
 * de la entidad {@link Order}.
 */
public final class OrderSpecifications {

    /**
     * Constructor privado para evitar instanciación.
     */
    private OrderSpecifications() {}

    /**
     * Filtra los pedidos pertenecientes a un cliente específico.
     *
     * @param customerId identificador del cliente
     * @return especificación para filtrar por id de cliente o null si no aplica
     */
    public static Specification<Order> hasCustomerId(Long customerId) {
        return (root, cq, cb) -> {
            if (customerId == null) return null;
            return cb.equal(root.get("customer").get("id"), customerId);
        };
    }

    /**
     * Filtra los pedidos con fecha igual o posterior a la indicada.
     *
     * @param from fecha mínima del pedido
     * @return especificación para filtrar desde una fecha o null si no aplica
     */
    public static Specification<Order> fromDate(LocalDateTime from) {
        return (root, cq, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("orderDate"), from);
        };
    }

    /**
     * Filtra los pedidos con fecha igual o anterior a la indicada.
     *
     * @param to fecha máxima del pedido
     * @return especificación para filtrar hasta una fecha o null si no aplica
     */
    public static Specification<Order> toDate(LocalDateTime to) {
        return (root, cq, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("orderDate"), to);
        };
    }

    /**
     * Filtra los pedidos según su estado.
     *
     * @param status estado del pedido (created, paid, shipped, cancelled)
     * @return especificación para filtrar por estado o null si no aplica
     */
    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, cq, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }
}
