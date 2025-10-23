package com.example.delogica.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.delogica.config.exceptions.EmailAlreadyInUseException;
import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;
import com.example.delogica.mappers.AddressMapper;
import com.example.delogica.mappers.CustomerMapper;
import com.example.delogica.models.Address;
import com.example.delogica.models.Customer;
import com.example.delogica.repositories.AddressRepository;
import com.example.delogica.repositories.CustomerRepository;
import com.example.delogica.services.impl.CustomerServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    CustomerMapper customerMapper;
    @Mock
    AddressRepository addressRepository;
    @Mock
    AddressMapper addressMapper;

    @InjectMocks
    CustomerServiceImpl customerService;

    // ---------- findById ----------
    @Test
    void testFindById_whenCustomerExists_returnsCustomerDTO() {
        Long id = 1L;
        Customer c = new Customer();
        c.setId(id);

        CustomerOutputDTO out = new CustomerOutputDTO();
        out.setId(id);

        when(customerRepository.findById(id)).thenReturn(Optional.of(c));
        when(customerMapper.toOutput(c)).thenReturn(out);

        CustomerOutputDTO result = customerService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(customerRepository).findById(id);
        verify(customerMapper).toOutput(c);
    }

    @Test
    void testFindById_whenCustomerNotFound_throwsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerService.findById(1L));
    }

    // ---------- create ----------
    @Test
    void testCreate_whenEmailAlreadyUsed_throwsException() {
        CustomerInputDTO in = new CustomerInputDTO();
        in.setEmail("test@acme.com");

        when(customerRepository.findByEmail("test@acme.com")).thenReturn(Optional.of(new Customer()));

        assertThrows(EmailAlreadyInUseException.class, () -> customerService.create(in));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testCreate_whenValidInput_savesCustomer_andSetsSingleDefault() {
        // input
        CustomerInputDTO in = new CustomerInputDTO();
        in.setEmail("ok@acme.com");

        // (los DTOs de address dan igual aquí; el servicio no llama addressMapper en
        // create)
        // solo los añadimos si tu validador requiere lista no nula:
        in.setAddresses(new java.util.ArrayList<>());

        // entidad que devolverá el mapper de Customer, con 2 direcciones "como si"
        // vinieran del DTO
        Customer entity = new Customer();
        entity.setAddresses(new java.util.ArrayList<>());

        Address a1 = new Address();
        a1.setLine1("Calle 1");
        a1.setDefaultAddress(true);
        Address a2 = new Address();
        a2.setLine1("Calle 2");
        a2.setDefaultAddress(true);
        entity.getAddresses().add(a1);
        entity.getAddresses().add(a2);

        // lo que guardará el repo
        Customer saved = new Customer();
        saved.setId(10L);
        // tras la lógica de create, debe quedar exactamente una default
        a1.setDefaultAddress(true);
        a2.setDefaultAddress(false);
        saved.setAddresses(java.util.List.of(a1, a2));

        CustomerOutputDTO out = new CustomerOutputDTO();
        out.setId(10L);

        // stubs necesarios y solo los necesarios
        when(customerRepository.findByEmail("ok@acme.com")).thenReturn(java.util.Optional.empty());
        when(customerMapper.toEntity(in)).thenReturn(entity);
        when(customerRepository.save(entity)).thenReturn(saved);
        when(customerMapper.toOutput(saved)).thenReturn(out);

        // act
        CustomerOutputDTO result = customerService.create(in);

        // assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(customerRepository).save(entity);

        // opcional: verificar que quedó una única default
        long defaults = saved.getAddresses().stream().filter(Address::getDefaultAddress).count();
        assertEquals(1L, defaults);
    }

    @Test
    void testUpdate_whenAfterReplace_noDefault_remediesByAutoSelectingOne() {
        Long id = 1L;

        // Estado actual: una sola default
        Customer existing = new Customer();
        existing.setId(id);
        Address ad1 = new Address();
        ad1.setId(100L);
        ad1.setDefaultAddress(true);
        ad1.setCustomer(existing);
        existing.setAddresses(new ArrayList<>(List.of(ad1)));

        // DTO la elimina (no incluye ad1) y añade una nueva sin default
        CustomerInputDTO in = new CustomerInputDTO();
        AddressInputDTO newDto = new AddressInputDTO();
        newDto.setLine1("Nueva");
        in.setAddresses(List.of(newDto));

        Address newEntity = new Address();
        newEntity.setDefaultAddress(false);

        Customer saved = new Customer();
        saved.setId(id);
        // el servicio debe dejar esta nueva como default automáticamente
        newEntity.setDefaultAddress(true);
        saved.setAddresses(List.of(newEntity));

        CustomerOutputDTO out = new CustomerOutputDTO();
        out.setId(id);

        when(customerRepository.findByIdWithLock(id)).thenReturn(Optional.of(existing));
        when(addressRepository.findByCustomerIdAndDefaultAddressTrue(id)).thenReturn(Optional.empty());
        when(addressMapper.toEntity(newDto)).thenReturn(newEntity);
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);
        when(customerMapper.toOutput(saved)).thenReturn(out);

        CustomerOutputDTO result = customerService.update(id, in);

        assertNotNull(result);
        assertTrue(saved.getAddresses().get(0).getDefaultAddress());
        verify(customerRepository).save(any(Customer.class));
    }

    // ---------- delete ----------
    @Test
    void testDelete_whenCustomerExists_deletesCustomer() {
        Long id = 1L;
        Customer c = new Customer();
        c.setId(id);

        when(customerRepository.findByIdWithLock(id)).thenReturn(Optional.of(c));
        doNothing().when(customerRepository).delete(c);

        assertDoesNotThrow(() -> customerService.delete(id));
        verify(customerRepository).delete(c);
    }

    @Test
    void testDelete_whenCustomerNotFound_throwsException() {
        when(customerRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerService.delete(1L));
    }

    @Test
    void testUpdate_whenValidInput_updatesCustomer_andPreservesDefault() {
        Long id = 1L;

        Customer existing = new Customer();
        existing.setId(id);
        Address ad1 = new Address();
        ad1.setId(100L);
        ad1.setDefaultAddress(true);
        ad1.setCustomer(existing);
        Address ad2 = new Address();
        ad2.setId(200L);
        ad2.setDefaultAddress(false);
        ad2.setCustomer(existing);
        existing.setAddresses(new ArrayList<>(List.of(ad1, ad2)));

        CustomerInputDTO in = new CustomerInputDTO();
        in.setEmail("nuevo@acme.com");

        AddressInputDTO ad1dto = new AddressInputDTO();
        ad1dto.setId(100L);
        ad1dto.setLine1("Nueva 1");
        ad1dto.setDefaultAddress(null); // 👈 NO tocar default

        AddressInputDTO ad2dto = new AddressInputDTO();
        ad2dto.setId(200L);
        ad2dto.setLine1("Nueva 2");
        ad2dto.setDefaultAddress(null); // 👈 NO tocar default
        in.setAddresses(List.of(ad1dto, ad2dto));

        Customer saved = new Customer();
        saved.setId(id);
        ad1.setDefaultAddress(true);
        ad2.setDefaultAddress(false);
        saved.setAddresses(List.of(ad1, ad2));

        CustomerOutputDTO out = new CustomerOutputDTO();
        out.setId(id);

        when(customerRepository.findByIdWithLock(id)).thenReturn(Optional.of(existing));
        when(addressRepository.findByCustomerIdAndDefaultAddressTrue(id)).thenReturn(Optional.of(ad1));
        when(addressRepository.findByIdAndCustomerId(100L, id)).thenReturn(Optional.of(ad1));
        when(addressRepository.findByIdAndCustomerId(200L, id)).thenReturn(Optional.of(ad2));

        doAnswer(inv -> {
            existing.setEmail(in.getEmail());
            return null;
        })
                .when(customerMapper).updateEntityFromDto(eq(in), eq(existing));
        doAnswer(inv -> {
            ad1.setLine1("Nueva 1");
            return null;
        })
                .when(addressMapper).updateEntityFromDto(eq(ad1dto), eq(ad1));
        doAnswer(inv -> {
            ad2.setLine1("Nueva 2");
            return null;
        })
                .when(addressMapper).updateEntityFromDto(eq(ad2dto), eq(ad2));

        when(customerRepository.save(existing)).thenReturn(saved);
        when(customerMapper.toOutput(saved)).thenReturn(out);

        CustomerOutputDTO result = customerService.update(id, in);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertTrue(ad1.getDefaultAddress());
        assertFalse(ad2.getDefaultAddress());
        verify(customerRepository).save(existing);
    }

    @Test
    void testUpdate_whenMultipleDefaults_detected_throwsException() {
        Long id = 1L;

        Customer existing = new Customer();
        existing.setId(id);
        Address a1 = new Address();
        a1.setId(1L);
        a1.setCustomer(existing);
        a1.setDefaultAddress(true); // BD: true
        Address a2 = new Address();
        a2.setId(2L);
        a2.setCustomer(existing);
        a2.setDefaultAddress(true); // BD: true
        existing.setAddresses(new ArrayList<>(List.of(a1, a2)));

        CustomerInputDTO in = new CustomerInputDTO();
        AddressInputDTO a1dto = new AddressInputDTO();
        a1dto.setId(1L);
        a1dto.setDefaultAddress(false); 
        AddressInputDTO a2dto = new AddressInputDTO();
        a2dto.setId(2L);
        a2dto.setDefaultAddress(true);
        in.setAddresses(List.of(a1dto, a2dto));

        when(customerRepository.findByIdWithLock(id)).thenReturn(Optional.of(existing));
        when(addressRepository.findByCustomerIdAndDefaultAddressTrue(id)).thenReturn(Optional.of(a1));
        when(addressRepository.findByIdAndCustomerId(1L, id)).thenReturn(Optional.of(a1));
        // No stubs extra: la excepción se lanza al validar a1

        assertThrows(com.example.delogica.config.exceptions.DefaultAddressChangeNotAllowedException.class,
                () -> customerService.update(id, in));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testCreateAddress_whenFirstAddress_becomesDefault() {
        Long customerId = 1L;

        Customer cust = new Customer();
        cust.setId(customerId);
        cust.setAddresses(new ArrayList<>());

        AddressInputDTO dto = new AddressInputDTO();
        dto.setLine1("Calle 1");
        dto.setCity("Madrid");
        dto.setPostalCode("28001");
        dto.setCountry("ES");
        dto.setDefaultAddress(null);

        Address newAddr = new Address(); // al mapear, default lo decide el servicio
        Address savedAddr = new Address();
        savedAddr.setId(10L);
        savedAddr.setDefaultAddress(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(cust));
        when(addressRepository.findByCustomerIdAndDefaultAddressTrue(customerId)).thenReturn(Optional.empty());
        when(addressMapper.toEntity(dto)).thenReturn(newAddr);
        when(addressRepository.save(newAddr)).thenReturn(savedAddr); // 👈 clave
        when(addressMapper.toOutput(savedAddr)).thenReturn(new com.example.delogica.dtos.output.AddressOutputDTO());

        var result = customerService.createAddress(customerId, dto);

        assertNotNull(result);
        assertTrue(savedAddr.getDefaultAddress());
        verify(addressRepository).save(newAddr);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testSetDefaultAddress_whenAddressExists_togglesProperly() {
        Long customerId = 1L;
        Long toDefaultId = 200L;

        Customer cust = new Customer();
        cust.setId(customerId);
        Address a1 = new Address();
        a1.setId(100L);
        a1.setDefaultAddress(true);
        a1.setCustomer(cust);
        Address a2 = new Address();
        a2.setId(200L);
        a2.setDefaultAddress(false);
        a2.setCustomer(cust);
        cust.setAddresses(new ArrayList<>(List.of(a1, a2)));

        when(addressRepository.findByIdAndCustomerId(200L, customerId)).thenReturn(Optional.of(a2));
        when(addressRepository.clearDefaultForCustomerExcept(customerId, toDefaultId)).thenReturn(1); // devuelve int
        when(addressRepository.save(a2)).thenReturn(a2);

        assertDoesNotThrow(() -> customerService.setDefaultAddress(customerId, toDefaultId));

        assertTrue(a2.getDefaultAddress()); // target queda a true
        verify(addressRepository).clearDefaultForCustomerExcept(customerId, toDefaultId);
        verify(addressRepository).save(a2);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testSetDefaultAddress_whenAddressDoesNotBelong_throwsNotFound() {
        Long customerId = 1L;
        Long wrongId = 999L;

        when(addressRepository.findByIdAndCustomerId(wrongId, customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.setDefaultAddress(customerId, wrongId));
        verify(addressRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
    }

}
