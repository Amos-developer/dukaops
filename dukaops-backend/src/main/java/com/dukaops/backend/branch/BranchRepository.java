package com.dukaops.backend.branch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
public interface BranchRepository
        extends JpaRepository<Branch, UUID> {

    List<Branch> findAllByTenant_IdOrderByCreatedAtDesc(
            UUID tenantId);

    Optional<Branch> findByIdAndTenant_Id(
            UUID branchId,
            UUID tenantId);

    boolean existsByTenant_IdAndCode(
            UUID tenantId,
            String code);

    boolean existsByTenant_IdAndMainBranchTrue(
            UUID tenantId);

        @Query("""
        SELECT b FROM Branch b
        WHERE b.id = :branchId
        AND b.tenant.id = :tenantId
        """)
        Optional<Branch> findTenantScopedBranchById(
                @Param("branchId") UUID branchId,
                @Param("tenantId") UUID tenantId
        );

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
        SELECT b FROM Branch b
        WHERE b.id = :branchId
        AND b.tenant.id = :tenantId
        """)
        Optional<Branch> findTenantScopedBranchByIdForUpdate(
                @Param("branchId") UUID branchId,
                @Param("tenantId") UUID tenantId
);
}