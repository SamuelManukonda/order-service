package com.hometask.orderservice.service;

import com.hometask.orderservice.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Service class for communicating with the Inventory Service
 * Handles all inventory-related API calls using WebClient
 */
@Service
public class InventoryServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceClient.class);

    private static final String ALL_PRODUCTS_ENDPOINT = "/api/products/all";
    private static final int RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(3);

    private final WebClient inventoryWebClient;

    public InventoryServiceClient(@Qualifier("inventoryWebClient") WebClient inventoryWebClient) {
        this.inventoryWebClient = inventoryWebClient;
        logger.info("InventoryServiceClient initialized successfully");
    }

    /**
     * Fetches all products from the inventory service
     *
     * @return List of ProductDTO objects
     * @throws RuntimeException if the API call fails after retries
     */
    public List<ProductDTO> getAllProducts() {
        logger.info("Fetching all products from inventory service");

        try {
            List<ProductDTO> products = inventoryWebClient
                    .get()
                    .uri(ALL_PRODUCTS_ENDPOINT)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                logger.error("Error response from inventory service: {}", response.statusCode());
                                return response.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            logger.error("Error body: {}", errorBody);
                                            return Mono.error(new RuntimeException(
                                                    "Failed to fetch products. Status: " + response.statusCode() +
                                                            ", Body: " + errorBody));
                                        });
                            }
                    )
                    .bodyToFlux(ProductDTO.class)
                    .collectList()
                    .retryWhen(Retry.backoff(RETRY_ATTEMPTS, RETRY_DELAY)
                            .doBeforeRetry(retrySignal ->
                                    logger.warn("Retrying API call to inventory service. Attempt: {}",
                                            retrySignal.totalRetries() + 1))
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                logger.error("Retry attempts exhausted after {} tries", RETRY_ATTEMPTS);
                                return new RuntimeException("Failed to fetch products after " + RETRY_ATTEMPTS + " attempts");
                            })
                    )
                    .block();

            if (products != null) {
                logger.info("Successfully fetched {} products from inventory service", products.size());
                logger.debug("Products retrieved: {}", products);
                return products;
            } else {
                logger.warn("Received null response from inventory service, returning empty list");
                return Collections.emptyList();
            }

        } catch (WebClientResponseException e) {
            logger.error("WebClient response exception while fetching products. Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error calling inventory service: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching products from inventory service", e);
            throw new RuntimeException("Unexpected error calling inventory service: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches all products from the inventory service asynchronously
     *
     * @return Mono containing List of ProductDTO objects
     */
    public Mono<List<ProductDTO>> getAllProductsAsync() {
        logger.info("Fetching all products from inventory service (async)");

        return inventoryWebClient
                .get()
                .uri(ALL_PRODUCTS_ENDPOINT)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            logger.error("Error response from inventory service: {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        logger.error("Error body: {}", errorBody);
                                        return Mono.error(new RuntimeException(
                                                "Failed to fetch products. Status: " + response.statusCode() +
                                                        ", Body: " + errorBody));
                                    });
                        }
                )
                .bodyToFlux(ProductDTO.class)
                .collectList()
                .retryWhen(reactor.util.retry.Retry.fixedDelay(RETRY_ATTEMPTS, RETRY_DELAY)
                        .doBeforeRetry(retrySignal ->
                                logger.warn("Retrying async API call to inventory service. Attempt: {}",
                                        retrySignal.totalRetries() + 1))
                )
                .doOnSuccess(products -> {
                    if (products != null) {
                        logger.info("Successfully fetched {} products from inventory service (async)", products.size());
                        logger.debug("Products retrieved: {}", products);
                    }
                })
                .doOnError(error ->
                        logger.error("Error fetching products from inventory service (async)", error)
                )
                .onErrorReturn(Collections.emptyList());
    }
}


