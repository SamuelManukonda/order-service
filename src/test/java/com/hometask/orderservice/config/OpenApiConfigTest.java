package com.hometask.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OpenApiConfig
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OpenAPI Configuration Tests")
class OpenApiConfigTest {

    @Autowired
    private OpenApiConfig openApiConfig;

    @Test
    @DisplayName("OpenAPI configuration bean is created")
    void testOpenApiConfigBeanCreation() {
        assertNotNull(openApiConfig);
    }

    @Test
    @DisplayName("OpenAPI configuration creates valid OpenAPI instance")
    void testOpenAPIInstance() {
        // Arrange & Act
        OpenAPI openAPI = openApiConfig.openAPI();

        // Assert
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Order Service", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertTrue(openAPI.getInfo().getDescription().contains("Order Service"));
    }
}

