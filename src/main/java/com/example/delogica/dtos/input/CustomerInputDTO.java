package com.example.delogica.dtos.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Payload para crear/actualizar clientes
 * Reglas de negocio relevantes:
 * - create() lanza 409 si el email ya existe
 * - update() impide cambiar la dirección por defecto y valida consistencia de
 * flags
 * - En updates, direcciones nuevas entran con default=false y se preserva la
 * default existente
 */
@Data
@Schema(name = "CustomerInput", description = "Datos de entrada para creación/actualización de clientes")
public class CustomerInputDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    @Schema(description = "Nombre completo del cliente", example = "María López", maxLength = 120)
    private String fullName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe proporcionar un email válido")
    @Size(max = 30, message = "El email no puede superar los 30 caracteres")
    @Schema(description = "Correo electrónico único del cliente", example = "maria.lopez@example.com", maxLength = 30)
    private String email;

    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe contener exactamente 9 dígitos numéricos")
    @Schema(description = "Teléfono nacional de 9 dígitos (sin prefijo). Para internacional usa E.164 en la capa de negocio si aplica", example = "600123456", pattern = "^[0-9]{9}$", nullable = true)
    private String phone;

    @NotNull(message = "La lista de direcciones no puede ser nula")
    @Valid
    @Schema(description = "Listado de direcciones. Puede ser vacío en updates según tu lógica actual", example = """
            [
              {
                "line1": "Calle Mayor 10",
                "line2": "Piso 3º B",
                "city": "Madrid",
                "postalCode": "28013",
                "country": "ES",
                "defaultAddress": true
              }
            ]
            """)
    private List<AddressInputDTO> addresses = new ArrayList<>();
}
