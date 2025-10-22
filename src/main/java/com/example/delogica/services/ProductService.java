package com.example.delogica.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.delogica.dtos.input.ProductInputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;

public interface ProductService {

    ProductOutputDTO create (ProductInputDTO input );

    ProductOutputDTO findById(Long productId);

    Page<ProductOutputDTO> search (Pageable pageable, String name, Boolean active);

    ProductOutputDTO update (Long productId, ProductInputDTO input);

    void delete (Long productId);
}
