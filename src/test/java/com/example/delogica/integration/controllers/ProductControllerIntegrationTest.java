package com.example.delogica.integration.controllers;

import com.example.delogica.integration.common.AbstractIntegrationTest;
import com.example.delogica.models.Product;
import com.example.delogica.repositories.ProductRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ProductController con autenticación JWT simulada.
 */
@ActiveProfiles("testing")
@Transactional
class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product activeProduct;
    @SuppressWarnings("unused")
    private Product inactiveProduct;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();

        Product p1 = new Product();
        p1.setName("Camiseta Azul");
        p1.setSku("SKU-001");
        p1.setDescription("Algodón 100%");
        p1.setPrice(new BigDecimal("19.99"));
        p1.setStock(50);
        p1.setActive(true);
        activeProduct = productRepository.saveAndFlush(p1);

        Product p2 = new Product();
        p2.setName("Pantalón Negro");
        p2.setSku("SKU-002");
        p2.setDescription("Slim fit");
        p2.setPrice(new BigDecimal("39.90"));
        p2.setStock(20);
        p2.setActive(false);
        inactiveProduct = productRepository.saveAndFlush(p2);
    }

    // ---------- helpers ----------
    private String productJson(String name, String sku, String desc,
                               String price, Integer stock, Boolean active) {
        return """
                {
                  "name": "%s",
                  "sku": "%s",
                  "description": "%s",
                  "price": %s,
                  "stock": %d,
                  "active": %s
                }
                """.formatted(name, sku, desc, price, stock, active);
    }

    // ---------- tests ----------

    @Test
    void createProduct_returns201_andBody_andLocation() throws Exception {
        mockMvc.perform(authPost("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Zapato Marrón", "SKU-NEW", "Cuero", "59.95", 15, true)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", Matchers.containsString("/api/products/")))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.sku").value("SKU-NEW"))
            .andExpect(jsonPath("$.price").value(59.95));
    }

    @Test
    void createProduct_missingSku_returns400() throws Exception {
        String body = """
            {
              "name": "Sin SKU",
              "sku": "",
              "description": "X",
              "price": 9.99,
              "stock": 1,
              "active": true
            }
        """;

        mockMvc.perform(authPost("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void createProduct_duplicateSku_returns409() throws Exception {
        mockMvc.perform(authPost("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Otro nombre", "SKU-001", "desc", "10.00", 5, true)))
            .andExpect(status().isConflict());
    }

    @Test
    void detail_existing_returns200_andBody() throws Exception {
        mockMvc.perform(authGet("/api/products/{id}", activeProduct.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(activeProduct.getId()))
            .andExpect(jsonPath("$.name").value("Camiseta Azul"));
    }

    @Test
    void detail_notFound_returns404() throws Exception {
        mockMvc.perform(authGet("/api/products/{id}", 999999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void list_withoutFilter_returns200_andPage() throws Exception {
        mockMvc.perform(authGet("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void list_withNameFilter_returnsFilteredPage() throws Exception {
        mockMvc.perform(authGet("/api/products").param("name", "Camiseta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name", Matchers.containsString("Camiseta")));
    }

    @Test
    void list_withActiveFilter_onlyActive() throws Exception {
        mockMvc.perform(authGet("/api/products").param("active", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].active",
                    Matchers.everyItem(Matchers.equalTo(true))));
    }

    @Test
    void update_existing_returns200_andUpdatedBody() throws Exception {
        mockMvc.perform(authPut("/api/products/{id}", activeProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Camiseta Actualizada", "SKU-001", "Nueva desc", "21.99", 60, true)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Camiseta Actualizada"))
            .andExpect(jsonPath("$.price").value(21.99));
    }

    @Test
    void update_whenChangingSkuToExisting_returns409_or400() throws Exception {
        mockMvc.perform(authPut("/api/products/{id}", activeProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Nombre", "SKU-002", "desc", "29.99", 10, true)))
            .andExpect(status().isConflict()); // o BadRequest si así lo manejas
    }

    @Test
    void update_notFound_returns404() throws Exception {
        mockMvc.perform(authPut("/api/products/{id}", 987654321L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("X", "SKU-X", "Y", "9.99", 1, true)))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returns204_andSetsInactive() throws Exception {
        mockMvc.perform(authDelete("/api/products/{id}", activeProduct.getId()))
            .andExpect(status().isNoContent());

        var reloaded = productRepository.findById(activeProduct.getId()).orElseThrow();
        assertThat(reloaded.isActive()).isFalse();
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        mockMvc.perform(authDelete("/api/products/{id}", 999999L))
            .andExpect(status().isNotFound());
    }
}
