/*
package com.hometask.orderservice;

import com.hometask.orderservice.dto.ProductDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

*/
/**
 * End-to-End Integration Tests for Order Service
 * Tests complete flows through controllers, services, and external API calls
 *//*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "inventory.service.base-url=http://localhost:0/")
@DisplayName("Order Service E2E Integration Tests")
class OrderServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    private static MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Update the inventory service base URL to point to mock server

//        System.setProperty("inventory.service.base-url", mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ==================== Inventory Endpoint Tests ====================

    @Test
    @DisplayName("E2E: Get all products successfully")
    void testGetAllProducts_Success() throws Exception {
        // Arrange
        String jsonResponse = """
                [
                    {
                        "id": "1",
                        "name": "Product 1",
                        "description": "Description 1",
                        "price": 99.99,
                        "currency": "USD",
                        "category": "Electronics",
                        "stock": 10,
                        "imageUrl": "http://example.com/image1.jpg",
                        "rating": 4.5
                    },
                    {
                        "id": "2",
                        "name": "Product 2",
                        "description": "Description 2",
                        "price": 49.99,
                        "currency": "USD",
                        "category": "Books",
                        "stock": 5,
                        "imageUrl": "http://example.com/image2.jpg",
                        "rating": 4.0
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act & Assert
        mockMvc.perform(get("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[0].price").value(99.99))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].stock").value(5));
    }

    @Test
    @DisplayName("E2E: Handle inventory service failure with fallback")
    void testGetAllProducts_ServiceUnavailable() throws Exception {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503));

        // Act & Assert - Should return 503 through fallback
        mockMvc.perform(get("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("E2E: Get products with empty response")
    void testGetAllProducts_EmptyList() throws Exception {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act & Assert
        mockMvc.perform(get("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Order Placement Tests ====================

    @Test
    @DisplayName("E2E: Place order successfully with sufficient stock")
    void testPlaceOrder_Success() throws Exception {
        // Arrange
        String productsJsonResponse = """
                [
                    {
                        "id": "prod123",
                        "name": "Laptop",
                        "description": "High-end Laptop",
                        "price": 999.99,
                        "currency": "USD",
                        "category": "Electronics",
                        "stock": 50,
                        "imageUrl": "http://example.com/laptop.jpg",
                        "rating": 4.8
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productsJsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act & Assert
        mockMvc.perform(post("/api/orders/place/prod123/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Order placed successfully")));
    }

    @Test
    @DisplayName("E2E: Place order fails with insufficient stock")
    void testPlaceOrder_InsufficientStock() throws Exception {
        // Arrange
        String productsJsonResponse = """
                [
                    {
                        "id": "prod123",
                        "name": "Laptop",
                        "description": "High-end Laptop",
                        "price": 999.99,
                        "currency": "USD",
                        "category": "Electronics",
                        "stock": 2,
                        "imageUrl": "http://example.com/laptop.jpg",
                        "rating": 4.8
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productsJsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act & Assert
        mockMvc.perform(post("/api/orders/place/prod123/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Insufficient stock")));
    }

    @Test
    @DisplayName("E2E: Place order fails when product not found")
    void testPlaceOrder_ProductNotFound() throws Exception {
        // Arrange
        String productsJsonResponse = """
                [
                    {
                        "id": "prod456",
                        "name": "Mouse",
                        "description": "Wireless Mouse",
                        "price": 29.99,
                        "currency": "USD",
                        "category": "Accessories",
                        "stock": 100,
                        "imageUrl": "http://example.com/mouse.jpg",
                        "rating": 4.2
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productsJsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act & Assert
        mockMvc.perform(post("/api/orders/place/nonexistent123/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("E2E: Place order with quantity = 1")
    void testPlaceOrder_MinimumQuantity() throws Exception {
        // Arrange
        String productsJsonResponse = """
                [
                    {
                        "id": "prod789",
                        "name": "Keyboard",
                        "description": "Mechanical Keyboard",
                        "price": 149.99,
                        "currency": "USD",
                        "category": "Accessories",
                        "stock": 30,
                        "imageUrl": "http://example.com/keyboard.jpg",
                        "rating": 4.6
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productsJsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act & Assert
        mockMvc.perform(post("/api/orders/place/prod789/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Order placed successfully")));
    }

    @Test
    @DisplayName("E2E: Place order with large quantity")
    void testPlaceOrder_LargeQuantity() throws Exception {
        // Arrange
        String productsJsonResponse = """
                [
                    {
                        "id": "prod999",
                        "name": "Monitor",
                        "description": "4K Monitor",
                        "price": 399.99,
                        "currency": "USD",
                        "category": "Electronics",
                        "stock": 1000,
                        "imageUrl": "http://example.com/monitor.jpg",
                        "rating": 4.7
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productsJsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act & Assert
        mockMvc.perform(post("/api/orders/place/prod999/500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Order placed successfully")));
    }

    @Test
    @DisplayName("E2E: Multiple sequential order placements")
    void testMultipleOrderPlacements() throws Exception {
        // Arrange
        String productsJsonResponse = """
                [
                    {
                        "id": "prod001",
                        "name": "Widget",
                        "description": "Premium Widget",
                        "price": 19.99,
                        "currency": "USD",
                        "category": "Widgets",
                        "stock": 100,
                        "imageUrl": "http://example.com/widget.jpg",
                        "rating": 4.3
                    }
                ]
                """;

        // Queue multiple responses
        for (int i = 0; i < 3; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setBody(productsJsonResponse)
                    .addHeader("Content-Type", "application/json")
                    .setResponseCode(200));
        }

        // Act & Assert - First order
        mockMvc.perform(post("/api/orders/place/prod001/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Act & Assert - Second order
        mockMvc.perform(post("/api/orders/place/prod001/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Act & Assert - Third order
        mockMvc.perform(post("/api/orders/place/prod001/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("E2E: Order placement with service timeout")
    void testPlaceOrder_ServiceTimeout() throws Exception {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(504)); // Gateway timeout

        // Act & Assert
        mockMvc.perform(post("/api/orders/place/prod-timeout/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }
}

*/
