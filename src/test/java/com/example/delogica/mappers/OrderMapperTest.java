package com.example.delogica.mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.input.OrderItemInputDTO;
import com.example.delogica.dtos.output.OrderItemSimpleOutputDTO;

import com.example.delogica.dtos.output.OrderSimpleOutputDTO;

import com.example.delogica.models.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class OrderMapperTest {

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private OrderMapperImpl orderMapper;

    @Test
    void testToEntity() {
        OrderCreateInputDTO dto = new OrderCreateInputDTO();
        dto.setCustomerId(1L);
        dto.setShippingAddressId(2L);

        OrderItemInputDTO itemDTO = new OrderItemInputDTO();
        itemDTO.setProductId(100L);
        itemDTO.setQuantity(2);

        dto.setItems(List.of(itemDTO));

        OrderItem orderItem = new OrderItem();
        when(orderItemMapper.toEntity(itemDTO)).thenReturn(orderItem);

        Order result = orderMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(1L, result.getCustomer().getId());
        assertEquals(2L, result.getShippingAddress().getId());
        assertEquals(1, result.getItems().size());
        assertSame(orderItem, result.getItems().get(0));

        // Total, orderDate y status se ignoran → null
        assertNull(result.getTotal());
        assertNull(result.getOrderDate());
        assertNull(result.getStatus());
    }

    @Test
    void testToSimpleOutput() {
        Customer customer = new Customer();
        customer.setId(1L);

        Address address = new Address();
        address.setId(2L);

        OrderItem item = new OrderItem();
        List<OrderItem> items = List.of(item);

        Order order = new Order();
        order.setId(10L);
        order.setCustomer(customer);
        order.setShippingAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);
        order.setItems(items);
        order.setTotal(BigDecimal.valueOf(99.99));

        OrderItemSimpleOutputDTO itemDTO = new OrderItemSimpleOutputDTO();
        when(orderItemMapper.toSimpleOutput(item)).thenReturn(itemDTO);

        OrderSimpleOutputDTO dto = orderMapper.toSimpleOutput(order);

        assertNotNull(dto);
        assertEquals(1L, dto.getCustomerId());
        assertEquals(2L, dto.getShippingAddressId());
        assertEquals(order.getOrderDate(), dto.getOrderDate());
        assertEquals(order.getStatus(), dto.getStatus());
        assertEquals(order.getTotal(), dto.getTotal());
        assertEquals(1, dto.getItems().size());
    }

    @Test
    void testIdToCustomer() {
        Customer customer = orderMapper.idToCustomer(42L);
        assertNotNull(customer);
        assertEquals(42L, customer.getId());
    }

    @Test
    void testIdToAddress() {
        Address address = orderMapper.idToAddress(7L);
        assertNotNull(address);
        assertEquals(7L, address.getId());
    }

}
