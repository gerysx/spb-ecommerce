// src/main/java/com/example/delogica/mappers/OrderItemMapper.java
package com.example.delogica.mappers;

import org.mapstruct.*;
import com.example.delogica.models.*;
import com.example.delogica.dtos.input.*;
import com.example.delogica.dtos.output.*;

@Mapper(componentModel = "spring", uses = {
        ProductMapper.class }, unmappedTargetPolicy = ReportingPolicy.ERROR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {

   
    // Regla de negocio: unitPrice se obtiene del Product en la capa de servicio
    // Por ello lo ignoramos en el mapeo de entrada (aunque exista en DTO heredado)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", source = "productId", qualifiedByName = "idToProduct")
    @Mapping(target = "unitPrice", ignore = true)
    OrderItem toEntity(OrderItemInputDTO dto);

    
    OrderItemOutputDTO toOutput(OrderItem entity);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "shippingAddressId", source = "shippingAddress.id")
    OrderSimpleOutputDTO toSimpleOutput(Order order);

    @Mapping(target = "productId", source = "product.id")
    OrderItemSimpleOutputDTO toSimpleOutput(OrderItem item);

    @Named("idToProduct")
    default Product idToProduct(Long id) {
        if (id == null)
            return null;
        Product p = new Product();
        p.setId(id);
        return p;
    }
}
