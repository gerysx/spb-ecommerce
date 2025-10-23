package com.example.delogica.dtos.output;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "Customer", description = "Representa un cliente con sus datos de contacto y direcciones")
public class CustomerOutputDTO {

    @Schema(description = "Identificador único del cliente", example = "123")
    private Long id;

    @Schema(description = "Nombre completo", example = "María López")
    private String fullName;

    @Schema(description = "Correo electrónico del cliente", example = "maria.lopez@example.com")
    private String email;

    @Schema(description = "Teléfono de contacto en formato internacional", example = "+34 600 123 456")
    private String phone;

    @Schema(description = "Listado de direcciones asociadas al cliente")
    private List<AddressOutputDTO> addresses = new ArrayList<>();
}
