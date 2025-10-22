package com.example.delogica.dtos.input;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerInputDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String fullName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe proporcionar un email válido")
    @Size(max = 30, message = "El email no puede superar los 30 caracteres")
    private String email;

    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe contener exactamente 9 dígitos numéricos")
    private String phone;

    @NotNull(message = "La lista de direcciones no puede ser nula")
    @Valid
    private List<AddressInputDTO> addresses = new ArrayList<>();
}