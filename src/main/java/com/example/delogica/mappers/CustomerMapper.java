package com.example.delogica.mappers;

import org.mapstruct.*;
import com.example.delogica.models.Customer;
import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerInputDTO dto);

    CustomerOutputDTO toOutput(Customer entity);

    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CustomerInputDTO dto, @MappingTarget Customer entity);
}
