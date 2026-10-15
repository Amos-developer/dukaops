package com.dukaops.backend.inventory;

import java.math.BigDecimal;
import java.util.UUID;
import com.dukaops.backend.auth.*;
import com.dukaops.backend.branch.inventory.repository.StockLevelRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/inventory/branches/{branchId}/stock/{productId}/reorder-level")
public class InventoryReorderController {
    private final StockLevelRepository stock;
    public InventoryReorderController(StockLevelRepository stock) { this.stock = stock; }
    public record ReorderRequest(@NotNull @DecimalMin("0") @Digits(integer = 11, fraction = 3) BigDecimal reorderLevel) {}
    @PatchMapping @Transactional
    public void update(@AuthenticationPrincipal TenantUserPrincipal principal, @PathVariable UUID branchId,
            @PathVariable UUID productId, @Valid @RequestBody ReorderRequest request) {
        if (principal.getRole() != UserRole.OWNER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can change reorder levels.");
        var level = stock.findStockLevelForUpdate(principal.getTenantId(), branchId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory record not found."));
        level.updateReorderLevel(request.reorderLevel());
    }
}
