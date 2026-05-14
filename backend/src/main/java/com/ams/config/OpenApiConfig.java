package com.ams.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AMS Backend API")
                        .version("1.0.0")
                        .description("Asset Management System - Backend REST API documentation")
                        .contact(new Contact()
                                .name("AMS Team")
                                .email("ams@example.com")))
                .servers(List.of(
                        new Server().url("/").description("Default Server")
                ));
    }
}
