package com.dukaops.backend.inventory;

import java.math.BigDecimal;
import java.util.UUID;

public record StockResponse(
        UUID id,
        UUID branchId,
        UUID productId,
        String sku,
        String productName,
        BigDecimal quantity,
        BigDecimal reorderLevel,
        boolean lowStock,
        String updatedAt
) {
}