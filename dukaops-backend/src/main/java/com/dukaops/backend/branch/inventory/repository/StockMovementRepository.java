package com.dukaops.backend.branch.inventory.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dukaops.backend.branch.inventory.StockMovement;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, UUID> {

    List<StockMovement>
        findAllByTenant_IdAndBranch_IdAndProduct_IdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID branchId,
            UUID productId
        );

        @Query("""
            SELECT m FROM StockMovement m
            WHERE m.tenant.id = :tenantId
            AND m.branch.id = :branchId
            AND m.product.id = :productId
            ORDER BY m.createdAt DESC
            """)
        List<StockMovement> findStockMovementHistory(
                @Param("tenantId") UUID tenantId,
                @Param("branchId") UUID branchId,
                @Param("productId") UUID productId
);

        @Query("""
            SELECT m FROM StockMovement m
            WHERE m.tenant.id = :tenantId
            AND m.branch.id = :branchId
            ORDER BY m.createdAt DESC
            """)
        List<StockMovement> findBranchMovementHistory(
                @Param("tenantId") UUID tenantId,
                @Param("branchId") UUID branchId
        );
}
