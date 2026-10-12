package com.dukaops.backend.inventory;

import java.math.BigDecimal;
import java.util.UUID;

import com.dukaops.backend.branch.inventory.StockMovementType;

public record StockMovementResponse(
        UUID id,
        UUID branchId,
        UUID productId,
        StockMovementType movementType,
        BigDecimal quantityChange,
        BigDecimal quantityAfter,
        String reference,
        String notes,
        UUID performedById,
        String createdAt
) {
}