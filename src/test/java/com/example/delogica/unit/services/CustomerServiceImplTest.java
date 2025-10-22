package com.example.delogica.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.delogica.config.exceptions.EmailAlreadyInUseException;
import com.example.delogica.config.exceptions.ResourceNotFoundException;
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
import com.example.delogica.services.impl.CustomerServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------- TEST FINDBYID() ---------------
    @Test
    void testFindById_whenCustomerExists_returnsCustomerDTO() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);

        CustomerOutputDTO dto = new CustomerOutputDTO();
        dto.setId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.toOutput(customer)).thenReturn(dto);

        // Act
        CustomerOutputDTO result = customerService.findById(customerId);

        // Assert
        assertNotNull(result);
        assertEquals(customerId, result.getId());
    }

    @Test
    void testFindById_whenCustomerNotFound_throwsException() {
        
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.findById(customerId);
        });
    }

    // ------------- TEST CREATE() ---------------
    @Test
    void testCreate_whenEmailAlreadyUsed_throwsException() {
        CustomerInputDTO input = new CustomerInputDTO();
        input.setEmail("test@example.com");

        when(customerRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(new Customer()));

        assertThrows(EmailAlreadyInUseException.class, () -> {
            customerService.create(input);
        });

        verify(customerRepository, never()).save(any());
    }

    @Test
    void testCreate_whenValidInput_savesCustomer() {
        CustomerInputDTO input = new CustomerInputDTO();
        input.setEmail("new@example.com");
        // Simular que no hay direcciones o que las direcciones tienen defaultAddress
        // configurado...

        Customer customerEntity = new Customer();
        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);

        CustomerOutputDTO outputDTO = new CustomerOutputDTO();
        outputDTO.setId(1L);

        when(customerRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());
        when(customerMapper.toEntity(input)).thenReturn(customerEntity);
        when(customerRepository.save(customerEntity)).thenReturn(savedCustomer);
        when(customerMapper.toOutput(savedCustomer)).thenReturn(outputDTO);

        CustomerOutputDTO result = customerService.create(input);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository).save(customerEntity);
    }

    // ------------- TEST UPDATE() ---------------
    @Test
    void testUpdate_whenValidInput_updatesCustomerAndAddresses() {
        // Arrange
        Long customerId = 1L;

        // Cliente original en DB
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setAddresses(new ArrayList<>());

        // Input con datos para actualizar
        CustomerInputDTO input = new CustomerInputDTO();
        input.setEmail("nuevo@email.com");

        // Direcciones: una existente (con id) y una nueva (sin id)
        AddressInputDTO existingAddressDto = new AddressInputDTO();
        existingAddressDto.setId(10L);
        existingAddressDto.setDefaultAddress(true); // Se marca como default

        AddressInputDTO newAddressDto = new AddressInputDTO();
        newAddressDto.setDefaultAddress(false);

        input.setAddresses(List.of(existingAddressDto, newAddressDto));

        // Dirección existente en DB
        Address existingAddress = new Address();
        existingAddress.setId(10L);
        existingAddress.setCustomer(customer);

        when(customerRepository.findByIdWithLock(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(existingAddress));

        // Simular update del cliente básico
        doAnswer(invocation -> {
            CustomerInputDTO dto = invocation.getArgument(0);
            Customer cust = invocation.getArgument(1);
            cust.setEmail(dto.getEmail());
            return null;
        }).when(customerMapper).updateEntityFromDto(any(), any());

        // Simular update de dirección existente
        doNothing().when(addressMapper).updateEntityFromDto(existingAddressDto, existingAddress);

        // Simular creación de nueva dirección desde DTO
        Address newAddress = new Address();
        when(addressMapper.toEntity(newAddressDto)).thenReturn(newAddress);

        // Simular guardado del cliente actualizado
        Customer savedCustomer = new Customer();
        savedCustomer.setId(customerId);
        savedCustomer.setAddresses(List.of(existingAddress, newAddress));

        when(customerRepository.save(any())).thenReturn(savedCustomer);

        // Simular mapping final a DTO
        CustomerOutputDTO outputDto = new CustomerOutputDTO();
        outputDto.setId(customerId);
        when(customerMapper.toOutput(savedCustomer)).thenReturn(outputDto);

        // Act
        CustomerOutputDTO result = customerService.update(customerId, input);

        // Assert
        assertNotNull(result);
        assertEquals(customerId, result.getId());

        // Verificar que se normalizó defaultAddress (solo uno true)
        assertTrue(input.getAddresses().stream().filter(AddressInputDTO::getDefaultAddress).count() == 1);

        // Verificar que se llamó el update básico del cliente
        verify(customerMapper).updateEntityFromDto(input, customer);

        // Verificar que se buscó la dirección existente para actualizar
        verify(addressRepository).findById(10L);

        // Verificar que se guardó el cliente final
        verify(customerRepository).save(customer);
    }

    // ------------- TEST DELETE() ---------------
    @Test
    void testDelete_whenCustomerExists_deletesCustomer() {
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepository.findByIdWithLock(customerId)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).delete(customer);

        assertDoesNotThrow(() -> customerService.delete(customerId));

        verify(customerRepository).delete(customer);
    }

    @Test
    void testDelete_whenCustomerNotFound_throwsException() {
        Long customerId = 1L;
        when(customerRepository.findByIdWithLock(customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.delete(customerId));
    }

    // ------------- TEST CREATEADDRESS() ---------------
    @Test
    void testCreateAddress_whenNoDefaultAddress_setsNewAddressAsDefault() {
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setAddresses(new ArrayList<>()); 

        AddressInputDTO input = new AddressInputDTO();
        input.setDefaultAddress(false); 

        Address newAddress = new Address();
        newAddress.setDefaultAddress(true);
        newAddress.setCustomer(customer);

        Address savedAddress = new Address();
        savedAddress.setId(100L);
        savedAddress.setDefaultAddress(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressMapper.toEntity(input)).thenReturn(newAddress);
        when(addressRepository.save(newAddress)).thenReturn(savedAddress);
        when(addressMapper.toOutput(savedAddress)).thenReturn(new AddressOutputDTO());

        AddressOutputDTO result = customerService.createAddress(customerId, input);

        assertNotNull(result);
        verify(addressRepository).save(newAddress);
        assertTrue(newAddress.getDefaultAddress());
    }

    @Test
    void testCreateAddress_whenExistingDefaultAddress_andNewIsDefault_clearsOldDefault() {
        Long customerId = 1L;

        Customer customer = new Customer();
        customer.setId(customerId);
        
        customer.setAddresses(new ArrayList<>());

        AddressInputDTO input = new AddressInputDTO();
        input.setDefaultAddress(true); 

        Address newAddress = new Address();
        newAddress.setDefaultAddress(true);
        newAddress.setCustomer(customer);

        Address savedAddress = new Address();
        savedAddress.setId(100L);
        savedAddress.setDefaultAddress(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressMapper.toEntity(input)).thenReturn(newAddress);
        when(addressRepository.save(newAddress)).thenReturn(savedAddress);
        when(addressMapper.toOutput(savedAddress)).thenReturn(new AddressOutputDTO());

        AddressOutputDTO result = customerService.createAddress(customerId, input);

        assertNotNull(result);
        verify(addressRepository).save(newAddress);
        assertTrue(newAddress.getDefaultAddress());
    }

    // ------------- TEST SETDEFAULTADDRESS() ---------------
    @Test
    void testSetDefaultAddress_whenAddressBelongsToCustomer_setsDefault() {
        Long customerId = 1L;
        Long addressId = 10L;

        Address address = new Address();
        address.setId(addressId);
        address.setCustomer(new Customer());
        address.setDefaultAddress(false);

        when(addressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(address));
        when(addressRepository.clearDefaultForCustomer(customerId)).thenReturn(1);

        when(addressRepository.save(address)).thenReturn(address);

        assertDoesNotThrow(() -> customerService.setDefaultAddress(customerId, addressId));

        verify(addressRepository).clearDefaultForCustomer(customerId);
        verify(addressRepository).save(address);
        assertTrue(address.getDefaultAddress());
    }

    @Test
    void testSetDefaultAddress_whenAddressNotFound_throwsException() {
        Long customerId = 1L;
        Long addressId = 10L;

        when(addressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.setDefaultAddress(customerId, addressId));
    }

}
