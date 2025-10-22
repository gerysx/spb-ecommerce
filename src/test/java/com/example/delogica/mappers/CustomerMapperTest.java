package com.example.delogica.mappers;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;
import com.example.delogica.models.Customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;

public class CustomerMapperTest {

    private CustomerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CustomerMapper.class);
    }

    @Test
    void testToEntity() {
        CustomerInputDTO dto = new CustomerInputDTO();
        dto.setFullName("Juan Pérez");
        dto.setEmail("juan@example.com");
        dto.setPhone("123456789");
        dto.setAddresses(new ArrayList<>()); // aunque se ignoran, es necesario para no ser nulo

        Customer entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getFullName()).isEqualTo(dto.getFullName());
        assertThat(entity.getEmail()).isEqualTo(dto.getEmail());
        assertThat(entity.getPhone()).isEqualTo(dto.getPhone());
       assertThat(entity.getAddresses()).isEmpty(); // se ignora en el mapping
        assertThat(entity.getId()).isNull(); // se ignora el id
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void testToOutput() {
        Customer entity = new Customer();
        entity.setId(1L);
        entity.setFullName("Juan Pérez");
        entity.setEmail("juan@example.com");
        entity.setPhone("123456789");
        entity.setAddresses(new ArrayList<>());

        CustomerOutputDTO output = mapper.toOutput(entity);

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(entity.getId());
        assertThat(output.getFullName()).isEqualTo(entity.getFullName());
        assertThat(output.getEmail()).isEqualTo(entity.getEmail());
        assertThat(output.getPhone()).isEqualTo(entity.getPhone());
        assertThat(output.getAddresses()).isNotNull();
        assertThat(output.getAddresses()).isEmpty();
    }

    @Test
    void testUpdateEntityFromDto() {
        CustomerInputDTO dto = new CustomerInputDTO();
        dto.setFullName("Pedro Martínez");
        dto.setEmail("pedro@example.com");
        dto.setPhone("987654321");
        dto.setAddresses(new ArrayList<>()); // se ignora en el mapper

        Customer entity = new Customer();
        entity.setId(1L);
        entity.setFullName("Juan Pérez");
        entity.setEmail("juan@example.com");
        entity.setPhone("123456789");
        entity.setAddresses(new ArrayList<>());
        entity.setCreatedAt(null); // si tienes fechas, pon objetos válidos aquí
        entity.setUpdatedAt(null);

        mapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getId()).isEqualTo(1L); // no cambia
        assertThat(entity.getFullName()).isEqualTo(dto.getFullName());
        assertThat(entity.getEmail()).isEqualTo(dto.getEmail());
        assertThat(entity.getPhone()).isEqualTo(dto.getPhone());
        assertThat(entity.getAddresses()).isNotNull(); // no cambia (ignorado)
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
