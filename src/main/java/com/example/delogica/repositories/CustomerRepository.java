package com.example.delogica.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import jakarta.persistence.LockModeType;

import com.example.delogica.models.Customer;

/**
 * Repositorio JPA para la entidad {@link Customer}.
 * Proporciona operaciones de búsqueda y bloqueo relacionadas con clientes.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * Busca un cliente por su dirección de correo electrónico.
     *
     * @param email  Email del cliente.
     * @return Un {@link Optional} con el cliente si existe.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Verifica si existe un cliente con el correo electrónico especificado.
     *
     * @param email  Email a verificar.
     * @return {@code true} si existe un cliente con ese correo, de lo contrario {@code false}.
     */
    boolean existsByEmail(String email);

    /**
     * Obtiene un cliente junto con sus direcciones asociadas.
     * <p>Usado normalmente para solicitudes GET a <code>/api/customers/{id}</code>.</p>
     *
     * @param id  ID del cliente.
     * @return Un {@link Optional} con el cliente y sus direcciones cargadas.
     */
    @EntityGraph(type = EntityGraphType.LOAD, attributePaths = { "addresses" })
    Optional<Customer> findWithAddressesById(Long id);

    /**
     * Busca un cliente aplicando un bloqueo pesimista de escritura.
     * <p>Usado para evitar conflictos durante actualizaciones concurrentes.</p>
     *
     * @param id  ID del cliente a bloquear.
     * @return Un {@link Optional} con el cliente bloqueado para actualización.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findByIdWithLock(Long id);
}
