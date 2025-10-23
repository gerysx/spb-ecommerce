package com.example.delogica.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Customer API",
        version = "1.0.0",
        description = "API para la gestión de clientes y direcciones",
        contact = @Contact(name = "Equipo Delogica", email = "german.alvarez@delogica.example"),
        license = @License(name = "Apache-2.0")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local")
    }
)
public class OpenApiConfig { }
