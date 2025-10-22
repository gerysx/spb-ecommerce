package com.example.delogica.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;

public interface CustomerService {
    
    CustomerOutputDTO create (CustomerInputDTO input);

    CustomerOutputDTO findById(Long customerId);

    CustomerOutputDTO update (Long customerId, CustomerInputDTO input);

    void delete (Long customerId);

    Page<CustomerOutputDTO> findAll (Pageable pageable);

    Page<CustomerOutputDTO> searchCustomers(String email, Pageable pageable);

    AddressOutputDTO createAddress(Long customerId, AddressInputDTO input);
    void setDefaultAddress(Long customerId, Long addressId);
}
