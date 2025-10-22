package com.example.delogica.controllers;

import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;
import com.example.delogica.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    // POST /api/customers  -> 201 Created + Location
    @PostMapping
    public ResponseEntity<CustomerOutputDTO> createCustomer(@Valid @RequestBody CustomerInputDTO input) {
        logger.info("Creando cliente con email: {}", input.getEmail());
        CustomerOutputDTO created = customerService.create(input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        logger.info("Cliente creado con ID: {}", created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // GET /api/customers  -> 200 OK (Page)
    @GetMapping
    public Page<CustomerOutputDTO> listCustomers(
            @RequestParam(required = false) String email,
            Pageable pageable) {
        logger.info("Listando clientes con filtro email: {}", email);
        Page<CustomerOutputDTO> result = (email != null && !email.isBlank())
                ? customerService.searchCustomers(email, pageable)
                : customerService.findAll(pageable);
        logger.info("Se encontraron {} clientes", result.getTotalElements());
        return result;
    }

    // GET /api/customers/{id}  -> 200 OK
    @GetMapping("/{id}")
    public CustomerOutputDTO getCustomer(@PathVariable Long id) {
        logger.info("Buscando cliente con ID: {}", id);
        CustomerOutputDTO customer = customerService.findById(id);
        logger.info("Cliente encontrado con ID: {}", id);
        return customer;
    }

    // PUT /api/customers/{id}  -> 200 OK
    @PutMapping("/{id}")
    public CustomerOutputDTO updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerInputDTO input) {
        logger.info("Actualizando cliente con ID: {}", id);
        CustomerOutputDTO response = customerService.update(id, input);
        logger.info("Cliente actualizado con ID: {}", id);
        return response;
    }

    // DELETE /api/customers/{id}  -> 204 No Content
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        logger.info("Eliminando cliente con ID: {}", id);
        customerService.delete(id);
        logger.info("Cliente eliminado con ID: {}", id);
    }

    // POST /api/customers/{id}/addresses  -> 201 Created + Location
    @PostMapping("/{id}/addresses")
    public ResponseEntity<AddressOutputDTO> createAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressInputDTO input) {
        logger.info("Creando dirección para cliente ID: {}", id);
        AddressOutputDTO created = customerService.createAddress(id, input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{addressId}")
                .buildAndExpand(created.getId())
                .toUri();

        logger.info("Dirección {} creada para cliente {}", created.getId(), id);
        return ResponseEntity.created(location).body(created);
    }

    // PUT /api/customers/{id}/addresses/{addressId}/default  -> 204 No Content
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
