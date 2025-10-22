// AddressMapper.java
package com.example.delogica.mappers;

import org.mapstruct.*;
import com.example.delogica.models.Address;
import com.example.delogica.models.Customer;
import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    // Usado desde AddressService (con customerId)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "defaultAddress", source = "defaultAddress")
    Address toEntity(AddressInputDTO dto);

    AddressOutputDTO toOutput(Address entity);


    @Mapping(target = "id", ignore = true) // o no, depende si quieres actualizar id
    @Mapping(target = "customer", ignore = true)
    void updateEntityFromDto(AddressInputDTO dto, @MappingTarget Address entity);

    @AfterMapping
    default void attachCustomer(@MappingTarget Address address, @Context Customer customer) {
        if (customer != null) {
            address.setCustomer(customer);
        }
    }

}
