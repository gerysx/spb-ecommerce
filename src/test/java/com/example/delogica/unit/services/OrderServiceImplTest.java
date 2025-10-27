package com.example.delogica.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.input.OrderItemInputDTO;
import com.example.delogica.dtos.input.OrderStatusInputDTO;
import com.example.delogica.dtos.output.OrderOutputDTO;
import com.example.delogica.dtos.output.OrderSimpleOutputDTO;
import com.example.delogica.mappers.OrderMapper;
import com.example.delogica.models.*;
import com.example.delogica.repositories.*;
import com.example.delogica.services.impl.OrderServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- TEST CREATE() -----------
    @Test
    public void create_validOrder_createsOrderSuccessfully() {
        // Arrange
        Long customerId = 1L;
        Long addressId = 2L;

        Customer customer = new Customer();
        customer.setId(customerId);

        Address address = new Address();
        address.setId(addressId);
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setActive(true);
        product.setStock(5);
        product.setPrice(new BigDecimal("10.0"));

        OrderItemInputDTO itemInput = new OrderItemInputDTO();
        itemInput.setProductId(product.getId());
        itemInput.setQuantity(2);

        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customerId);
        input.setShippingAddressId(addressId);
        input.setItems(Collections.singletonList(itemInput));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        // Mapper output DTO mock
        OrderSimpleOutputDTO expectedOutput = new OrderSimpleOutputDTO();
        when(orderMapper.toSimpleOutput(any(Order.class))).thenReturn(expectedOutput);

        // Act
        OrderSimpleOutputDTO result = orderService.create(input);

        // Assert
        assertNotNull(result);
        assertEquals(expectedOutput, result);

        verify(orderRepository).save(any(Order.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void create_customerNotFound_throwsException() {
        Long customerId = 1L;
        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.create(input));
        assertEquals("Cliente no encontrado", ex.getMessage());
    }

    @Test
    public void create_shippingAddressNotFound_throwsException() {
        Long customerId = 1L;
        Long addressId = 2L;

        Customer customer = new Customer();
        customer.setId(customerId);

        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customerId);
        input.setShippingAddressId(addressId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(addressId))
                .thenThrow(ResourceNotFoundException.forId(Address.class, addressId));

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(input));
    }

    @Test
    public void create_addressDoesNotBelongToCustomer_throwsException() {
        Long customerId = 1L;
        Long addressId = 2L;

        Customer customer = new Customer();
        customer.setId(customerId);

        Address address = new Address();
        address.setId(addressId);
        address.setCustomer(new Customer()); // otro cliente distinto

        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customerId);
        input.setShippingAddressId(addressId);
        input.setItems(Collections.singletonList(new OrderItemInputDTO()));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.create(input));
        assertEquals("La dirección no pertenece al cliente especificado", ex.getMessage());
    }

    @Test
    public void create_emptyItems_throwsException() {
        Long customerId = 1L;
        Long addressId = 2L;

        Customer customer = new Customer();
        customer.setId(customerId);
        Address address = new Address();
        address.setId(addressId);
        address.setCustomer(customer);

        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customerId);
        input.setShippingAddressId(addressId);
        input.setItems(Collections.emptyList());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.create(input));
        assertEquals("El pedido debe contener al menos un producto", ex.getMessage());
    }

    @Test
    public void create_productInactive_throwsException() {
        Long customerId = 1L;
        Long addressId = 2L;

        Customer customer = new Customer();
        customer.setId(customerId);
        Address address = new Address();
        address.setId(addressId);
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(10L);
        product.setActive(false);

        OrderItemInputDTO itemInput = new OrderItemInputDTO();
        itemInput.setProductId(product.getId());
        itemInput.setQuantity(1);

        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customerId);
        input.setShippingAddressId(addressId);
        input.setItems(Collections.singletonList(itemInput));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.create(input));
        assertTrue(ex.getMessage().contains("Producto inactivo"));
    }

    @Test
    public void create_insufficientStock_throwsException() {
        Long customerId = 1L;
        Long addressId = 2L;

        Customer customer = new Customer();
        customer.setId(customerId);
        Address address = new Address();
        address.setId(addressId);
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(10L);
        product.setActive(true);
        product.setStock(1);

        OrderItemInputDTO itemInput = new OrderItemInputDTO();
        itemInput.setProductId(product.getId());
        itemInput.setQuantity(5);

        OrderCreateInputDTO input = new OrderCreateInputDTO();
        input.setCustomerId(customerId);
        input.setShippingAddressId(addressId);
        input.setItems(Collections.singletonList(itemInput));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.create(input));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    // ------------- TEST SEARCH() ---------------
    @SuppressWarnings("unchecked")
    @Test
    public void search_withFilters_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Long customerId = 1L;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(10);
        LocalDateTime toDate = LocalDateTime.now();
        OrderStatus status = OrderStatus.CREATED;

        List<Order> orders = new ArrayList<>();
        orders.add(new Order());

        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);

        OrderOutputDTO outputDTO = new OrderOutputDTO();
        when(orderMapper.toOutput(any(Order.class))).thenReturn(outputDTO);

        Page<OrderOutputDTO> result = orderService.search(pageable, customerId, fromDate, toDate, status);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(outputDTO, result.getContent().get(0));

        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------- TEST getById() ----------------
    @Test
    public void getById_existingOrder_returnsDTO() {
        Long orderId = 1L;
        Order order = new Order();

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(order));
        OrderOutputDTO outputDTO = new OrderOutputDTO();
        when(orderMapper.toOutput(order)).thenReturn(outputDTO);

        OrderOutputDTO result = orderService.getById(orderId);

        assertEquals(outputDTO, result);
    }

    @Test
    public void getById_notFound_throwsException() {
        Long orderId = 1L;

        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getById(orderId));
    }

    // ------------ TEST changeStatus() --------------
   @Test
public void changeStatus_validTransition_changesStatus() {
    Long orderId = 1L;
    Order order = new Order();
    order.setStatus(OrderStatus.CREATED);

    when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(order);

    OrderOutputDTO outputDTO = new OrderOutputDTO();
    when(orderMapper.toOutput(order)).thenReturn(outputDTO);

    // <-- Creamos el DTO en lugar de pasar el enum directamente
    OrderStatusInputDTO inputDTO = new OrderStatusInputDTO();
    inputDTO.setStatus(OrderStatus.PAID.name());

    OrderOutputDTO result = orderService.changeStatus(orderId, inputDTO);

    assertEquals(outputDTO, result);
    assertEquals(OrderStatus.PAID, order.getStatus());
}


   @Test
public void changeStatus_invalidTransition_throwsException() {
    Long orderId = 1L;
    Order order = new Order();
    order.setStatus(OrderStatus.SHIPPED);

    when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.of(order));

    OrderStatusInputDTO inputDTO = new OrderStatusInputDTO();
    inputDTO.setStatus(OrderStatus.CREATED.name());

    IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> orderService.changeStatus(orderId, inputDTO));
    assertTrue(ex.getMessage().contains("Transición de estado inválida"));
}


    @Test
public void changeStatus_orderNotFound_throwsException() {
    Long orderId = 1L;

    when(orderRepository.findByIdWithLock(orderId)).thenReturn(Optional.empty());

    OrderStatusInputDTO inputDTO = new OrderStatusInputDTO();
    inputDTO.setStatus(OrderStatus.PAID.name());

    assertThrows(ResourceNotFoundException.class,
            () -> orderService.changeStatus(orderId, inputDTO));
}

}
