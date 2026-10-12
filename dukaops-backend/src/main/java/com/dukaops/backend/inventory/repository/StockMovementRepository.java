package com.dukaops.backend.inventory.repository;

import java.util.List;
import java.util.UUID;

import com.dukaops.backend.inventory.StockMovement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, UUID> {

    List<StockMovement>
        findAllByTenant_IdAndBranch_IdAndProduct_IdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID branchId,
            UUID productId
        );
}