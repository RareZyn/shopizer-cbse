package com.shopizer.springboot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 * Configures API documentation for all REST endpoints
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI shopizerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shopizer CBSE API")
                        .description("Component-Based E-Commerce Backend API\n\n" +
                                "## Modules\n" +
                                "- **Catalog** (FR-001 to FR-005): Product and Category management\n" +
                                "- **Cart** (FR-006 to FR-009): Shopping cart operations\n" +
                                "- **Order** (FR-010 to FR-014): Order processing\n" +
                                "- **Merchant** (FR-015 to FR-018): Merchant management\n" +
                                "- **Payment** (FR-019 to FR-023): Payment processing\n" +
                                "- **Customer** (FR-024 to FR-027): Customer management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Shopizer CBSE Team")
                                .email("support@shopizer.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
