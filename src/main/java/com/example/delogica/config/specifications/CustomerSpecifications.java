package com.example.delogica.config.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.delogica.models.Customer;

public final class CustomerSpecifications {
    private CustomerSpecifications() {}

    // filtro por email (contains, case-insensitive)
    public static Specification<Customer> emailContains(String email) {
        return (root, cq, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }
}
