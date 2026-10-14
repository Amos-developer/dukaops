package com.dukaops.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantUserBranchAssignmentRepository
        extends JpaRepository<TenantUserBranchAssignment, UUID> {

    boolean existsByTenant_IdAndUser_IdAndBranch_Id(
            UUID tenantId,
            UUID userId,
            UUID branchId
    );
}
