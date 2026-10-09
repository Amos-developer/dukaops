package com.dukaops.backend.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
}