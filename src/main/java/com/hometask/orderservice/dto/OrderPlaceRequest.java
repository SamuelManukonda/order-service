package com.hometask.orderservice.dto;


public record OrderPlaceRequest(String productId, int quantity) {
}
