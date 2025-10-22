package com.example.delogica.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.delogica.models.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);

    List<Address> findByCustomerId(Long customerId);

    // Desmarca todas menos la que quieres mantener
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Address a set a.defaultAddress = false where a.customer.id = :customerId and a.id <> :keepId")
    int clearDefaultForCustomerExcept(@Param("customerId") Long customerId, @Param("keepId") Long keepId);

    // Localiza la actual predeterminada (sin params extra)
    Optional<Address> findByCustomerIdAndDefaultAddressTrue(Long customerId);
}
