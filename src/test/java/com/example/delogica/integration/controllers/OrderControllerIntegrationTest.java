package com.example.delogica.integration.controllers;

import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.input.OrderItemInputDTO;
import com.example.delogica.dtos.input.OrderStatusInputDTO;
import com.example.delogica.integration.common.AbstractIntegrationTest;
import com.example.delogica.models.*;
import com.example.delogica.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para OrderController.
 * Hereda de AbstractIntegrationTest para usar JWT real sin tocar la BD.
 */
@ActiveProfiles("testing")
@Transactional
public class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    private Customer customer;
    private Address address;
    private Product product;

    @BeforeEach
    void setup() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        // --- Customer ---
        customer = new Customer();
        customer.setFullName("Juan Test");
        customer.setEmail("juan.test+" + suffix + "@delogica.example");
        customer.setPhone("+34999999999");
        customer = customerRepository.save(customer);

        // --- Address ---
        address = new Address();
        address.setCustomer(customer);
        address.setLine1("Calle Falsa 123");
        address.setCity("Madrid");
        address.setPostalCode("28001");
        address.setCountry("España");
        address.setDefaultAddress(true);
        address = addressRepository.save(address);

        // --- Product ---
        product = new Product();
        product.setSku("SKU-" + suffix);
        product.setName("Producto Test");
        product.setDescription("Producto para IT");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setActive(true);
        product = productRepository.save(product);
    }

    // ------------------------------
    // CREATE ORDER
    // ------------------------------
    @Test
    void createOrder_returnsCreatedOrder() throws Exception {
        OrderItemInputDTO item = new OrderItemInputDTO();
        item.setProductId(product.getId());
        item.setQuantity(2);

        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customer.getId());
        input.setShippingAddressId(address.getId());
        input.setItems(List.of(item));

        mockMvc.perform(authPost("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.total").value(200));
    }

    // ------------------------------
    // GET ORDER BY ID
    // ------------------------------
    @Test
    void getOrderById_returnsOrder() throws Exception {
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(BigDecimal.valueOf(300));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        item.setUnitPrice(product.getPrice());

        order.setItems(List.of(item));
        order = orderRepository.save(order);

        mockMvc.perform(authGet("/api/orders/{id}", order.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(order.getId()))
            .andExpect(jsonPath("$.total").value(300));
    }

    // ------------------------------
    // CHANGE STATUS - VALID
    // ------------------------------
    @Test
    void changeStatus_validTransition() throws Exception {
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(BigDecimal.valueOf(100));
        order = orderRepository.save(order);

        OrderStatusInputDTO statusDTO = new OrderStatusInputDTO();
        statusDTO.setStatus("PAID");

        mockMvc.perform(authPut("/api/orders/{id}/status", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    // ------------------------------
    // CHANGE STATUS - INVALID
    // ------------------------------
    @Test
    void changeStatus_invalidTransition_returnsBadRequest() throws Exception {
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.SHIPPED); // Estado final
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(BigDecimal.valueOf(100));
        order = orderRepository.save(order);

        OrderStatusInputDTO statusDTO = new OrderStatusInputDTO();
        statusDTO.setStatus("CREATED"); // Retroceso no permitido

        mockMvc.perform(authPut("/api/orders/{id}/status", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    // ------------------------------
    // SEARCH ORDERS
    // ------------------------------
    @Test
    void searchOrders_withFilters_returnsOrders() throws Exception {
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(BigDecimal.valueOf(100));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPrice(product.getPrice());

        order.setItems(List.of(item));
        order = orderRepository.save(order);

        mockMvc.perform(authGet("/api/orders")
                .param("customerId", customer.getId().toString())
                .param("status", "CREATED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(order.getId()));
    }
}
