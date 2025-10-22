package com.example.delogica.integration;

import com.example.delogica.ApiCommerceApplication;
import com.example.delogica.config.errors.ErrorCode;
import com.example.delogica.models.Address;
import com.example.delogica.models.Customer;
import com.example.delogica.repositories.AddressRepository;
import com.example.delogica.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ApiCommerceApplication.class)
@ActiveProfiles("testing")
@AutoConfigureMockMvc
@Transactional
class CustomerDefaultAddressIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AddressRepository addressRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setFullName("Ana García");
        customer.setEmail("ana.garcia+" + System.nanoTime() + "@example.com");
        customer.setPhone("654987321"); // 9 dígitos
        customer = customerRepository.save(customer);
    }

    // ---------- helpers ----------
    private String addressJson(String line1, String city, String postal, String country, Boolean isDefault) {
        return """
        {
          "line1": "%s",
          "line2": null,
          "city": "%s",
          "postalCode": "%s",
          "country": "%s",
          "defaultAddress": %s
        }
        """.formatted(line1, city, postal, country, String.valueOf(isDefault));
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

    // ========== 1) Primera dirección creada → queda default ==========
    @Test
    void createAddress_firstBecomesDefault() throws Exception {
        String payload = addressJson("Calle Sol 45", "Sevilla", "41001", "España", true);

        mockMvc.perform(post("/api/customers/{id}/addresses", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.defaultAddress").value(true));

        // Verifica en BD
        var addrs = addressRepository.findByCustomerId(customer.getId());
        assertThat(addrs).hasSize(1);
        assertThat(addrs.get(0).getDefaultAddress()).isTrue();
    }

    // ========== 2) Segunda dirección con default=true → sustituye a la anterior ==========
    @Test
    void createSecondAddress_withDefaultTrue_clearsPrevious() throws Exception {
        // 1a dirección (queda default)
        var a1 = new Address();
        a1.setCustomer(customer);
        a1.setLine1("Calle Sol 45");
        a1.setCity("Sevilla");
        a1.setPostalCode("41001");
        a1.setCountry("España");
        a1.setDefaultAddress(true);
        a1 = addressRepository.save(a1);

        // 2a dirección: pido que sea default
        String payload2 = addressJson("Plaza Mayor 10", "Granada", "18001", "España", true);

        mockMvc.perform(post("/api/customers/{id}/addresses", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload2))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.defaultAddress").value(true));

        // En BD: la nueva es default y la anterior ya no
        var r1 = addressRepository.findById(a1.getId()).orElseThrow();
        var all = addressRepository.findByCustomerId(customer.getId());
        assertThat(all.stream().filter(Address::getDefaultAddress)).hasSize(1);
        assertThat(r1.getDefaultAddress()).isFalse();
    }

    // ========== 3) Intentar cambiar default vía PUT /api/customers/{id} → 400 BAD_REQUEST ==========
    @Test
    void updateCustomer_tryFlipDefaultViaDTO_returns400() throws Exception {
        // Creamos dos direcciones, la primera default
        Address a1 = new Address();
        a1.setCustomer(customer);
        a1.setLine1("Av. Uno 1");
        a1.setCity("Madrid");
        a1.setPostalCode("28001");
        a1.setCountry("España");
        a1.setDefaultAddress(true);
        a1 = addressRepository.save(a1);

        Address a2 = new Address();
        a2.setCustomer(customer);
        a2.setLine1("Av. Dos 2");
        a2.setCity("Madrid");
        a2.setPostalCode("28002");
        a2.setCountry("España");
        a2.setDefaultAddress(false);
        a2 = addressRepository.save(a2);

        // Intento “cambiar” la default via DTO: marco a2 como default=true
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

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.status").value(400));
    }

    // ========== 4) Update sin tocar default (sólo datos básicos) → OK ==========
    @Test
    void updateCustomer_withoutTouchingDefault_ok() throws Exception {
        Address a1 = new Address();
        a1.setCustomer(customer);
        a1.setLine1("Av. Uno 1");
        a1.setCity("Madrid");
        a1.setPostalCode("28001");
        a1.setCountry("España");
        a1.setDefaultAddress(true);
        addressRepository.save(a1);

        String body = updateCustomerJson(
                "Ana G. Edit",
                customer.getEmail(),
                customer.getPhone(),
                // NO tocamos defaultAddress (mantenemos igual el valor enviado)
                """
                [
                  { "id": %d, "line1": "Av. Uno 1", "city":"Madrid", "postalCode":"28001", "country":"España", "defaultAddress": true }
                ]
                """.formatted(a1.getId())
        );

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Ana G. Edit"));
    }

    // ========== 5) Endpoint dedicado para marcar default → deja exactamente una ==========
    @Test
    void markAddressAsDefault_endpoint_setsExactlyOneDefault() throws Exception {
        Address a1 = new Address();
        a1.setCustomer(customer);
        a1.setLine1("Calle A");
        a1.setCity("Madrid");
        a1.setPostalCode("28001");
        a1.setCountry("España");
        a1.setDefaultAddress(true);
        a1 = addressRepository.save(a1);

        Address a2 = new Address();
        a2.setCustomer(customer);
        a2.setLine1("Calle B");
        a2.setCity("Madrid");
        a2.setPostalCode("28002");
        a2.setCountry("España");
        a2.setDefaultAddress(false);
        a2 = addressRepository.save(a2);

        mockMvc.perform(put("/api/customers/{id}/addresses/{addressId}/default",
                        customer.getId(), a2.getId()))
            .andExpect(status().isNoContent());

        var r1 = addressRepository.findById(a1.getId()).orElseThrow();
        var r2 = addressRepository.findById(a2.getId()).orElseThrow();

        assertThat(r1.getDefaultAddress()).isFalse();
        assertThat(r2.getDefaultAddress()).isTrue();
    }
}
