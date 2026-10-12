package com.dukaops.backend.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.dukaops.backend.product.Product;
import com.dukaops.backend.product.ProductStatus;

public record ProductResponse(
    UUID id,
    UUID tenantId,
    String sku,
    String name,
    String description,
    BigDecimal unitPrice,
    BigDecimal costPrice,
    ProductStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getTenant().getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getUnitPrice(),
            product.getCostPrice(),
            product.getStatus(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}