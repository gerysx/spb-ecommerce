package com.example.delogica.config.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.delogica.models.Product;

/**
 * Clase que contiene especificaciones JPA para aplicar filtros
 * sobre la entidad {@link Product}.
 */
public final class ProductSpecifications {

    /**
     * Constructor privado para evitar instanciación.
     */
    private ProductSpecifications() {}

    /**
     * Filtra los productos cuyo nombre contiene el texto indicado,
     * sin distinguir entre mayúsculas y minúsculas.
     * Si el parámetro es nulo o vacío, no se aplica ningún filtro.
     *
     * @param name texto a buscar en el nombre del producto
     * @return especificación para filtrar por nombre o null si no aplica
     */
    public static Specification<Product> nameContains(String name) {
        return (root, cq, cb) -> {
            if (name == null || name.isBlank()) return null; // ignora filtro si no viene
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Filtra los productos según su estado activo o inactivo.
     * Si el valor es nulo, no se aplica ningún filtro.
     *
     * @param active indica si el producto está activo o no
     * @return especificación para filtrar por estado activo o null si no aplica
     */
    public static Specification<Product> hasActive(Boolean active) {
        return (root, cq, cb) -> {
            if (active == null) return null; // ignora filtro si es opcional
            return cb.equal(root.get("active"), active);
        };
    }
}
