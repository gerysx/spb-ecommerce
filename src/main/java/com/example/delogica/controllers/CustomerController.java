package com.example.delogica.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;
import com.example.delogica.services.CustomerService;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    // Crear cliente
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOutputDTO createCustomer(@Valid @RequestBody CustomerInputDTO input) {
        logger.info("Creando cliente con email: {}", input.getEmail());
        CustomerOutputDTO response = customerService.create(input);
        logger.info("Cliente creado con ID: {}", response.getId());
        return response;
    }

    // Listar clientes con paginación y filtro opcional por email
    @GetMapping
    public Page<CustomerOutputDTO> listCustomers(
            @RequestParam(required = false) String email,
            Pageable pageable) {

        logger.info("Listando clientes con filtro email: {}", email);
        Page<CustomerOutputDTO> result;
        if (email != null && !email.isBlank()) {
            result = customerService.searchCustomers(email, pageable);
        } else {
            result = customerService.findAll(pageable);
        }
        logger.info("Se encontraron {} clientes", result.getTotalElements());
        return result;
    }

    // Detalle cliente
    @GetMapping("/{id}")
    public CustomerOutputDTO getCustomer(@PathVariable Long id) {
        logger.info("Buscando cliente con ID: {}", id);
        CustomerOutputDTO customer = customerService.findById(id);
        logger.info("Cliente encontrado con ID: {}", id);
        return customer;
    }

    // Actualizar cliente
    @PutMapping("/{id}")
    public CustomerOutputDTO updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerInputDTO input) {
        logger.info("Actualizando cliente con ID: {}", id);
        CustomerOutputDTO response = customerService.update(id, input);
        logger.info("Cliente actualizado con ID: {}", id);
        return response;
    }

    // Eliminar cliente
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        logger.info("Eliminando cliente con ID: {}", id);
        customerService.delete(id);
        logger.info("Cliente eliminado con ID: {}", id);
    }

    // Crear dirección para cliente
    @PostMapping("/{id}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressOutputDTO createAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressInputDTO input) {
        logger.info("Creando dirección para cliente ID: {}", id);
        AddressOutputDTO response = customerService.createAddress(id, input);
        logger.info("Dirección creada para cliente ID: {}", id);
        return response;
    }

    // Marcar dirección como predeterminada
    @PutMapping("/{id}/addresses/{addressId}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAddressAsDefault(
            @PathVariable Long id,
            @PathVariable Long addressId) {
        logger.info("Marcando dirección {} como predeterminada para cliente {}", addressId, id);
        customerService.setDefaultAddress(id, addressId);
        logger.info("Dirección {} marcada como predeterminada para cliente {}", addressId, id);
    }
}
