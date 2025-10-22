package com.example.delogica.mappers;



import static org.assertj.core.api.Assertions.assertThat;

import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;
import com.example.delogica.models.Address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class AddressMapperTest {

    private AddressMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AddressMapper.class);
    }

    @Test
    void testToEntity() {
        AddressInputDTO dto = new AddressInputDTO();
        dto.setLine1("Calle Falsa 123");
        dto.setCity("Springfield");
        dto.setPostalCode("12345");
        dto.setCountry("España");

        Address entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getLine1()).isEqualTo(dto.getLine1());
        assertThat(entity.getCity()).isEqualTo(dto.getCity());
        assertThat(entity.getPostalCode()).isEqualTo(dto.getPostalCode());
        assertThat(entity.getCountry()).isEqualTo(dto.getCountry());
        assertThat(entity.getId()).isNull(); // se ignora id
        assertThat(entity.getCustomer()).isNull(); // se ignora customer
        assertThat(entity.getDefaultAddress()).isNull(); // se ignora defaultAddress
    }

    @Test
    void testToOutput() {
        Address entity = new Address();
        entity.setId(1L);
        entity.setLine1("Calle Falsa 123");
        entity.setCity("Springfield");
        entity.setPostalCode("12345");
        entity.setCountry("España");

        AddressOutputDTO output = mapper.toOutput(entity);

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(entity.getId());
        assertThat(output.getLine1()).isEqualTo(entity.getLine1());
        assertThat(output.getCity()).isEqualTo(entity.getCity());
        assertThat(output.getPostalCode()).isEqualTo(entity.getPostalCode());
        assertThat(output.getCountry()).isEqualTo(entity.getCountry());
    }

    @Test
    void testUpdateEntityFromDto() {
        AddressInputDTO dto = new AddressInputDTO();
        dto.setLine1("Nueva Calle 456");
        dto.setCity("Shelbyville");
        dto.setPostalCode("54321");
        dto.setCountry("Francia");

        Address entity = new Address();
        entity.setId(1L);
        entity.setLine1("Calle Antigua");
        entity.setCity("Springfield");
        entity.setPostalCode("12345");
        entity.setCountry("España");

        mapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getId()).isEqualTo(1L); // no debe cambiar
        assertThat(entity.getLine1()).isEqualTo(dto.getLine1());
        assertThat(entity.getCity()).isEqualTo(dto.getCity());
        assertThat(entity.getPostalCode()).isEqualTo(dto.getPostalCode());
        assertThat(entity.getCountry()).isEqualTo(dto.getCountry());
        assertThat(entity.getCustomer()).isNull(); // no se actualiza
        assertThat(entity.getDefaultAddress()).isNull(); // no se actualiza
    }
}
