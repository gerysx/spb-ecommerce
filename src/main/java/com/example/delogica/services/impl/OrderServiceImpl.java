package com.example.delogica.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.delogica.config.exceptions.ResourceNotFoundException;
import com.example.delogica.config.specifications.OrderSpecifications;
import com.example.delogica.dtos.input.OrderCreateInputDTO;
import com.example.delogica.dtos.input.OrderItemInputDTO;
import com.example.delogica.dtos.output.OrderOutputDTO;
import com.example.delogica.dtos.output.OrderSimpleOutputDTO;
import com.example.delogica.mappers.OrderMapper;
import com.example.delogica.models.Address;
import com.example.delogica.models.Customer;
import com.example.delogica.models.Order;
import com.example.delogica.models.OrderItem;
import com.example.delogica.models.OrderStatus;
import com.example.delogica.models.Product;
import com.example.delogica.repositories.AddressRepository;
import com.example.delogica.repositories.CustomerRepository;
import com.example.delogica.repositories.OrderRepository;
import com.example.delogica.repositories.ProductRepository;
import com.example.delogica.services.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;

    @Transactional
    @Override
    public OrderSimpleOutputDTO create(OrderCreateInputDTO input) {
        logger.info("Creando pedido para clienteId={}, shippingAddressId={}, itemsCount={}",
                input.getCustomerId(), input.getShippingAddressId(),
                input.getItems() == null ? 0 : input.getItems().size());

        // Validar cliente y dirección
        Customer customer = customerRepository.findById(input.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        Address shippingAddress = addressRepository.findById(input.getShippingAddressId())
                .orElseThrow(() -> ResourceNotFoundException.forId(Address.class, input.getShippingAddressId()));

        if (!Objects.equals(
                shippingAddress.getCustomer() != null ? shippingAddress.getCustomer().getId() : null,
                customer != null ? customer.getId() : null)) {
            throw new IllegalArgumentException("La dirección no pertenece al cliente especificado");
        }

        // Validar items no vacíos
        if (input.getItems() == null || input.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(shippingAddress);
        order.setOrderDate(LocalDateTime.now()); // Fecha actual
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemInputDTO itemDTO : input.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> ResourceNotFoundException.forId(Product.class, itemDTO.getProductId()));

            if (!product.isActive()) {
                throw new IllegalArgumentException("Producto inactivo: " + product.getName());
            }

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para producto: " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(unitPrice);

            orderItems.add(orderItem);

            total = total.add(unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        order.setItems(orderItems);
        order.setTotal(total);

        // Guardar pedido (items se guardan en cascada)
        orderRepository.save(order);

        // Actualizar stock productos
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // Mapear y devolver DTO salida
        return orderMapper.toSimpleOutput(order);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderOutputDTO> search(Pageable pageable, Long customerId, LocalDateTime fromDate, LocalDateTime toDate,
            OrderStatus status) {
        logger.info("Buscando pedidos con filtros: customerId={}, fromDate={}, toDate={}, status={}",
                customerId, fromDate, toDate, status);

        Specification<Order> spec = null;

        if (customerId != null) {
            spec = (spec == null)
                    ? OrderSpecifications.hasCustomerId(customerId)
                    : spec.and(OrderSpecifications.hasCustomerId(customerId));
        }

        if (fromDate != null) {
            spec = (spec == null)
                    ? OrderSpecifications.fromDate(fromDate)
                    : spec.and(OrderSpecifications.fromDate(fromDate));
        }

        if (toDate != null) {
            spec = (spec == null)
                    ? OrderSpecifications.toDate(toDate)
                    : spec.and(OrderSpecifications.toDate(toDate));
        }

        if (status != null) {
            spec = (spec == null)
                    ? OrderSpecifications.hasStatus(status)
                    : spec.and(OrderSpecifications.hasStatus(status));
        }

        Page<Order> page = orderRepository.findAll(spec, pageable);

        logger.info("Pedidos encontrados: {}", page.getTotalElements());

        return page.map(orderMapper::toOutput);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderOutputDTO getById(Long id) {
        logger.info("Obteniendo pedido por ID: {}", id);

        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId(Order.class, id));

        logger.info("Pedido encontrado: ID {}", id);

        return orderMapper.toOutput(order);
    }

    @Override
    @Transactional
    public OrderOutputDTO changeStatus(Long id, OrderStatus newStatus) {
        logger.info("Cambiando estado del pedido ID {} a {}", id, newStatus);

        Order order = orderRepository.findByIdWithLock(id)
                .orElseThrow(() -> ResourceNotFoundException.forId(Order.class, id));

        OrderStatus currentStatus = order.getStatus();

        logger.debug("Estado actual: {}", currentStatus);

        boolean validTransition = false;

        switch (currentStatus) {
            case CREATED:
                if (newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED)
                    validTransition = true;
                break;
            case PAID:
                if (newStatus == OrderStatus.SHIPPED)
                    validTransition = true;
                else if (newStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.SHIPPED)
                    validTransition = true;
                break;
            case SHIPPED:
            case CANCELLED:
                validTransition = false;
                break;
        }

        if (!validTransition) {
            logger.warn("Intento de transición inválida: {} -> {}", currentStatus, newStatus);
            throw new IllegalStateException("Transición de estado inválida: " + currentStatus + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        logger.info("Estado del pedido ID {} cambiado exitosamente a {}", id, newStatus);

        return orderMapper.toOutput(order);
    }
}
