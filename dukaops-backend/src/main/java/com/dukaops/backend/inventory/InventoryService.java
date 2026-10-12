package com.dukaops.backend.inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.dukaops.backend.auth.TenantUser;
import com.dukaops.backend.auth.TenantUserRepository;
import com.dukaops.backend.branch.Branch;
import com.dukaops.backend.branch.BranchRepository;
import com.dukaops.backend.branch.inventory.StockLevel;
import com.dukaops.backend.branch.inventory.repository.StockLevelRepository;
import com.dukaops.backend.branch.inventory.repository.StockMovementRepository;
import com.dukaops.backend.branch.inventory.StockMovementType;
import com.dukaops.backend.product.Product;
import com.dukaops.backend.product.ProductRepository;

@Service
public class InventoryService {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final TenantUserRepository tenantUserRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryService(
            BranchRepository branchRepository,
            ProductRepository productRepository,
            TenantUserRepository tenantUserRepository,
            StockLevelRepository stockLevelRepository,
            StockMovementRepository stockMovementRepository
    ) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public StockResponse receiveStock(
            UUID tenantId,
            UUID userId,
            ReceiveStockRequest request
    ) {
        if (request.quantity() == null
                || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Received quantity must be greater than zero.");
        }

        return changeStock(
                tenantId,
                userId,
                request.branchId(),
                request.productId(),
                request.quantity(),
                StockMovementType.RECEIPT,
                request.reorderLevel(),
                request.reference(),
                request.notes()
        );
    }

    @Transactional
    public StockResponse adjustStock(
            UUID tenantId,
            UUID userId,
            AdjustStockRequest request
    ) {
        if (request.quantityChange() == null
                || request.quantityChange().compareTo(BigDecimal.ZERO) == 0) {
            throw badRequest("Adjustment quantity must not be zero.");
        }

        return changeStock(
                tenantId,
                userId,
                request.branchId(),
                request.productId(),
                request.quantityChange(),
                StockMovementType.ADJUSTMENT,
                null,
                request.reference(),
                request.notes()
        );
    }

    private StockResponse changeStock(
            UUID tenantId,
            UUID userId,
            UUID branchId,
            UUID productId,
            BigDecimal delta,
            StockMovementType movementType,
            BigDecimal requestedReorderLevel,
            String reference,
            String notes
    ) {
        // Lock the branch while changing inventory.
        Branch branch = branchRepository
                .findTenantScopedBranchByIdForUpdate(branchId, tenantId)
                .orElseThrow(() -> notFound("Branch not found for this business."));

        Product product = productRepository
                .findTenantScopedProductById(productId, tenantId)
                .orElseThrow(() -> notFound("Product not found for this business."));

        TenantUser performedBy = tenantUserRepository
                .findTenantScopedUserById(userId, tenantId)
                .orElseThrow(() -> notFound("Authenticated user was not found."));

        var existing = stockLevelRepository.findStockLevelForUpdate(
                tenantId, branchId, productId
        );

        StockLevel stock;

        if (existing.isPresent()) {
            stock = existing.get();

            if (requestedReorderLevel != null) {
                if (requestedReorderLevel.compareTo(BigDecimal.ZERO) < 0) {
                    throw badRequest("Reorder level cannot be negative.");
                }
                stock.updateReorderLevel(requestedReorderLevel);
            }
        } else {
            BigDecimal initialReorderLevel = requestedReorderLevel == null
                    ? BigDecimal.ZERO
                    : requestedReorderLevel;

            if (initialReorderLevel.compareTo(BigDecimal.ZERO) < 0) {
                throw badRequest("Reorder level cannot be negative.");
            }

            stock = new StockLevel(
                    branch.getTenant(),
                    branch,
                    product,
                    initialReorderLevel
            );
        }

        BigDecimal resultingQuantity = stock.getQuantity().add(delta);

        if (resultingQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw badRequest("Insufficient stock. Quantity cannot become negative.");
        }

        stock.applyQuantityChange(delta);

        StockLevel savedStock = stockLevelRepository.save(stock);

        StockMovement movement = new StockMovement(
                branch.getTenant(),
                branch,
                product,
                movementType,
                delta,
                savedStock.getQuantity(),
                reference,
                notes,
                performedBy
        );

        stockMovementRepository.save(movement);

        return toStockResponse(savedStock);
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getBranchStock(
            UUID tenantId,
            UUID branchId
    ) {
        branchRepository.findTenantScopedBranchById(branchId, tenantId)
                .orElseThrow(() -> notFound("Branch not found for this business."));

        return stockLevelRepository
                .findStockLevelsForBranch(tenantId, branchId)
                .stream()
                .map(this::toStockResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementHistory(
            UUID tenantId,
            UUID branchId,
            UUID productId
    ) {
        branchRepository.findTenantScopedBranchById(branchId, tenantId)
                .orElseThrow(() -> notFound("Branch not found for this business."));

        productRepository.findTenantScopedProductById(productId, tenantId)
                .orElseThrow(() -> notFound("Product not found for this business."));

        return stockMovementRepository
                .findStockMovementHistory(tenantId, branchId, productId)
                .stream()
                .map(movement -> new StockMovementResponse(
                        movement.getId(),
                        movement.getBranch().getId(),
                        movement.getProduct().getId(),
                        movement.getMovementType(),
                        movement.getQuantityChange(),
                        movement.getQuantityAfter(),
                        movement.getReference(),
                        movement.getNotes(),
                        movement.getPerformedBy() == null
                                ? null
                                : movement.getPerformedBy().getId(),
                        movement.getCreatedAt() == null
                                ? null
                                : movement.getCreatedAt().toString()
                ))
                .toList();
    }

    private StockResponse toStockResponse(StockLevel stock) {
        boolean lowStock =
                stock.getReorderLevel().compareTo(BigDecimal.ZERO) > 0
                && stock.getQuantity().compareTo(stock.getReorderLevel()) <= 0;

        return new StockResponse(
                stock.getId(),
                stock.getBranch().getId(),
                stock.getProduct().getId(),
                stock.getProduct().getSku(),
                stock.getProduct().getName(),
                stock.getQuantity(),
                stock.getReorderLevel(),
                lowStock,
                stock.getUpdatedAt() == null
                        ? null
                        : stock.getUpdatedAt().toString()
        );
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}