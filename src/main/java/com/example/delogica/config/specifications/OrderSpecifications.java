package com.example.delogica.config.specifications;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.example.delogica.models.Order;
import com.example.delogica.models.OrderStatus;

public final class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<Order> hasCustomerId(Long customerId) {
        return (root, cq, cb) -> {
            if (customerId == null) return null;
            return cb.equal(root.get("customer").get("id"), customerId);
        };
    }

    public static Specification<Order> fromDate(LocalDateTime from) {
        return (root, cq, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("orderDate"), from);
        };
    }

    public static Specification<Order> toDate(LocalDateTime to) {
        return (root, cq, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("orderDate"), to);
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, cq, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }
}
