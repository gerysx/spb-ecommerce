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

        // Verificar que el email no esté en uso
        customerRepository.findByEmail(input.getEmail())
                .ifPresent(c -> {
                    logger.warn("Email ya en uso: {}", input.getEmail());
                    throw new EmailAlreadyInUseException(input.getEmail());
                });

        // Mapear cliente sin direcciones (mapper ignora direcciones)
        Customer customerEntity = customerMapper.toEntity(input);

        // Procesar las direcciones manualmente
        if (input.getAddresses() != null && !input.getAddresses().isEmpty()) {
            List<Address> addressesToAdd = new ArrayList<>();

            // Buscar índice de la primera dirección marcada como default en el input
            int firstDefaultIndex = -1;
            for (int i = 0; i < input.getAddresses().size(); i++) {
                AddressInputDTO addressDto = input.getAddresses().get(i);
                if (Boolean.TRUE.equals(addressDto.getDefaultAddress())) {
                    firstDefaultIndex = i;
                    break;
                }
            }

            // Mapear y asignar customer y defaultAddress
            for (int i = 0; i < input.getAddresses().size(); i++) {
                AddressInputDTO addressDto = input.getAddresses().get(i);
                Address addressEntity = addressMapper.toEntity(addressDto);
                addressEntity.setCustomer(customerEntity);

                if (firstDefaultIndex == -1) {
                    // No hay default en el input, marcar la primera dirección como default
                    addressEntity.setDefaultAddress(i == 0);
                } else {
                    // Solo la primera dirección con default=true se mantiene así
                    addressEntity.setDefaultAddress(i == firstDefaultIndex);
                }

                addressesToAdd.add(addressEntity);
            }

            // Asignar las direcciones al cliente
            customerEntity.setAddresses(addressesToAdd);
        }

        // Guardar cliente junto con las direcciones (en cascada)
        Customer savedCustomer = customerRepository.save(customerEntity);

        logger.info("Cliente creado correctamente: {}", savedCustomer.getId());
        return customerMapper.toOutput(savedCustomer);
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
        logger.info("Creando dirección para cliente {} (defaultAddress en payload: {})",
                customerId, input.getDefaultAddress());

        // 1) Cargar cliente
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forId(Customer.class, customerId));

        // 2) No permitir pedir default desde aquí
        if (Boolean.TRUE.equals(input.getDefaultAddress())) {
            logger.warn("Intento de marcar default por POST /addresses. Bloqueado.");
            throw new DefaultAddressChangeNotAllowedException(
                    "No se permite cambiar la dirección por defecto desde esta operación.");
        }

        // 3) Comprobar si ya existe una default en BD (NO dependas de
        // customer.getAddresses())
        boolean existsDefault = addressRepository.findByCustomerIdAndDefaultAddressTrue(customerId).isPresent();

        // 4) Mapear y fijar el flag default correctamente
        Address newAddress = addressMapper.toEntity(input);
        newAddress.setCustomer(customer);
        newAddress.setDefaultAddress(!existsDefault); // primera dirección -> true, si no -> false

        Address saved = addressRepository.save(newAddress);
        logger.info("Dirección {} creada para cliente {} (default: {})", saved.getId(), customerId,
                saved.getDefaultAddress());

        return addressMapper.toOutput(saved);
    }

    @Override
    @Transactional
    public CustomerOutputDTO update(Long customerId, CustomerInputDTO input) {
        logger.info("Actualizando cliente {} (se bloqueará cambio de defaultAddress)", customerId);

        Customer customer = customerRepository.findByIdWithLock(customerId)
                .orElseThrow(() -> ResourceNotFoundException.forId(Customer.class, customerId));

        // Actualiza campos simples del cliente
        customerMapper.updateEntityFromDto(input, customer);

        // Si vienen direcciones en el DTO, procesamos merge
        if (input.getAddresses() != null) {

            // 1) ID de la default actual en BD (si existe)
            Long currentDefaultId = addressRepository.findByCustomerIdAndDefaultAddressTrue(customerId)
                    .map(Address::getId)
                    .orElse(null);

            // 2) Validación: no se puede cambiar la default desde aquí
            for (AddressInputDTO dto : input.getAddresses()) {

                // a) Si el DTO trae defaultAddress=true en una nueva -> prohibido
                if (dto.getId() == null && Boolean.TRUE.equals(dto.getDefaultAddress())) {
                    logger.warn("Intento de crear nueva dirección como default en PUT /customers");
                    throw new DefaultAddressChangeNotAllowedException(
                            "No se permite cambiar la dirección por defecto desde esta operación.");
                }

                // b) Si es una existente y su flag default en el DTO difiere del estado real ->
                // prohibido
                if (dto.getId() != null && dto.getDefaultAddress() != null) {
                    Address existing = addressRepository.findByIdAndCustomerId(dto.getId(), customerId)
                            .orElseThrow(() -> ResourceNotFoundException.forId(Address.class, dto.getId()));

                    boolean dtoDefault = Boolean.TRUE.equals(dto.getDefaultAddress());
                    boolean dbDefault = Boolean.TRUE.equals(existing.getDefaultAddress());

                    if (dtoDefault != dbDefault) {
                        logger.warn("Intento de modificar defaultAddress en PUT /customers (addrId={}, dto={}, db={})",
                                dto.getId(), dtoDefault, dbDefault);
                        throw new DefaultAddressChangeNotAllowedException(
                                "No se permite cambiar la dirección por defecto desde esta operación.");
                    }
                }
            }

            // 3) Reconstruir la colección preservando la default
            List<Address> rebuilt = new ArrayList<>();
            for (AddressInputDTO dto : input.getAddresses()) {
                if (dto.getId() != null) {
                    Address existing = addressRepository.findByIdAndCustomerId(dto.getId(), customerId)
                            .orElseThrow(() -> ResourceNotFoundException.forId(Address.class, dto.getId()));

                    // Actualiza campos (sin tocar default)
                    addressMapper.updateEntityFromDto(dto, existing);
                    existing.setCustomer(customer);
                    existing.setDefaultAddress(Boolean.TRUE.equals(existing.getDefaultAddress())); // explícito

                    rebuilt.add(existing);

                } else {
                    // Nueva dirección SIEMPRE entra con default=false aquí
                    Address created = addressMapper.toEntity(dto);
                    created.setCustomer(customer);
                    created.setDefaultAddress(false);
                    rebuilt.add(created);
                }
            }

            // 4) Si no hay default en BD (caso borde: cliente sin direcciones antes) =>
            // fuerza una
            boolean rebuiltHasDefault = rebuilt.stream().anyMatch(Address::getDefaultAddress);
            if (currentDefaultId == null && !rebuiltHasDefault && !rebuilt.isEmpty()) {
                logger.info("No había default previa; se fuerza la primera como default en el update.");
                rebuilt.get(0).setDefaultAddress(true);
            }

            // 5) Reemplazar colección del agregado
            List<Address> old = new ArrayList<>(customer.getAddresses());
            for (Address a : old)
                customer.removeAddress(a);
            for (Address a : rebuilt)
                customer.addAddress(a);
        }

        Customer saved = customerRepository.save(customer);
        logger.info("Cliente {} actualizado correctamente", customerId);
        return customerMapper.toOutput(saved);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long customerId, Long addressId) {
        logger.info("Estableciendo dirección {} como default para cliente {}", addressId, customerId);

        Address target = addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dirección no encontrada para el cliente con id: " + customerId));

        // Desmarcar todas menos la target
        addressRepository.clearDefaultForCustomerExcept(customerId, addressId);

        if (Boolean.FALSE.equals(target.getDefaultAddress())) {
            target.setDefaultAddress(true);
            addressRepository.save(target);
        }

        logger.info("Default establecida correctamente: cliente={}, address={}", customerId, addressId);
    }
}
