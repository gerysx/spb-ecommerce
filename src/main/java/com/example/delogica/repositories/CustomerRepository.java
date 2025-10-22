// src/main/java/com/example/delogica/repositories/CustomerRepository.java
package com.example.delogica.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import jakarta.persistence.LockModeType;

import com.example.delogica.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);

    // Detalle con direcciones para GET /api/customers/{id}
    @EntityGraph(type = EntityGraphType.LOAD, attributePaths = { "addresses" })
    Optional<Customer> findWithAddressesById(Long id);

    // Para actualizaciones seguras de cliente
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findByIdWithLock(Long id);

    
}
