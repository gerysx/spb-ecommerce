package com.example.delogica.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.delogica.config.exceptions.DefaultAddressChangeNotAllowedException;
import com.example.delogica.config.exceptions.EmailAlreadyInUseException;
import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.config.specifications.CustomerSpecifications;
import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;
import com.example.delogica.mappers.AddressMapper;
import com.example.delogica.mappers.CustomerMapper;
import com.example.delogica.models.Address;
import com.example.delogica.models.Customer;
import com.example.delogica.repositories.AddressRepository;
import com.example.delogica.repositories.CustomerRepository;
import com.example.delogica.services.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Override
    @Transactional
    public CustomerOutputDTO create(CustomerInputDTO input) {
        logger.info("Creando cliente con email: {}", input.getEmail());

        customerRepository.findByEmail(input.getEmail())
                .ifPresent(c -> {
                    logger.warn("Email ya en uso: {}", input.getEmail());
                    throw new EmailAlreadyInUseException(input.getEmail());
                });

        Customer customer = customerMapper.toEntity(input);

        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            // relación inversa + limpiar defaults nulos
            customer.getAddresses().forEach(a -> {
                a.setCustomer(customer);
                a.setDefaultAddress(Boolean.TRUE.equals(a.getDefaultAddress())); // normaliza null->false
            });

            boolean anyDefault = customer.getAddresses().stream().anyMatch(Address::getDefaultAddress);
            if (!anyDefault) {
                customer.getAddresses().get(0).setDefaultAddress(true);
            }
        }

        Customer saved = customerRepository.save(customer);
        logger.info("Cliente creado correctamente: {}", saved.getId());
        return customerMapper.toOutput(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOutputDTO findById(Long customerId) {
        Customer findCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forId(Customer.class, customerId));
        return customerMapper.toOutput(findCustomer);
    }

    @Override
    @Transactional
    public CustomerOutputDTO update(Long customerId, CustomerInputDTO input) {
        logger.info("Actualizando cliente con id: {}", customerId);

        Customer customer = customerRepository.findByIdWithLock(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forId(Customer.class, customerId));

        // 1) Datos básicos
        customerMapper.updateEntityFromDto(input, customer);

        // 2) Detectar default actual
        Long currentDefaultId = customer.getAddresses() == null ? null
                : customer.getAddresses().stream()
                        .filter(Address::getDefaultAddress)
                        .map(Address::getId)
                        .findFirst()
                        .orElse(null);

        // 3) ¿El payload intenta cambiar la default?
        if (input.getAddresses() != null) {
            boolean triesToSwitchDefault = input.getAddresses().stream().anyMatch(dto -> {
                // Si marca default=true en una dirección distinta a la actual => intenta
                // cambiar
                Boolean wantsDefault = dto.getDefaultAddress();
                if (Boolean.TRUE.equals(wantsDefault)) {
                    // Si no hay default actual, permitiríamos que exista una (ver más abajo),
                    // pero como la regla es NO cambiar default via PUT, sólo lo permitimos si
                    // es la misma dirección que ya era default
                    return currentDefaultId == null || (dto.getId() != null && !dto.getId().equals(currentDefaultId));
                }
                return false;
            });

            if (triesToSwitchDefault) {
                throw new DefaultAddressChangeNotAllowedException();
            }
        }

        // 4) Procesar direcciones SIN tocar defaultAddress
        // (ignoramos cualquier default del DTO)
        List<Address> toKeep = new ArrayList<>();
        if (input.getAddresses() != null) {
            for (AddressInputDTO dto : input.getAddresses()) {
                if (dto.getId() != null) {
                    Address existing = addressRepository.findById(dto.getId())
                            .orElseThrow(() -> ResourceNotFoundException.forId(Address.class, dto.getId()));
                    addressMapper.updateEntityFromDto(dto, existing);
                    // mantenemos el default que tenía
                    existing.setDefaultAddress(existing.getDefaultAddress() != null && existing.getDefaultAddress());
                    existing.setCustomer(customer);
                    toKeep.add(existing);
                } else {
                    Address newAddr = addressMapper.toEntity(dto);
                    newAddr.setCustomer(customer);
                    // toda nueva dirección creada por PUT no puede ser default aquí
                    newAddr.setDefaultAddress(false);
                    toKeep.add(newAddr);
                }
            }
        }

        // reemplazar la colección (PUT semántica)
        customer.getAddresses().clear();
        customer.getAddresses().addAll(toKeep);

        // 5) Garantizar que quede EXACTAMENTE UNA default
        boolean hasDefault = customer.getAddresses().stream().anyMatch(Address::getDefaultAddress);
        if (!hasDefault && !customer.getAddresses().isEmpty()) {
            // si se “perdió” la default (p.ej. se eliminó la que lo era),
            // fuerza la primera como default
            customer.getAddresses().get(0).setDefaultAddress(true);
        }

        Customer saved = customerRepository.save(customer);
        logger.info("Cliente actualizado correctamente con id: {}", customerId);
        return customerMapper.toOutput(saved);
    }

    @Override
    @Transactional
    public void delete(Long customerId) {
        logger.info("Eliminando cliente con id: {}", customerId);
        Customer findCustomer = customerRepository.findByIdWithLock(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forId(Customer.class, customerId));
        customerRepository.delete(findCustomer);
        logger.info("Cliente eliminado con id: {}", customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerOutputDTO> findAll(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        return page.map(customerMapper::toOutput);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerOutputDTO> searchCustomers(String email, Pageable pageable) {
        Specification<Customer> spec = CustomerSpecifications.emailContains(email);
        Page<Customer> page = customerRepository.findAll(spec, pageable);
        return page.map(customerMapper::toOutput);
    }

    @Override
    @Transactional
    public AddressOutputDTO createAddress(Long customerId, AddressInputDTO input) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forId(Customer.class, customerId));

        Address newAddress = addressMapper.toEntity(input);

        // Asignamos la relación inversa
        newAddress.setCustomer(customer);

        // Chequeamos si ya existe una dirección default
        boolean hasDefault = customer.getAddresses() != null &&
                customer.getAddresses().stream()
                        .anyMatch(Address::getDefaultAddress);

        if (!hasDefault) {
            // Si no hay ninguna default, esta debe ser default sí o sí
            newAddress.setDefaultAddress(true);

        } else {
            // Si ya hay una default y la nueva viene con isDefault = true, limpiamos la
            // anterior
            if (input.getDefaultAddress()) {
                addressRepository.clearDefaultForCustomer(customerId);
                newAddress.setDefaultAddress(true);
            } else {
                newAddress.setDefaultAddress(false);
            }
        }

        // Asegurarse que la lista de direcciones es mutable antes de añadir
        if (customer.getAddresses() == null) {
            customer.setAddresses(new ArrayList<>());
        } else if (!(customer.getAddresses() instanceof ArrayList)) {
            customer.setAddresses(new ArrayList<>(customer.getAddresses()));
        }

        // Añadimos la nueva dirección
        customer.getAddresses().add(newAddress);

        // Guardamos la nueva dirección
        Address saved = addressRepository.save(newAddress);

        return addressMapper.toOutput(saved);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long customerId, Long addressId) {
        // Validar que la dirección pertenece al cliente
        Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dirección no encontrada para el cliente con id: " + customerId));

        // Limpiar cualquier dirección marcada como default previamente
        addressRepository.clearDefaultForCustomer(customerId);

        // Marcar esta dirección como default
        address.setDefaultAddress(true);

        addressRepository.save(address);
    }
}
