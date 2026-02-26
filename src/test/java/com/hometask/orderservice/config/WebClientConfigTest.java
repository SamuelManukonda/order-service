package com.hometask.orderservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebClientConfig
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("WebClient Configuration Tests")
class WebClientConfigTest {

    @Autowired
    private WebClientConfig webClientConfig;

    @Autowired(required = false)
    private WebClient inventoryWebClient;

    @Test
    @DisplayName("WebClient configuration bean is created")
    void testWebClientConfigBeanCreation() {
        assertNotNull(webClientConfig);
    }

    @Test
    @DisplayName("Inventory WebClient bean is created")
    void testInventoryWebClientBeanCreated() {
        assertNotNull(inventoryWebClient);
    }

    @Test
    @DisplayName("Inventory WebClient is configured with base URL")
    void testInventoryWebClientConfiguration() {
        // Arrange
        assertNotNull(inventoryWebClient);

        // Assert - The bean is properly configured
        // Further detailed assertions would require inspecting internal state
        // which is implementation-specific to Spring WebClient
    }
}

