package com.rbdip.bookstore.order;

public class OrderValidator {
    public void validateCustomerName(CreateOrderRequest request) {
        if (request.customerFullName() == null || request.customerFullName().isBlank()) {
            throw new IllegalArgumentException("customerFullName is required");
        }
    }

    public void validateCustomerAddress(CreateOrderRequest request) {
        if (request.customerAddress() == null || request.customerAddress().isBlank()) {
            throw new IllegalArgumentException("customerAddress is required");
        }
    }

    public void validateItems(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
    }

    public void validateQuantity(CreateOrderRequest.Item orderItem) {
        if (orderItem.quantity() != null && orderItem.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
