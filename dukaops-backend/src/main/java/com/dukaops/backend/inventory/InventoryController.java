package com.dukaops.backend.inventory;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dukaops.backend.auth.TenantUserPrincipal;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/receipts")
    public StockResponse receiveStock(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @Valid @RequestBody ReceiveStockRequest request
    ) {
        return inventoryService.receiveStock(
                principal.getTenantId(),
                principal.getId(),
                request
        );
    }

    @PostMapping("/adjustments")
    public StockResponse adjustStock(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        return inventoryService.adjustStock(
                principal.getTenantId(),
                principal.getId(),
                request
        );
    }

    @GetMapping("/branches/{branchId}/stock")
    public List<StockResponse> getBranchStock(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @PathVariable UUID branchId
    ) {
        return inventoryService.getBranchStock(
                principal.getTenantId(),
                branchId
        );
    }

    @GetMapping("/branches/{branchId}/stock/{productId}/movements")
    public List<StockMovementResponse> getMovementHistory(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @PathVariable UUID branchId,
            @PathVariable UUID productId
    ) {
        return inventoryService.getMovementHistory(
                principal.getTenantId(),
                branchId,
                productId
        );
    }
}