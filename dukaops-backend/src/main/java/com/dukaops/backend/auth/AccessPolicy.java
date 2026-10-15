package com.dukaops.backend.auth;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.dukaops.backend.branch.BranchRepository;

@Component
public class AccessPolicy {
    private final BranchRepository branches;
    private final TenantUserBranchAssignmentRepository assignments;
    public AccessPolicy(BranchRepository branches, TenantUserBranchAssignmentRepository assignments) {
        this.branches = branches; this.assignments = assignments;
    }
    public boolean businessAdmin(TenantUserPrincipal actor) {
        return actor.getRole() == UserRole.OWNER || actor.getRole() == UserRole.ADMIN;
    }
    public void requireBranch(TenantUserPrincipal actor, UUID branchId) {
        branches.findByIdAndTenant_Id(branchId, actor.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found."));
        if (!businessAdmin(actor) && !assignments.existsByTenant_IdAndUser_IdAndBranch_Id(actor.getTenantId(), actor.getUserId(), branchId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this branch.");
    }
}
