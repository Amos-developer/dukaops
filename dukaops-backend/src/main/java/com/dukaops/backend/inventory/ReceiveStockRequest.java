package com.dukaops.backend.inventory;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReceiveStockRequest(
        @NotNull UUID branchId,
        @NotNull UUID productId,

        @NotNull
        @DecimalMin(value = "0.001")
        BigDecimal quantity,

        @DecimalMin(value = "0.000")
        BigDecimal reorderLevel,

        @Size(max = 100)
        String reference,

        @Size(max = 2000)
        String notes
) {
}