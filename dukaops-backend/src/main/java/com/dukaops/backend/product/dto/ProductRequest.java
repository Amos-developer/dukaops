package com.dukaops.backend.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank
    @Size(max = 64)
    String sku,

    @NotBlank
    @Size(max = 150)
    String name,

    @Size(max = 5000)
    String description,

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    BigDecimal unitPrice,

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    BigDecimal costPrice,
    java.util.UUID categoryId
) {
    public ProductRequest(String sku, String name, String description, BigDecimal unitPrice, BigDecimal costPrice) {
        this(sku, name, description, unitPrice, costPrice, null);
    }
}
