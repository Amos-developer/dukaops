package com.dukaops.backend.branch.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dukaops.backend.branch.inventory.StockLevel;

public interface StockLevelRepository
        extends JpaRepository<StockLevel, UUID> {

    Optional<StockLevel> findByTenant_IdAndBranch_IdAndProduct_Id(
        UUID tenantId,
        UUID branchId,
        UUID productId
    );

    List<StockLevel> findAllByTenant_IdAndBranch_IdOrderByProduct_NameAsc(
        UUID tenantId,
        UUID branchId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM StockLevel s
        WHERE s.tenant.id = :tenantId
          AND s.branch.id = :branchId
          AND s.product.id = :productId
        """)
    Optional<StockLevel> findForUpdate(
        @Param("tenantId") UUID tenantId,
        @Param("branchId") UUID branchId,
        @Param("productId") UUID productId
    );

        @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM StockLevel s
        WHERE s.tenant.id = :tenantId
        AND s.branch.id = :branchId
        AND s.product.id = :productId
        """)
    Optional<StockLevel> findStockLevelForUpdate(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("productId") UUID productId
    );

    @Query("""
        SELECT s FROM StockLevel s
        WHERE s.tenant.id = :tenantId
        AND s.branch.id = :branchId
        ORDER BY s.product.name ASC
        """)
    List<StockLevel> findStockLevelsForBranch(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId
    );
}
