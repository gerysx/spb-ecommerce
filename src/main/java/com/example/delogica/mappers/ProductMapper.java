// src/main/java/com/example/delogica/mappers/ProductMapper.java
package com.example.delogica.mappers;

import org.mapstruct.*;
import com.example.delogica.models.*;
import com.example.delogica.dtos.input.*;
import com.example.delogica.dtos.output.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    // INPUT → ENTITY
    @Mapping(target = "id", ignore = true)
    // 'active' se establece en @PrePersist en el modelo
    @Mapping(target = "active", ignore = true)
    // createdAt / updatedAt gestionados por callbacks JPA
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductInputDTO dto);

    // ENTITY → OUTPUT
    ProductOutputDTO toOutput(Product entity);

     @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", source = "active")
    void updateEntityFromDto(ProductInputDTO dto, @MappingTarget Product entity);
}
