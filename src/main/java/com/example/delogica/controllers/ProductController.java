package com.example.delogica.controllers;

import com.example.delogica.dtos.input.ProductInputDTO;
import com.example.delogica.dtos.output.ProductOutputDTO;
import com.example.delogica.services.ProductService;
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
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Operaciones de gestión del catálogo de productos")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    /**
     * Crea un producto
     */
    @Operation(
        summary = "Crear producto",
        description = "Crea un nuevo producto. Valida SKU único, precio > 0 y stock ≥ 0",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = ProductInputDTO.class),
                examples = @ExampleObject(
                    name = "Producto válido",
                    value = """
                    {
                      "sku": "SKU-ABC-001",
                      "name": "Café Molido 500g",
                      "description": "Café arábica 100% de tueste natural",
                      "price": 8.95,
                      "stock": 150,
                      "active": true
                    }
                    """
                )
            )
        )
    )
    @ApiResponse(responseCode = "201", description = "Producto creado",
        content = @Content(schema = @Schema(implementation = ProductOutputDTO.class)))
    @ApiResponse(responseCode = "400", description = "Entrada inválida",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SKU ya en uso",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<ProductOutputDTO> create(
            @Valid @RequestBody ProductInputDTO input,
            UriComponentsBuilder ucb) {

        logger.info("Creando producto con SKU: {}", input.getSku());
        ProductOutputDTO out = productService.create(input);
        var location = ucb.path("/api/products/{id}").buildAndExpand(out.getId()).toUri();
        logger.info("Producto creado con ID: {}", out.getId());
        return ResponseEntity.created(location).body(out);
    }

    /**
     * Lista productos con filtros y paginación
     */
    @Operation(
        summary = "Listar productos",
        description = "Devuelve una página de productos filtrando opcionalmente por nombre (contiene, case-insensitive) y estado activo"
    )
    @ApiResponse(responseCode = "200", description = "Página de productos",
        content = @Content(schema = @Schema(implementation = ProductOutputDTO.class)))
    @PageableAsQueryParam
    @GetMapping
    public Page<ProductOutputDTO> list(
            @Parameter(description = "Filtro por nombre (contiene)", example = "café")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filtro por estado activo", example = "true")
            @RequestParam(required = false) Boolean active,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        logger.info("Listando productos name={}, active={}", name, active);
        return productService.search(pageable, name, active);
    }

    /**
     * Obtiene el detalle de un producto por ID
     */
    @Operation(summary = "Detalle producto", description = "Recupera la información completa de un producto por su identificador")
    @ApiResponse(responseCode = "200", description = "Producto encontrado",
        content = @Content(schema = @Schema(implementation = ProductOutputDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @GetMapping("/{id}")
    public ProductOutputDTO detail(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del producto", example = "1000")
            @PathVariable Long id) {
        logger.info("Detalle producto id={}", id);
        return productService.findById(id);
    }

    /**
     * Actualiza un producto existente
     */
    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza un producto existente. Si cambia el SKU, se valida que siga siendo único",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = ProductInputDTO.class),
                examples = @ExampleObject(
                    name = "Actualización básica",
                    value = """
                    {
                      "sku": "SKU-ABC-001",
                      "name": "Café Molido 500g Premium",
                      "description": "Café arábica 100% de tueste natural. Nueva cosecha",
                      "price": 9.50,
                      "stock": 200,
                      "active": true
                    }
                    """
                )
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "Producto actualizado",
        content = @Content(schema = @Schema(implementation = ProductOutputDTO.class)))
    @ApiResponse(responseCode = "400", description = "Entrada inválida",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SKU ya en uso por otro producto",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @PutMapping("/{id}")
    public ProductOutputDTO update(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del producto", example = "1000")
            @PathVariable Long id,
            @Valid @RequestBody ProductInputDTO input) {
        logger.info("Actualizando producto id={}", id);
        return productService.update(id, input);
    }

    /**
     * Desactiva (borrado lógico) un producto
     */
    @Operation(summary = "Eliminar producto", description = "Desactiva el producto (borrado lógico). No elimina registros históricos")
    @ApiResponse(responseCode = "204", description = "Producto desactivado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
        content = @Content(schema = @Schema(implementation = com.example.delogica.config.errors.ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(in = ParameterIn.PATH, description = "Identificador del producto", example = "1000")
            @PathVariable Long id) {
        logger.info("Eliminando (lógico) producto id={}", id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
