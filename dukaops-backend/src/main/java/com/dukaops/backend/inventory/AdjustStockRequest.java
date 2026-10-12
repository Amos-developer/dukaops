package com.dukaops.backend.inventory;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdjustStockRequest(
        @NotNull UUID branchId,
        @NotNull UUID productId,
        @NotNull BigDecimal quantityChange,

        @Size(max = 100)
        String reference,

        @Size(max = 2000)
        String notes
) {
}