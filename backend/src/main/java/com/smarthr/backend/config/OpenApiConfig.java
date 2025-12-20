package com.smarthr.backend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de OpenAPI/Swagger UI (si usas springdoc).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiDocs() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartHR API")
                        .description("CRUD de Employees para TFG")
                        .version("v1"));
    }
}
