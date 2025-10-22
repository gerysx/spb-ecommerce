package com.example.delogica.outputs;

import org.junit.jupiter.api.Test;

import com.example.delogica.dtos.output.AddressOutputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerOutputDTOTest {

    @Test
    void testGettersSetters() {
        CustomerOutputDTO dto = new CustomerOutputDTO();
        dto.setId(1L);
        dto.setFullName("Ana Gómez");
        dto.setEmail("ana.gomez@example.com");
        dto.setPhone("987654321");

        List<AddressOutputDTO> addresses = new ArrayList<>();
        dto.setAddresses(addresses);

        assertEquals(1L, dto.getId());
        assertEquals("Ana Gómez", dto.getFullName());
        assertEquals("ana.gomez@example.com", dto.getEmail());
        assertEquals("987654321", dto.getPhone());
        assertEquals(addresses, dto.getAddresses());
    }
}
