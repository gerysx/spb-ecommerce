package com.example.delogica.integration.controllers;

import com.example.delogica.config.errors.ErrorCode;
import com.example.delogica.integration.common.AbstractIntegrationTest;
import com.example.delogica.models.Address;
import com.example.delogica.models.Customer;
import com.example.delogica.repositories.AddressRepository;
import com.example.delogica.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para verificar el comportamiento de direcciones por defecto en clientes.
 */
@ActiveProfiles("testing")
@Transactional
class CustomerDefaultAddressIntegrationTest extends AbstractIntegrationTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private AddressRepository addressRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setFullName("Ana García");
        customer.setEmail(("ana" + System.nanoTime() + "@t.es")); // corto para @Size(max=30)
        customer.setPhone("654987321");
        customer = customerRepository.save(customer);
    }

    // ---------- helpers ----------
    private String addressJson(String line1, String city, String postal, String country, Boolean defaultAddress) {
        return """
            {
              "line1": "%s",
              "line2": null,
              "city": "%s",
              "postalCode": "%s",
              "country": "%s",
              "defaultAddress": %s
            }
        """.formatted(line1, city, postal, country, String.valueOf(defaultAddress));
    }

    private String updateCustomerJson(String fullName, String email, String phone, String addressesJson) {
        return """
            {
              "fullName": "%s",
              "email": "%s",
              "phone": "%s",
              "addresses": %s
            }
        """.formatted(fullName, email, phone, addressesJson);
    }

    private Address createAddress(String line1, String city, String postal, String country, boolean isDefault) {
        Address address = new Address();
        address.setCustomer(customer);
        address.setLine1(line1);
        address.setCity(city);
        address.setPostalCode(postal);
        address.setCountry(country);
        address.setDefaultAddress(isDefault);
        return addressRepository.save(address);
    }

    // ========== 1) Primera dirección creada → debe ser default automáticamente ==========
    @Test
    void createAddress_firstBecomesDefault() throws Exception {
        String payload = addressJson("Calle Sol 45", "Sevilla", "41001", "España", null);

        mockMvc.perform(authPost("/api/customers/{id}/addresses", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.defaultAddress").value(true));

        var addrs = addressRepository.findByCustomerId(customer.getId());
        assertThat(addrs).hasSize(1);
        assertThat(addrs.get(0).getDefaultAddress()).isTrue();
    }

    // ========== 2) Crear segunda dirección y marcarla por endpoint ==========
    @Test
    void createSecondAddress_thenSetDefaultViaEndpoint() throws Exception {
        Address a1 = createAddress("Calle Sol 45", "Sevilla", "41001", "España", true);

        String payload2 = addressJson("Plaza Mayor 10", "Granada", "18001", "España", null);
        mockMvc.perform(authPost("/api/customers/{id}/addresses", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload2))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.defaultAddress").value(false));

        addressRepository.flush();
        List<Address> all = addressRepository.findByCustomerId(customer.getId());
        Address a2 = all.stream()
                .filter(a -> "Plaza Mayor 10".equals(a.getLine1()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(authPut("/api/customers/{id}/addresses/{addressId}/default",
                customer.getId(), a2.getId()))
            .andExpect(status().isNoContent());

        addressRepository.flush();

        var r1 = addressRepository.findById(a1.getId()).orElseThrow();
        var r2 = addressRepository.findById(a2.getId()).orElseThrow();

        assertThat(r1.getDefaultAddress()).isFalse();
        assertThat(r2.getDefaultAddress()).isTrue();
    }

    // ========== 3) Intentar cambiar defaultAddress vía PUT /customers ==========
    @Test
    void updateCustomer_tryToChangeDefaultAddress_throwsValidationError() throws Exception {
        Address a1 = createAddress("Av. Uno 1", "Madrid", "28001", "España", true);
        Address a2 = createAddress("Av. Dos 2", "Madrid", "28002", "España", false);

        String body = updateCustomerJson(
                "Ana G. Edit",
                customer.getEmail(),
                customer.getPhone(),
                """
                [
                  { "id": %d, "line1": "Av. Uno 1", "city":"Madrid", "postalCode":"28001", "country":"España", "defaultAddress": false },
                  { "id": %d, "line1": "Av. Dos 2", "city":"Madrid", "postalCode":"28002", "country":"España", "defaultAddress": true  }
                ]
                """.formatted(a1.getId(), a2.getId())
        );

        mockMvc.perform(authPut("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    // ========== 4) PUT sin tocar default ==========
    @Test
    void updateCustomer_withoutModifyingDefaultAddress_ok() throws Exception {
        Address a1 = createAddress("Av. Uno 1", "Madrid", "28001", "España", true);

        String body = updateCustomerJson(
                "Ana G. Edit",
                customer.getEmail(),
                customer.getPhone(),
                """
                [
                  { "id": %d, "line1": "Av. Uno 1", "city":"Madrid", "postalCode":"28001", "country":"España", "defaultAddress": true }
                ]
                """.formatted(a1.getId())
        );

        mockMvc.perform(authPut("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Ana G. Edit"));

        addressRepository.flush();
        List<Address> all = addressRepository.findByCustomerId(customer.getId());
        assertThat(all.stream().filter(Address::getDefaultAddress).count()).isEqualTo(1);
        assertThat(all.stream().filter(Address::getDefaultAddress).findFirst().orElseThrow().getId())
            .isEqualTo(a1.getId());
    }

    // ========== 5) Endpoint dedicado marca solo una por defecto ==========
    @Test
    void setDefaultAddress_endpoint_correctlyMarksOnlyOneAsDefault() throws Exception {
        Address a1 = createAddress("Calle A", "Madrid", "28001", "España", true);
        Address a2 = createAddress("Calle B", "Madrid", "28002", "España", false);

        mockMvc.perform(authPut("/api/customers/{id}/addresses/{addressId}/default",
                customer.getId(), a2.getId()))
            .andExpect(status().isNoContent());

        addressRepository.flush();

        var r1 = addressRepository.findById(a1.getId()).orElseThrow();
        var r2 = addressRepository.findById(a2.getId()).orElseThrow();

        assertThat(r1.getDefaultAddress()).isFalse();
        assertThat(r2.getDefaultAddress()).isTrue();
    }
}
