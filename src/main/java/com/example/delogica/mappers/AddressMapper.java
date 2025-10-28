package com.example.delogica.mappers;

import org.mapstruct.*;
import com.example.delogica.models.Address;
import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AddressMapper {

    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "defaultAddress", ignore = true)
    Address toEntity(AddressInputDTO dto);

    AddressOutputDTO toOutput(Address entity);

   
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "defaultAddress", ignore = true)
    void updateEntityFromDto(AddressInputDTO dto, @MappingTarget Address entity);
}
