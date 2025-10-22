// CustomerMapper.java
package com.example.delogica.mappers;

import org.mapstruct.*;
import com.example.delogica.models.*;
import com.example.delogica.dtos.input.*;
import com.example.delogica.dtos.output.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {
        AddressMapper.class }, unmappedTargetPolicy = ReportingPolicy.ERROR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerInputDTO dto);

    CustomerOutputDTO toOutput(Customer entity);

    List<CustomerOutputDTO> toOutputList(List<Customer> customers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CustomerInputDTO dto, @MappingTarget Customer entity);

    @AfterMapping
    default void bindAddresses(@MappingTarget Customer customer) {
        if (customer.getAddresses() != null) {
            customer.getAddresses().forEach(a -> a.setCustomer(customer));
        }
    }
}
