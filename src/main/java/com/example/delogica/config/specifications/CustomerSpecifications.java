package com.example.delogica.config.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.delogica.models.Customer;

/**
 * Clase que contiene especificaciones JPA para aplicar filtros
 * sobre la entidad {@link Customer}.
 */
public final class CustomerSpecifications {

    /**
     * Constructor privado para evitar la creación de instancias.
     */
    private CustomerSpecifications() {}

    /**
     * Devuelve una especificación que filtra los clientes cuyo email
     * contiene el texto indicado, sin distinguir mayúsculas ni minúsculas.
     * Si el valor es nulo o vacío, no aplica ningún filtro.
     *
     * @param email texto a buscar dentro del email del cliente
     * @return especificación JPA para filtrar por email o null si no aplica
     */
    public static Specification<Customer> emailContains(String email) {
        return (root, cq, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }
}
