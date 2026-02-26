package com.hometask.orderservice.service;

import com.hometask.orderservice.dto.ProductDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InventoryServiceClient
 * Uses MockWebServer to simulate the Inventory Service
 */
class InventoryServiceClientTest {

    private MockWebServer mockWebServer;
    private InventoryServiceClient inventoryServiceClient;

    @BeforeEach
    void setUp() throws IOException {
        // Initialize mock server
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Create WebClient pointing to mock server
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        // Initialize service client with mock WebClient
        inventoryServiceClient = new InventoryServiceClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        String jsonResponse = """
                [
                    {
                        "id": 1,
                        "name": "Product 1",
                        "description": "Description 1",
                        "price": 99.99,
                        "quantity": 10,
                        "category": "Electronics"
                    },
                    {
                        "id": 2,
                        "name": "Product 2",
                        "description": "Description 2",
                        "price": 49.99,
                        "quantity": 20,
                        "category": "Books"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act
        List<ProductDTO> products = inventoryServiceClient.getAllProducts();

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals("Product 1", products.get(0).getName());
        assertEquals(BigDecimal.valueOf( 99.99), products.get(0).getPrice());
        assertEquals("Product 2", products.get(1).getName());
        assertEquals(BigDecimal.valueOf(49.99), products.get(1).getPrice());
    }

    @Test
    void testGetAllProducts_EmptyList() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act
        List<ProductDTO> products = inventoryServiceClient.getAllProducts();

        // Assert
        assertNotNull(products);
        assertEquals(0, products.size());
    }

}

