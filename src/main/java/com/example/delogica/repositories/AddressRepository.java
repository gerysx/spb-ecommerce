package com.example.delogica.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.delogica.models.Address;

/**
 * Repositorio JPA para la entidad {@link Address}.
 * Proporciona métodos de consulta personalizados relacionados con direcciones de clientes.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Busca una dirección por su ID y el ID del cliente propietario.
     *
     * @param id          ID de la dirección.
     * @param customerId  ID del cliente propietario de la dirección.
     * @return Un {@link Optional} que contiene la dirección si existe.
     */
    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);

    /**
     * Obtiene todas las direcciones asociadas a un cliente.
     *
     * @param customerId  ID del cliente.
     * @return Lista de direcciones pertenecientes al cliente.
     */
    List<Address> findByCustomerId(Long customerId);

    /**
     * Desmarca todas las direcciones predeterminadas de un cliente, 
     * excepto la dirección cuyo ID se desea mantener como predeterminada.
     * <p>
     * Se usa cuando el cliente cambia su dirección predeterminada.
     * </p>
     *
     * @param customerId  ID del cliente cuyas direcciones se actualizarán.
     * @param keepId      ID de la dirección que debe permanecer como predeterminada.
     * @return Número de registros actualizados.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Address a set a.defaultAddress = false where a.customer.id = :customerId and a.id <> :keepId")
    int clearDefaultForCustomerExcept(@Param("customerId") Long customerId, @Param("keepId") Long keepId);

    /**
     * Busca la dirección actual marcada como predeterminada para un cliente.
     *
     * @param customerId  ID del cliente.
     * @return Un {@link Optional} que contiene la dirección predeterminada si existe.
     */
    Optional<Address> findByCustomerIdAndDefaultAddressTrue(Long customerId);
}
