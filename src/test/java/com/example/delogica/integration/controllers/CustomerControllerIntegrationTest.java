package com.example.delogica.integration.controllers;

import com.example.delogica.ApiCommerceApplication;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ApiCommerceApplication.class)
@ActiveProfiles("testing")
@AutoConfigureMockMvc
@Transactional
public class CustomerControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AddressRepository addressRepository;

    private String uniqueSuffix;
    private Customer existingCustomer;

    // -----------------------------
    // Helpers
    // -----------------------------
    static String customerJson(String name, String email, String phone) {
        return """
            {
              "fullName": "%s",
              "email": "%s",
              "phone": "%s",
              "addresses": []
            }
        """.formatted(name, email, phone);
    }

    static String customerJsonWithAddressesNull(String name, String email, String phone) {
        return """
            {
              "fullName": "%s",
              "email": "%s",
              "phone": "%s",
              "addresses": null
            }
        """.formatted(name, email, phone);
    }

    /** addressJson con defaultAddress nullable para poder enviar null/false sin violar la regla */
    static String addressJson(String line1, String city, String postal, String country, Boolean defaultAddress) {
        String def = defaultAddress == null ? "null" : String.valueOf(defaultAddress);
        return """
            {
              "line1": "%s",
              "line2": null,
              "city": "%s",
              "postalCode": "%s",
              "country": "%s",
              "defaultAddress": %s
            }
        """.formatted(line1, city, postal, country, def);
    }

    static String longEmail(int localPartLen) {
        String local = "a".repeat(localPartLen);
        return local + "@t.es";
    }

    /** Crea y persiste una Address ligada a existingCustomer */
    private Address createAddress(String line1, String city, String postal, String country, boolean isDefault) {
        Address a = new Address();
        a.setCustomer(existingCustomer);
        a.setLine1(line1);
        a.setCity(city);
        a.setPostalCode(postal);
        a.setCountry(country);
        a.setDefaultAddress(isDefault);
        return addressRepository.saveAndFlush(a);
    }

    @BeforeEach
    void setup() {
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        Customer c = new Customer();
        c.setFullName("Cliente Base");
        c.setEmail(("cliente.base." + uniqueSuffix + "@t.es"));
        c.setPhone("600000000");
        existingCustomer = customerRepository.saveAndFlush(c);
    }

    // ---------------------------------------------------------------------
    // CREATE CUSTOMER
    // ---------------------------------------------------------------------
    @Test
    void createCustomer_returns201_andBody() throws Exception {
        String payload = customerJson("Juan Test " + uniqueSuffix,
                "juan." + uniqueSuffix + "@t.es",
                "600111222");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.fullName").value("Juan Test " + uniqueSuffix))
            .andExpect(jsonPath("$.email").value("juan." + uniqueSuffix + "@t.es"));
    }

    @Test
    void createCustomer_missingEmail_returns400() throws Exception {
        String payloadSinEmail = """
            {
              "fullName": "Sin Email",
              "phone": "600000001",
              "addresses": []
            }
        """;

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadSinEmail))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_duplicateEmail_returns409_or400() throws Exception {
        String payloadDuplicado = customerJson("Otro",
                existingCustomer.getEmail(),
                "600111222");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadDuplicado))
            .andExpect(result -> {
                int sc = result.getResponse().getStatus();
                assertThat(sc == 409 || sc == 400).as("status debe ser 409 o 400").isTrue();
            });
    }

    @Test
    void createCustomer_phone8Digits_returns400() throws Exception {
        String payload = customerJson("Tel Corto " + uniqueSuffix,
                "tc" + uniqueSuffix + "@t.es",
                "60000000");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_phone10Digits_returns400() throws Exception {
        String payload = customerJson("Tel Largo " + uniqueSuffix,
                "tl" + uniqueSuffix + "@t.es",
                "6000000000");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_emailTooLong_returns400() throws Exception {
        String longMail = longEmail(31);
        String payload = customerJson("Email Largo " + uniqueSuffix,
                longMail,
                "600123456");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_addressesNull_returns400() throws Exception {
        String payload = customerJsonWithAddressesNull("Addr Null " + uniqueSuffix,
                "addrnull" + uniqueSuffix + "@t.es",
                "600234567");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------
    // LIST CUSTOMERS (PAGE + FILTER)
    // ---------------------------------------------------------------------
    @Test
    void listCustomers_withoutFilter_returnsPage200() throws Exception {
        mockMvc.perform(get("/api/customers")
                .param("page", "0").param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void listCustomers_withEmailFilter_returnsFilteredPage() throws Exception {
        mockMvc.perform(get("/api/customers")
                .param("email", existingCustomer.getEmail())
                .param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].email").value(existingCustomer.getEmail()));
    }

    @Test
    void listCustomers_pagination_page1_size1_returnsOneElement() throws Exception {
        String p1 = customerJson("Pag Uno " + uniqueSuffix, "pag1." + uniqueSuffix + "@t.es", "601000001");
        String p2 = customerJson("Pag Dos " + uniqueSuffix, "pag2." + uniqueSuffix + "@t.es", "601000002");

        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(p1))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(p2))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/customers")
                .param("page", "1").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.number").value(1));
    }

    // ---------------------------------------------------------------------
    // GET CUSTOMER BY ID
    // ---------------------------------------------------------------------
    @Test
    void getCustomer_existing_returns200_andBody() throws Exception {
        mockMvc.perform(get("/api/customers/" + existingCustomer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingCustomer.getId()))
            .andExpect(jsonPath("$.email").value(existingCustomer.getEmail()));
    }

    @Test
    void getCustomer_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/customers/99999999"))
            .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------
    // UPDATE CUSTOMER
    // ---------------------------------------------------------------------
    @Test
    void updateCustomer_existing_returns200_andUpdatedBody() throws Exception {
        String nuevoEmail = "upd." + uniqueSuffix + "@t.es";
        String payload = customerJson("Cliente Base Editado", nuevoEmail, "600222333");

        mockMvc.perform(put("/api/customers/" + existingCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Cliente Base Editado"))
            .andExpect(jsonPath("$.email").value(nuevoEmail));
    }

    @Test
    void updateCustomer_notFound_returns404() throws Exception {
        String payload = customerJson("No Existe", "no." + uniqueSuffix + "@t.es", "600222333");

        mockMvc.perform(put("/api/customers/99999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------
    // DELETE CUSTOMER
    // ---------------------------------------------------------------------
    @Test
    void deleteCustomer_existing_returns204_andRemovesIt() throws Exception {
        mockMvc.perform(delete("/api/customers/" + existingCustomer.getId()))
            .andExpect(status().isNoContent());

        assertThat(customerRepository.findById(existingCustomer.getId())).isEmpty();
    }

    // ---------------------------------------------------------------------
    // CREATE ADDRESS for CUSTOMER
    // ---------------------------------------------------------------------
    @Test
    void createAddress_forCustomer_returns201_andBody() throws Exception {
        // No marcamos default; la primera se marcará automáticamente en servicio
        String payload = addressJson("Calle 123", "Madrid", "28001", "España", null);

        customerRepository.flush();

        mockMvc.perform(post("/api/customers/{id}/addresses", existingCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.defaultAddress").value(true));
    }

    @Test
    void createAddress_missingRequiredField_returns400() throws Exception {
        // Falta postalCode (NOT NULL)
        String payload = """
            {
              "line1": "Calle Test 123",
              "city": "Madrid",
              "country": "España",
              "defaultAddress": false
            }
        """;

        mockMvc.perform(post("/api/customers/" + existingCustomer.getId() + "/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------
    // MARK ADDRESS AS DEFAULT
    // ---------------------------------------------------------------------
    @Test
    void markAddressAsDefault_setsFlag_and_unsetsPreviousDefault() throws Exception {
        Address a1 = createAddress("Calle A", "Madrid", "28001", "España", true);
        Address a2 = createAddress("Calle B", "Madrid", "28002", "España", false);

        addressRepository.flush();
        customerRepository.flush();

        mockMvc.perform(put("/api/customers/{id}/addresses/{addressId}/default",
                existingCustomer.getId(), a2.getId()))
            .andExpect(status().isNoContent());

        addressRepository.flush();

        var r1 = addressRepository.findById(a1.getId()).orElseThrow();
        var r2 = addressRepository.findById(a2.getId()).orElseThrow();

        assertThat(r1.getDefaultAddress()).isFalse();
        assertThat(r2.getDefaultAddress()).isTrue();
    }
}
