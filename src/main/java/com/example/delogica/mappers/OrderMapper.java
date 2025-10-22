// src/main/java/com/example/delogica/mappers/OrderMapper.java
package com.example.delogica.mappers;

import org.mapstruct.*;
import com.example.delogica.models.*;
import com.example.delogica.dtos.input.*;
import com.example.delogica.dtos.output.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class, AddressMapper.class,
        CustomerMapper.class }, unmappedTargetPolicy = ReportingPolicy.ERROR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    // Convierte DTO de creación a entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", source = "customerId", qualifiedByName = "idToCustomer")
    @Mapping(target = "shippingAddress", source = "shippingAddressId", qualifiedByName = "idToAddress")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Order toEntity(OrderCreateInputDTO dto);

    // Convierte entidad a DTO de salida
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "shippingAddress", source = "shippingAddress")
    @Mapping(target = "items", source = "items")
    OrderOutputDTO toOutput(Order entity);

    List<OrderOutputDTO> toOutputList(List<Order> orders);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "shippingAddressId", source = "shippingAddress.id")
    @Mapping(target = "items", source = "items")
    OrderSimpleOutputDTO toSimpleOutput(Order order);

    // Métodos auxiliares para crear entidades con solo el id para relaciones
    @Named("idToCustomer")
    default Customer idToCustomer(Long id) {
        if (id == null)
            return null;
        Customer c = new Customer();
        c.setId(id);
        return c;
    }

    @Named("idToAddress")
    default Address idToAddress(Long id) {
        if (id == null)
            return null;
        Address a = new Address();
        a.setId(id);
        return a;
    }
}
