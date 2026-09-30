package com.dental.dentalbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 * Provides API documentation with JWT bearer token authentication support.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI dentalClinicOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dental Clinic Management System API")
                        .description("REST API for managing a dental clinic: appointments, patients, "
                                + "doctors, services, payments, clinical records, and content.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dental Clinic")
                                .email("info@dentalclinic.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token")));
    }
}
