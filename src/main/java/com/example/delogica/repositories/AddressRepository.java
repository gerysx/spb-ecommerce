// src/main/java/com/example/delogica/repositories/AddressRepository.java
package com.example.delogica.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.delogica.models.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // Para PUT /api/customers/{id}/addresses/{addressId}/default
    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);

    // Listar direcciones de un cliente
    List<Address> findByCustomerId(Long customerId);

    // Desmarcar cualquier predeterminada del cliente
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.customer.id = :customerId AND a.defaultAddress = true")
    int clearDefaultForCustomer(@Param("customerId") Long customerId);

    // Localizar la actual predeterminada si existe
    Optional<Address> findByCustomerIdAndDefaultAddressTrue(Long customerId);

}
