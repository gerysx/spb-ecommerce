package com.example.delogica.dtos.output;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CustomerOutputDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private List<AddressOutputDTO> addresses = new ArrayList<>();
}
