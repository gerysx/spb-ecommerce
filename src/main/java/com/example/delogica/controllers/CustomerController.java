package com.example.delogica.controllers;

import com.example.delogica.dtos.input.AddressInputDTO;
import com.example.delogica.dtos.input.CustomerInputDTO;
import com.example.delogica.dtos.output.AddressOutputDTO;
import com.example.delogica.dtos.output.CustomerOutputDTO;
import com.example.delogica.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Operaciones de gestión de clientes y sus direcciones")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    /**
     * Crea un nuevo cliente
     */
    @Operation(
        summary = "Crear cliente",
        description = "Crea un nuevo cliente y retorna la entidad creada junto con la cabecera Location",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = CustomerInputDTO.class),
                examples = {
                    @ExampleObject(
                        name = "Cliente básico",
                        value = """
                        {
                          "name": "Ana Pérez",
                          "email": "ana.perez@example.com",
                          "phone": "+34 600 123 456"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Cliente con datos completos",
                        value = """
                        {
                          "name": "Juan López",
                          "email": "juan.lopez@example.com",
                          "phone": "+34 699 111 222"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponse(responseCode = "201", description = "Cliente creado",
        content = @Content(schema = @Schema(implementation = CustomerOutputDTO.class)))
    @ApiResponse(responseCode = "400", description = "Entrada inválida",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email ya en uso",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<CustomerOutputDTO> createCustomer(
            @Valid @RequestBody @Parameter(description = "Datos del cliente a crear", required = true) CustomerInputDTO input
    ) {
        logger.info("Creando cliente con email: {}", input.getEmail());
        CustomerOutputDTO created = customerService.create(input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        logger.info("Cliente creado con ID: {}", created.getId());
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Lista clientes paginados con filtro opcional por email
     */
    @Operation(
        summary = "Listar clientes",
        description = "Obtiene una página de clientes, permitiendo filtrar por coincidencia parcial del email"
    )
    @ApiResponse(responseCode = "200", description = "Página de clientes",
        content = @Content(schema = @Schema(implementation = CustomerOutputDTO.class)))
    @PageableAsQueryParam
    @GetMapping
    public Page<CustomerOutputDTO> listCustomers(
            @Parameter(description = "Filtro de búsqueda por email, coincidencia parcial", example = "example.com")
            @RequestParam(required = false) String email,
            @ParameterObject Pageable pageable
    ) {
        logger.info("Listando clientes con filtro email: {}", email);
        Page<CustomerOutputDTO> result = (email != null && !email.isBlank())
                ? customerService.searchCustomers(email, pageable)
                : customerService.findAll(pageable);
        logger.info("Se encontraron {} clientes", result.getTotalElements());
        return result;
    }

    /**
     * Recupera un cliente por su identificador
     */
    @Operation(
        summary = "Obtener cliente por ID",
        description = "Devuelve el cliente asociado al identificador proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Cliente encontrado",
        content = @Content(schema = @Schema(implementation = CustomerOutputDTO.class)))
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @GetMapping("/{id}")
    public CustomerOutputDTO getCustomer(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del cliente", example = "123", required = true)
            @PathVariable Long id
    ) {
        logger.info("Buscando cliente con ID: {}", id);
        CustomerOutputDTO customer = customerService.findById(id);
        logger.info("Cliente encontrado con ID: {}", id);
        return customer;
    }

    /**
     * Actualiza la información de un cliente existente
     */
    @Operation(
        summary = "Actualizar cliente",
        description = "Actualiza los datos del cliente indicado por ID",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = CustomerInputDTO.class),
                examples = @ExampleObject(
                    name = "Actualización básica",
                    value = """
                    {
                      "name": "Ana P. Gómez",
                      "email": "ana.pg@example.com",
                      "phone": "+34 600 987 654"
                    }
                    """
                )
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "Cliente actualizado",
        content = @Content(schema = @Schema(implementation = CustomerOutputDTO.class)))
    @ApiResponse(responseCode = "400", description = "Entrada inválida",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Cliente o dirección no encontrada",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "No se permite cambiar la dirección por defecto desde esta operación",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @PutMapping("/{id}")
    public CustomerOutputDTO updateCustomer(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del cliente", example = "123", required = true)
            @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "Datos a actualizar del cliente", required = true) CustomerInputDTO input
    ) {
        logger.info("Actualizando cliente con ID: {}", id);
        CustomerOutputDTO response = customerService.update(id, input);
        logger.info("Cliente actualizado con ID: {}", id);
        return response;
    }

    /**
     * Elimina un cliente existente
     */
    @Operation(
        summary = "Eliminar cliente",
        description = "Elimina el cliente indicado por ID"
    )
    @ApiResponse(responseCode = "204", description = "Cliente eliminado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del cliente", example = "123", required = true)
            @PathVariable Long id
    ) {
        logger.info("Eliminando cliente con ID: {}", id);
        customerService.delete(id);
        logger.info("Cliente eliminado con ID: {}", id);
    }

    /**
     * Crea una nueva dirección para un cliente
     */
    @Operation(
        summary = "Crear dirección de un cliente",
        description = "Crea una dirección para el cliente indicado. Si es la primera dirección del cliente, se marcará por defecto de forma automática. No se permite solicitar default por esta operación",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = AddressInputDTO.class),
                examples = {
                    @ExampleObject(
                        name = "Dirección básica",
                        value = """
                        {
                          "street": "Calle Mayor 1",
                          "city": "Madrid",
                          "province": "Madrid",
                          "postalCode": "28013",
                          "country": "ES"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponse(responseCode = "201", description = "Dirección creada",
        content = @Content(schema = @Schema(implementation = AddressOutputDTO.class)))
    @ApiResponse(responseCode = "400", description = "Entrada inválida",
        content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
        content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Cambio de defaultAddress no permitido en esta operación",
        content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @PostMapping("/{id}/addresses")
    public ResponseEntity<AddressOutputDTO> createAddress(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del cliente", example = "123", required = true)
            @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "Datos de la nueva dirección", required = true) AddressInputDTO input
    ) {
        logger.info("Creando dirección para cliente ID: {}", id);
        AddressOutputDTO created = customerService.createAddress(id, input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{addressId}")
                .buildAndExpand(created.getId())
                .toUri();

        logger.info("Dirección {} creada para cliente {}", created.getId(), id);
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Marca una dirección como predeterminada para un cliente
     */
    @Operation(
        summary = "Marcar dirección como predeterminada",
        description = "Establece la dirección indicada como la dirección por defecto del cliente. Las demás direcciones quedarán desmarcadas automáticamente"
    )
    @ApiResponse(responseCode = "204", description = "Default actualizada")
    @ApiResponse(responseCode = "404", description = "Cliente o dirección no encontrada",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @PutMapping("/{id}/addresses/{addressId}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAddressAsDefault(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del cliente", example = "123", required = true)
            @PathVariable Long id,
            @Parameter(in = ParameterIn.PATH, description = "Identificador de la dirección", example = "45", required = true)
            @PathVariable Long addressId
    ) {
        logger.info("Marcando dirección {} como predeterminada para cliente {}", addressId, id);
        customerService.setDefaultAddress(id, addressId);
        logger.info("Dirección {} marcada como predeterminada para cliente {}", addressId, id);
    }
}
