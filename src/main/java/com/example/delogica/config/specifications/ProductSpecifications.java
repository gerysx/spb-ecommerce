package com.example.delogica.config.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.delogica.models.Product;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> nameContains(String name) {
        return (root, cq, cb) -> {
            if (name == null || name.isBlank()) return null; // ignora filtro si no viene
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasActive(Boolean active) {
        return (root, cq, cb) -> {
            if (active == null) return null; // ignora filtro si es opcional
            return cb.equal(root.get("active"), active);
        };
    }
}
