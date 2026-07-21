package com.ogm.hrms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration. Declares a global HTTP bearer (JWT) security scheme so the
 * Swagger UI "Authorize" dialog can attach {@code Authorization: Bearer <token>} to try-it-out calls.
 * Swagger UI is served at {@code /swagger-ui.html}; the OpenAPI document at {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI hrmsOpenAPI(@Value("${hrms.app.version:0.0.1-SNAPSHOT}") String version) {
        return new OpenAPI()
                .info(new Info()
                        .title("OGM HRMS API")
                        .version(version)
                        .description("Enterprise HR Operations Platform — REST API. "
                                + "Obtain a token via POST /api/v1/auth/login, then click Authorize and paste it.")
                        .contact(new Contact().name("OGM HR Platform Team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
